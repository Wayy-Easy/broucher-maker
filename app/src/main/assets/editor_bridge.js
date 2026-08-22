(function() {
    let selectedElement = null;
    let selectionOverlay = null;
    let resizeHandle = null;

    let isDragging = false;
    let isResizing = false;
    let startX, startY;
    let startLeft, startTop, startWidth, startHeight;

    function init() {
        createSelectionOverlay();
        document.body.addEventListener('click', handleElementClick, true);

        document.addEventListener('mousemove', handleMouseMove);
        document.addEventListener('mouseup', handleMouseUp);
        document.addEventListener('touchmove', handleMouseMove, { passive: false });
        document.addEventListener('touchend', handleMouseUp);

        setupMutationObserver();
        console.log("Editor bridge initialized");

        // Report initial content so Android has the full HTML (including viewport meta) immediately
        if (window.AndroidBridge) {
            window.AndroidBridge.onContentChanged(EditorApi.getHtml());
        }
    }

    function createSelectionOverlay() {
        selectionOverlay = document.createElement('div');
        selectionOverlay.className = 'bc-editor-overlay';
        selectionOverlay.style.position = 'absolute';
        selectionOverlay.style.border = '2px solid #4648D4';
        selectionOverlay.style.zIndex = '999999';
        selectionOverlay.style.display = 'none';
        selectionOverlay.style.cursor = 'move';
        selectionOverlay.style.boxSizing = 'border-box';

        // Touch/Mouse events for dragging
        selectionOverlay.addEventListener('mousedown', startDrag);
        selectionOverlay.addEventListener('touchstart', startDrag, { passive: false });

        resizeHandle = document.createElement('div');
        resizeHandle.style.position = 'absolute';
        resizeHandle.style.width = '24px';
        resizeHandle.style.height = '24px';
        resizeHandle.style.backgroundColor = '#4648D4';
        resizeHandle.style.right = '-12px';
        resizeHandle.style.bottom = '-12px';
        resizeHandle.style.borderRadius = '50%';
        resizeHandle.style.cursor = 'nwse-resize';
        resizeHandle.style.border = '2px solid white';

        resizeHandle.addEventListener('mousedown', startResize);
        resizeHandle.addEventListener('touchstart', startResize, { passive: false });

        selectionOverlay.appendChild(resizeHandle);
        document.body.appendChild(selectionOverlay);
    }

    function startDrag(e) {
        if (isResizing) return;
        e.preventDefault();
        e.stopPropagation();

        isDragging = true;
        const touch = e.touches ? e.touches[0] : e;
        startX = touch.clientX;
        startY = touch.clientY;

        const styles = window.getComputedStyle(selectedElement);
        startLeft = parseInt(styles.left) || selectedElement.offsetLeft;
        startTop = parseInt(styles.top) || selectedElement.offsetTop;

        // Ensure element is positioned for dragging
        if (styles.position === 'static') {
            selectedElement.style.position = 'relative';
        }
    }

    function startResize(e) {
        e.preventDefault();
        e.stopPropagation();

        isResizing = true;
        const touch = e.touches ? e.touches[0] : e;
        startX = touch.clientX;
        startY = touch.clientY;

        const styles = window.getComputedStyle(selectedElement);
        startWidth = parseInt(styles.width);
        startHeight = parseInt(styles.height);
    }

    function handleMouseMove(e) {
        if (!isDragging && !isResizing) return;
        if (e.cancelable) e.preventDefault();

        const touch = e.touches ? e.touches[0] : e;
        const dx = touch.clientX - startX;
        const dy = touch.clientY - startY;

        if (isDragging) {
            selectedElement.style.left = (startLeft + dx) + 'px';
            selectedElement.style.top = (startTop + dy) + 'px';
        } else if (isResizing) {
            selectedElement.style.width = Math.max(20, startWidth + dx) + 'px';
            selectedElement.style.height = Math.max(20, startHeight + dy) + 'px';
        }

        updateSelectionOverlay();
    }

    function handleMouseUp() {
        if (isDragging || isResizing) {
            isDragging = false;
            isResizing = false;
            reportChange();
        }
    }

    function handleElementClick(e) {
        // If we clicked the overlay or its handle, don't change selection
        if (e.target === selectionOverlay || e.target === resizeHandle) {
            return;
        }

        // If we are clicking a text element that is already selected and focused,
        // let the event pass through to allow caret movement/typing.
        if (selectedElement && selectedElement === e.target && selectedElement.contentEditable === "true") {
            return;
        }

        let target = e.target;

        // Don't select the body/html
        if (target === document.body || target === document.documentElement) {
            deselect();
            return;
        }

        selectElement(target);

        e.preventDefault();
        e.stopPropagation();
    }

    function selectElement(el) {
        if (selectedElement) {
            selectedElement.removeAttribute('contenteditable');
        }

        selectedElement = el;
        updateSelectionOverlay();

        // Get computed styles to send to Android
        const styles = window.getComputedStyle(el);
        const data = {
            tagName: el.tagName,
            id: el.id,
            text: el.innerText,
            color: rgbToHex(styles.color),
            backgroundColor: rgbToHex(styles.backgroundColor),
            fontSize: styles.fontSize,
            fontWeight: styles.fontWeight,
            fontFamily: styles.fontFamily,
            textAlign: styles.textAlign,
            lineHeight: styles.lineHeight,
            padding: styles.padding,
            margin: styles.margin,
            borderRadius: styles.borderRadius,
            boxShadow: styles.boxShadow,
            src: el.tagName === 'IMG' ? el.src : null,
            objectFit: el.tagName === 'IMG' ? styles.objectFit : null
        };

        if (window.AndroidBridge) {
            window.AndroidBridge.onElementSelected(JSON.stringify(data));
        }

        // Enable inline editing for text nodes
        if (isTextNode(el)) {
            el.contentEditable = "true";
            // Prevent Enter from creating divs
            el.onkeydown = function(e) {
                if (e.key === 'Enter') {
                    document.execCommand('insertLineBreak');
                    return false;
                }
            };
            el.oninput = function() {
                updateSelectionOverlay();
                reportChange();
            };
        }
    }

    function deselect() {
        if (selectedElement) {
            selectedElement.removeAttribute('contenteditable');
            selectedElement = null;
        }
        selectionOverlay.style.display = 'none';
        if (window.AndroidBridge) {
            window.AndroidBridge.onElementSelected(null);
        }
    }

    function updateSelectionOverlay() {
        if (!selectedElement) return;
        const rect = selectedElement.getBoundingClientRect();
        const scrollX = window.pageXOffset || document.documentElement.scrollLeft;
        const scrollY = window.pageYOffset || document.documentElement.scrollTop;

        selectionOverlay.style.display = 'block';
        selectionOverlay.style.width = rect.width + 'px';
        selectionOverlay.style.height = rect.height + 'px';
        selectionOverlay.style.left = (rect.left + scrollX) + 'px';
        selectionOverlay.style.top = (rect.top + scrollY) + 'px';
    }

    function isTextNode(el) {
        return ['H1', 'H2', 'H3', 'H4', 'H5', 'H6', 'P', 'SPAN', 'A', 'BUTTON', 'LI', 'TD', 'TH', 'SMALL', 'STRONG', 'EM', 'B', 'I'].includes(el.tagName);
    }

    function rgbToHex(rgb) {
        if (!rgb || rgb === 'transparent' || rgb === 'rgba(0, 0, 0, 0)') return '#00000000';
        const vals = rgb.match(/^rgba?\((\d+),\s*(\d+),\s*(\d+)(?:,\s*([\d.]+))?\)$/);
        if (!vals) return rgb;
        const r = parseInt(vals[1]).toString(16).padStart(2, '0');
        const g = parseInt(vals[2]).toString(16).padStart(2, '0');
        const b = parseInt(vals[3]).toString(16).padStart(2, '0');
        return `#${r}${g}${b}`;
    }

    function reportChange() {
        if (window.AndroidBridge) {
            window.AndroidBridge.onContentChanged(EditorApi.getHtml());
        }
    }

    // Exposed functions for Android
    window.EditorApi = {
        updateStyle: function(property, value) {
            if (selectedElement) {
                selectedElement.style[property] = value;
                updateSelectionOverlay();
                reportChange();
            }
        },
        updateText: function(text) {
            if (selectedElement) {
                selectedElement.innerText = text;
                updateSelectionOverlay();
                reportChange();
            }
        },
        setImage: function(url) {
            if (selectedElement) {
                if (selectedElement.tagName === 'IMG') {
                    selectedElement.src = url;
                } else {
                    selectedElement.style.backgroundImage = `url(${url})`;
                }
                updateSelectionOverlay();
                reportChange();
            }
        },
        duplicate: function() {
            if (selectedElement) {
                const clone = selectedElement.cloneNode(true);
                selectedElement.parentNode.insertBefore(clone, selectedElement.nextSibling);
                selectElement(clone);
                reportChange();
            }
        },
        delete: function() {
            if (selectedElement) {
                const parent = selectedElement.parentNode;
                selectedElement.remove();
                deselect();
                reportChange();
            }
        },
        moveUp: function() {
            if (selectedElement && selectedElement.previousElementSibling) {
                selectedElement.parentNode.insertBefore(selectedElement, selectedElement.previousElementSibling);
                updateSelectionOverlay();
                reportChange();
            }
        },
        moveDown: function() {
            if (selectedElement && selectedElement.nextElementSibling) {
                selectedElement.parentNode.insertBefore(selectedElement.nextElementSibling, selectedElement);
                updateSelectionOverlay();
                reportChange();
            }
        },
        addText: function(text) {
            const el = document.createElement('p');
            el.innerText = text;
            el.style.position = 'absolute';
            el.style.left = '50%';
            el.style.top = '50%';
            el.style.transform = 'translate(-50%, -50%)';
            el.style.fontSize = '24px';
            el.style.color = '#111C2D';
            el.style.zIndex = '1000';
            document.body.appendChild(el);
            selectElement(el);
            reportChange();
        },
        addShape: function(type) {
            const el = document.createElement('div');
            el.style.position = 'absolute';
            el.style.left = '50%';
            el.style.top = '50%';
            el.style.transform = 'translate(-50%, -50%)';
            el.style.width = '100px';
            el.style.height = '100px';
            el.style.backgroundColor = '#4648D4';
            el.style.zIndex = '1000';
            if (type === 'CIRCLE') {
                el.style.borderRadius = '50%';
            } else if (type === 'RECTANGLE') {
                el.style.borderRadius = '8px';
            }
            document.body.appendChild(el);
            selectElement(el);
            reportChange();
        },
        getHtml: function() {
            // Remove editor-specific elements before returning
            selectionOverlay.style.display = 'none';
            const html = document.documentElement.outerHTML;
            if (selectedElement) selectionOverlay.style.display = 'block';
            return html;
        }
    };

    function setupMutationObserver() {
        const observer = new MutationObserver(() => {
            if (selectedElement) updateSelectionOverlay();
        });
        observer.observe(document.body, { attributes: true, childList: true, subtree: true });
        window.addEventListener('resize', updateSelectionOverlay);
    }

    init();
})();
