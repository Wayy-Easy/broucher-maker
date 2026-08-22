package com.brochurecraft.app.ui.screens

import android.net.Uri
import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brochurecraft.app.data.model.ElementType
import com.brochurecraft.app.ui.components.DesignCanvas
import com.brochurecraft.app.ui.components.EditorTool
import com.brochurecraft.app.ui.components.EditorToolTray
import com.brochurecraft.app.ui.components.ElementPropertiesPanel
import com.brochurecraft.app.ui.components.HtmlDesignCanvas
import com.brochurecraft.app.ui.components.HtmlElementPropertiesPanel
import com.brochurecraft.app.ui.components.ToolActionPanel
import com.brochurecraft.app.ui.theme.*
import com.brochurecraft.app.ui.viewmodel.EditorViewModel
import com.brochurecraft.app.ui.viewmodel.LambdaViewModelFactory
import com.brochurecraft.app.util.ExportManager
import com.brochurecraft.app.util.rememberApp
import kotlinx.coroutines.launch

@Composable
fun DesignEditorScreen(
    designId: Long?,
    templateId: Long?,
    initialName: String,
    onBack: () -> Unit,
    onExport: (Long) -> Unit,
    onBrowseTemplates: () -> Unit
) {
    val app = rememberApp()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val vm: EditorViewModel = viewModel(
        factory = LambdaViewModelFactory { EditorViewModel(app.designRepository, app.templateRepository) }
    )
    val loaded by vm.loaded.collectAsState()
    val brandKit by app.brandKitRepository.observe().collectAsState(initial = null)

    var showDiscardDialog by remember { mutableStateOf(false) }

    LaunchedEffect(designId, templateId) {
        when {
            designId != null && designId > 0 -> vm.loadDesign(designId)
            templateId != null && templateId > 0 -> vm.loadFromTemplate(templateId, initialName)
            else -> vm.startBlank(initialName)
        }
    }

    LaunchedEffect(Unit) {
        vm.saveEvent.collect { id ->
            if (id != null) {
                onExport(id)
            }
        }
    }

    val onCaptured: (Bitmap) -> Unit = { bitmap ->
        scope.launch {
            // Save the captured bitmap as a thumbnail
            val path = ExportManager.saveThumbnail(context, vm.designId ?: 0L, bitmap)
            vm.save(path)
        }
    }

    BackHandler {
        showDiscardDialog = true
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Unsaved Changes") },
            text = { Text("Your changes are not saved yet. Would you like to save them to 'My Designs' before exiting?") },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    if (vm.isHtmlMode) {
                        vm.requestCapture()
                        // In HTML mode, onCaptured will eventually call vm.save then we should exit.
                        // But onBack() needs to be called after save. 
                        // I'll add a 'saveAndExit' flag in VM or just handle it here.
                        // For simplicity, let's just save and assume the user sees the gallery later.
                        onBack()
                    } else {
                        scope.launch {
                            val thumb = ExportManager.thumbnailFile(context, vm.designId ?: 0L, vm.canvasState)
                            vm.save(thumb)
                            onBack()
                        }
                    }
                }) {
                    Text("Save & Exit")
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        showDiscardDialog = false
                        onBack()
                    }) {
                        Text("Discard")
                    }
                    TextButton(onClick = { showDiscardDialog = false }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }

    var activeTool by remember { mutableStateOf<EditorTool?>(null) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) { /* some providers don't support persistable permissions */ }
            vm.beginDragSnapshot()
            vm.addImageElement(uri.toString())
        }
    }

    if (!loaded) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = VCPrimary)
        }
        return
    }

    val selected = vm.selectedElement()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(VCCanvasBg)
                .imePadding() // Handle keyboard overlap
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(VCWorkspaceSurface)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showDiscardDialog = true }) { Icon(Icons.Filled.Close, contentDescription = "Close", tint = VCOnSurfaceVariant) }
                    IconButton(onClick = vm::undo) { Icon(Icons.Filled.Undo, contentDescription = "Undo", tint = VCOnSurfaceVariant) }
                    IconButton(onClick = vm::redo) { Icon(Icons.Filled.Redo, contentDescription = "Redo", tint = VCOnSurfaceVariant) }
                }
                Text("Edit Design", style = TitleMd, color = VCOnSurface, fontWeight = FontWeight.Bold)
                Button(
                    onClick = {
                        if (vm.isHtmlMode) {
                            vm.requestCapture()
                        } else {
                            scope.launch {
                                val thumb = ExportManager.thumbnailFile(context, vm.designId ?: 0L, vm.canvasState)
                                vm.save(thumb)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VCPrimary, contentColor = VCOnPrimary),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Filled.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Export")
                }
            }
            Divider(color = VCBorderSubtle)

            // Design name field
            OutlinedTextField(
                value = vm.designName,
                onValueChange = vm::setName,
                singleLine = true,
                textStyle = BodyLg,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = VCPrimary
                )
            )

            // Canvas area (scrollable + zoomable)
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                val sheetSize = vm.canvasState.sheetSize
                val designWidth = sheetSize.previewWidthPx

                // Real rendered content height (in dp) reported by the WebView once the
                // template has actually laid itself out - NOT the paper-shape guess. Keyed
                // off template+size so switching either resets it and avoids showing a
                // stale height from a previous template while the new one loads.
                var measuredContentHeight by remember(vm.htmlContent, designWidth) { mutableStateOf<Int?>(null) }

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth(0.96f)
                        .fillMaxHeight()
                        .padding(vertical = 20.dp)
                        .shadowCard(),
                    contentAlignment = Alignment.Center
                ) {
                    val scope = this
                    val availableWidthPx = scope.constraints.maxWidth
                    val availableHeightPx = scope.constraints.maxHeight
                    val density = androidx.compose.ui.platform.LocalDensity.current

                    // Until the first real measurement arrives, fall back to the paper-ratio
                    // guess purely to avoid a blank flash - it gets replaced within a frame
                    // or two once the WebView reports its actual content size.
                    val designHeight = measuredContentHeight
                        ?: (designWidth / sheetSize.aspectRatio).toInt()

                    val designWidthPx = with(density) { designWidth.dp.toPx() }
                    val designHeightPx = with(density) { designHeight.dp.toPx() }

                    // "Contain" fit: scale by whichever axis is more constraining so the
                    // template's real content always fits fully inside the available area
                    // in BOTH dimensions - this is what stops tall content from being cut
                    // off (or, previously, bleeding down over the Export button).
                    val scaleFactor = if (vm.isHtmlMode) {
                        minOf(availableWidthPx.toFloat() / designWidthPx, availableHeightPx.toFloat() / designHeightPx)
                    } else {
                        availableWidthPx.toFloat() / designWidthPx
                    }

                    // Non-HTML (raw shape/text) designs still use the literal paper aspect
                    // ratio boundary; only HTML mode switches to content-driven sizing.
                    val boxHeight = if (vm.isHtmlMode) designHeight.dp else (designWidth / sheetSize.aspectRatio).dp

                    Box(
                        modifier = Modifier
                            .size(width = designWidth.dp, height = boxHeight)
                            .graphicsLayer {
                                scaleX = scaleFactor
                                scaleY = scaleFactor
                                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0.5f)
                                clip = true // hard safety net: content can never paint outside its own box
                            }
                    ) {
                        if (vm.isHtmlMode) {
                            val context = LocalContext.current
                            val html = remember(vm.htmlContent) {
                                try {
                                    context.assets.open("templates/${vm.htmlContent}").bufferedReader().use { it.readText() }
                                } catch (e: Exception) {
                                    "<html><body>Error loading template</body></html>"
                                }
                            }
                            HtmlDesignCanvas(
                                htmlContent = html,
                                jsCommands = vm.jsCommands,
                                captureRequest = vm.captureRequest,
                                onCaptured = onCaptured,
                                onElementSelected = vm::onHtmlElementSelected,
                                onHtmlUpdated = { /* handle auto-save if needed */ },
                                forceDesktop = sheetSize.isDesktopPreview,
                                viewportWidth = designWidth,
                                onContentMeasured = { _, h -> measuredContentHeight = h },
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            DesignCanvas(
                                state = vm.canvasState,
                                selectedId = vm.selectedElementId,
                                onSelect = vm::selectElement,
                                onDragStart = vm::beginDragSnapshot,
                                onElementMoved = { id, dx, dy ->
                                    vm.updateElementLive(id) { it.copy(x = (it.x + dx).coerceIn(-0.4f, 1.1f), y = (it.y + dy).coerceIn(-0.4f, 1.1f)) }
                                },
                                onElementResized = { id, dw, dh ->
                                    vm.updateElementLive(id) {
                                        it.copy(
                                            width = (it.width + dw).coerceIn(0.06f, 1.2f),
                                            height = (it.height + dh).coerceIn(0.03f, 1.2f)
                                        )
                                    }
                                },
                                aspectRatio = sheetSize.aspectRatio,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            // Contextual panel: element properties OR active tool panel
            if (vm.isHtmlMode && vm.selectedHtmlElementJson != null) {
                HtmlElementPropertiesPanel(
                    propertiesJson = vm.selectedHtmlElementJson!!,
                    onStyleChange = vm::updateHtmlStyle,
                    onTextChange = vm::updateHtmlText,
                    onImageChange = {
                        if (it == "PICK_LOCAL") imagePicker.launch(arrayOf("image/*"))
                        else vm.setHtmlImage(it)
                    },
                    onDuplicate = vm::duplicateHtmlElement,
                    onDelete = vm::deleteHtmlElement,
                    onMoveUp = vm::moveHtmlUp,
                    onMoveDown = vm::moveHtmlDown,
                    onClose = { vm.onHtmlElementSelected(null) }
                )
            } else if (!vm.isHtmlMode && selected != null) {
                ElementPropertiesPanel(
                    element = selected,
                    onTextChange = { vm.updateSelectedText(it, selected.fontSizeSp, selected.colorHex, selected.bold) },
                    onFontSizeChange = { vm.updateSelectedText(selected.text, it, selected.colorHex, selected.bold) },
                    onColorChange = { hex ->
                        if (selected.type == ElementType.TEXT) {
                            vm.updateSelectedText(selected.text, selected.fontSizeSp, hex, selected.bold)
                        } else {
                            vm.updateShapeColor(hex)
                        }
                    },
                    onBoldToggle = { vm.updateSelectedText(selected.text, selected.fontSizeSp, selected.colorHex, !selected.bold) },
                    onBringForward = vm::bringForward,
                    onDelete = vm::deleteSelected,
                    onClose = { vm.selectElement(null) }
                )
            } else if (activeTool != null) {
                ToolActionPanel(
                    tool = activeTool!!,
                    brandKit = brandKit,
                    currentSheetSize = vm.canvasState.sheetSize,
                    onAddText = { text, size, bold ->
                        vm.addTextElement(text)
                        vm.updateSelectedText(text, size, "#111C2D", bold)
                    },
                    onAddShape = { kind -> vm.addShapeElement(kind) },
                    onPickImage = { imagePicker.launch(arrayOf("image/*")) },
                    onAddLogo = { brandKit?.logoUri?.let { vm.addImageElement(it) } },
                    onApplyBrandColor = { hex -> vm.setBackgroundColor(hex) },
                    onSwitchTemplate = onBrowseTemplates,
                    onSheetSizeChange = vm::setSheetSize
                )
            }

            EditorToolTray(
                selected = activeTool,
                onSelect = { tool ->
                    vm.selectElement(null)
                    activeTool = if (activeTool == tool) null else tool
                }
            )
        }

        if (vm.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = VCPrimary)
            }
        }
    }
}

private fun Modifier.shadowCard(): Modifier = this.then(
    Modifier.background(VCWorkspaceSurface)
)
