(function() {
    let selectedElement = null;
    let selectionOverlay = null;

    function init() {
        createSelectionOverlay();
        document.body.addEventListener('click', handleElementClick, true);
        setupMutationObserver();
        console.log("Editor bridge initialized");
    }

    function createSelectionOverlay() {
        selectionOverlay = document.createElement('div');
        selectionOverlay.style.position = 'absolute';
        selectionOverlay.style.border = '2px solid #4648D4';
        selectionOverlay.style.pointerEvents = 'none';
        selectionOverlay.style.zIndex = '999999';
        selectionOverlay.style.display = 'none';
        selectionOverlay.style.borderRadius = '2px';
        selectionOverlay.style.boxShadow = '0 0 0 9999px rgba(0,0,0,0.1)';
        document.body.appendChild(selectionOverlay);
    }

    function handleElementClick(e) {
        // If we are clicking a text element that is already selected and focused,
        // let the event pass through to allow caret movement/typing.
        if (selectedElement && selectedElement === e.target && selectedElement.contentEditable === "true") {
            return;
        }

        e.preventDefault();
        e.stopPropagation();

        let target = e.target;

        // Don't select the overlay itself or the body/html
        if (target === document.body || target === document.documentElement) {
            deselect();
            return;
        }

        selectElement(target);
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
                if (window.AndroidBridge) {
                    window.AndroidBridge.onContentChanged(EditorApi.getHtml());
                }
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
        return ['H1', 'H2', 'H3', 'H4', 'H5', 'H6', 'P', 'SPAN', 'A', 'BUTTON', 'LI', 'TD'].includes(el.tagName);
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

    // Exposed functions for Android
    window.EditorApi = {
        updateStyle: function(property, value) {
            if (selectedElement) {
                selectedElement.style[property] = value;
                updateSelectionOverlay();
            }
        },
        updateText: function(text) {
            if (selectedElement) {
                selectedElement.innerText = text;
                updateSelectionOverlay();
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
            }
        },
        duplicate: function() {
            if (selectedElement) {
                const clone = selectedElement.cloneNode(true);
                selectedElement.parentNode.insertBefore(clone, selectedElement.nextSibling);
                selectElement(clone);
            }
        },
        delete: function() {
            if (selectedElement) {
                const parent = selectedElement.parentNode;
                selectedElement.remove();
                deselect();
            }
        },
        moveUp: function() {
            if (selectedElement && selectedElement.previousElementSibling) {
                selectedElement.parentNode.insertBefore(selectedElement, selectedElement.previousElementSibling);
                updateSelectionOverlay();
            }
        },
        moveDown: function() {
            if (selectedElement && selectedElement.nextElementSibling) {
                selectedElement.parentNode.insertBefore(selectedElement.nextElementSibling, selectedElement);
                updateSelectionOverlay();
            }
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
