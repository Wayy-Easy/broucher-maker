package com.brochurecraft.app.ui.screens

import android.net.Uri
import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
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

    LaunchedEffect(Unit) {
        vm.exitEvent.collect {
            onBack()
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
                    vm.triggerSaveAndExit()
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
    var zoomScale by remember { mutableStateOf(1f) }
    var zoomOffset by remember { mutableStateOf(Offset.Zero) }

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
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectTransformGestures { centroid, pan, zoom, _ ->
                            val oldScale = zoomScale
                            zoomScale = (zoomScale * zoom).coerceIn(0.5f, 5f)
                            
                            // Zoom around centroid
                            zoomOffset = (zoomOffset + centroid / oldScale) - (centroid / zoomScale + pan / zoomScale)
                        }
                    }
                    .clipToBounds(),
                contentAlignment = Alignment.Center
            ) {
                val sheetSize = vm.canvasState.sheetSize
                val designWidth = sheetSize.previewWidthPx

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth(0.96f)
                        .fillMaxHeight()
                        .padding(vertical = 20.dp)
                        .graphicsLayer {
                            scaleX = zoomScale
                            scaleY = zoomScale
                            translationX = -zoomOffset.x * zoomScale
                            translationY = -zoomOffset.y * zoomScale
                        }
                        .shadowCard(),
                    contentAlignment = Alignment.TopStart
                ) {
                    if (vm.isHtmlMode) {
                        // HTML mode: the WebView itself just fills this box directly - full
                        // stop, no separate scaling layer, no size math on our side at all.
                        // `designWidth` (the CSS breakpoint) is handed to the WebView, which
                        // computes its OWN zoom internally (see setInitialScale in
                        // HtmlDesignCanvas) using its exact real pixel width - it can't
                        // mismatch the way an external guess could. A plain View also can
                        // never paint outside its own laid-out bounds, so this can't bleed
                        // over other UI either.
                        val context = LocalContext.current
                        val html = remember(vm.htmlContent) {
                            try {
                                val content = vm.htmlContent ?: ""
                                if (content.startsWith("<html", ignoreCase = true) || content.startsWith("<!DOCTYPE", ignoreCase = true)) {
                                    content
                                } else {
                                    context.assets.open("templates/$content").bufferedReader().use { it.readText() }
                                }
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
                            onHtmlUpdated = vm::onHtmlContentChanged,
                            onPageFinished = vm::onPageLoaded,
                            forceDesktop = sheetSize.isDesktopPreview,
                            viewportWidth = designWidth,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Non-HTML (raw shape/text) designs keep the literal paper aspect
                        // ratio, since elements are positioned as fractions of that exact
                        // shape - stretching it would relocate/distort every element. This
                        // path still needs its own explicit scale-to-fit box.
                        val scope = this
                        val availableWidthPx = scope.constraints.maxWidth
                        val density = androidx.compose.ui.platform.LocalDensity.current
                        val designWidthPx = with(density) { designWidth.dp.toPx() }
                        val scaleFactor = availableWidthPx.toFloat() / designWidthPx

                        Box(
                            modifier = Modifier
                                .size(width = designWidth.dp, height = (designWidth / sheetSize.aspectRatio).dp)
                                .graphicsLayer {
                                    scaleX = scaleFactor
                                    scaleY = scaleFactor
                                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0f)
                                    clip = true
                                }
                        ) {
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
                    onSheetSizeChange = vm::setSheetSize,
                    onClose = { activeTool = null }
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
