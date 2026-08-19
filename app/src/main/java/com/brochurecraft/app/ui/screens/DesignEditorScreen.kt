package com.brochurecraft.app.ui.screens

import android.net.Uri
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brochurecraft.app.data.model.ElementType
import com.brochurecraft.app.ui.components.DesignCanvas
import com.brochurecraft.app.ui.components.EditorTool
import com.brochurecraft.app.ui.components.EditorToolTray
import com.brochurecraft.app.ui.components.ElementPropertiesPanel
import com.brochurecraft.app.ui.components.ToolActionPanel
import com.brochurecraft.app.ui.theme.*
import com.brochurecraft.app.ui.viewmodel.EditorViewModel
import com.brochurecraft.app.ui.viewmodel.LambdaViewModelFactory
import com.brochurecraft.app.util.ExportManager
import com.brochurecraft.app.util.rememberApp

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
    val vm: EditorViewModel = viewModel(
        factory = LambdaViewModelFactory { EditorViewModel(app.designRepository, app.templateRepository) }
    )
    val loaded by vm.loaded.collectAsState()
    val brandKit by app.brandKitRepository.observe().collectAsState(initial = null)

    LaunchedEffect(designId, templateId) {
        when {
            designId != null && designId > 0 -> vm.loadDesign(designId)
            templateId != null && templateId > 0 -> vm.loadFromTemplate(templateId, initialName)
            else -> vm.startBlank(initialName)
        }
    }

    var activeTool by remember { mutableStateOf<EditorTool?>(null) }
    var zoom by remember { mutableStateOf(1f) }

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

    Column(modifier = Modifier.fillMaxSize().background(VCCanvasBg)) {
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
                IconButton(onClick = onBack) { Icon(Icons.Filled.Close, contentDescription = "Close", tint = VCOnSurfaceVariant) }
                IconButton(onClick = vm::undo) { Icon(Icons.Filled.Undo, contentDescription = "Undo", tint = VCOnSurfaceVariant) }
                IconButton(onClick = vm::redo) { Icon(Icons.Filled.Redo, contentDescription = "Redo", tint = VCOnSurfaceVariant) }
            }
            Text("Edit Design", style = TitleMd, color = VCOnSurface, fontWeight = FontWeight.Bold)
            Button(
                onClick = {
                    val thumb = ExportManager.thumbnailFile(context, vm.designId ?: 0L, vm.canvasState)
                    vm.save(thumb)
                    val id = vm.designId
                    if (id != null) onExport(id)
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
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                focusedBorderColor = VCPrimary
            )
        )

        // Canvas area (scrollable + zoomable)
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.86f)
                    .graphicsLayerScale(zoom)
                    .shadowCard()
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
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Column(
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ZoomButton(Icons.Filled.ZoomIn) { zoom = (zoom + 0.1f).coerceAtMost(2.5f) }
                Spacer(Modifier.height(8.dp))
                ZoomButton(Icons.Filled.ZoomOut) { zoom = (zoom - 0.1f).coerceAtLeast(0.5f) }
            }
        }

        // Contextual panel: element properties OR active tool panel
        if (selected != null) {
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
                onDelete = vm::deleteSelected
            )
        } else if (activeTool != null) {
            ToolActionPanel(
                tool = activeTool!!,
                brandKit = brandKit,
                onAddText = { text, size, bold ->
                    vm.addTextElement(text)
                    vm.updateSelectedText(text, size, "#111C2D", bold)
                },
                onAddShape = { kind -> vm.addShapeElement(kind) },
                onPickImage = { imagePicker.launch(arrayOf("image/*")) },
                onAddLogo = { brandKit?.logoUri?.let { vm.addImageElement(it) } },
                onApplyBrandColor = { hex -> vm.setBackgroundColor(hex) },
                onSwitchTemplate = onBrowseTemplates
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
}

@Composable
private fun ZoomButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .background(VCWorkspaceSurface, CircleShape)
            .then(Modifier),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick) {
            Icon(icon, contentDescription = null, tint = VCOnSurfaceVariant)
        }
    }
}

private fun Modifier.graphicsLayerScale(scale: Float): Modifier = this.then(
    Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
)

private fun Modifier.shadowCard(): Modifier = this.then(
    Modifier.background(VCWorkspaceSurface)
)
