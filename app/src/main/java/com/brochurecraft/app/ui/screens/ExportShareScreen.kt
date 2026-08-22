package com.brochurecraft.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brochurecraft.app.ui.components.DesignCanvas
import com.brochurecraft.app.ui.components.HtmlDesignCanvas
import com.brochurecraft.app.ui.theme.*
import com.brochurecraft.app.ui.viewmodel.ExportViewModel
import com.brochurecraft.app.ui.viewmodel.LambdaViewModelFactory
import com.brochurecraft.app.util.ExportFormat
import com.brochurecraft.app.util.rememberApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow

@Composable
fun ExportShareScreen(designId: Long, onBack: () -> Unit) {
    val app = rememberApp()
    val context = LocalContext.current
    val vm: ExportViewModel = viewModel(factory = LambdaViewModelFactory { ExportViewModel(app.designRepository) })
    var zoomScale by remember { mutableStateOf(1f) }

    LaunchedEffect(designId) { vm.load(designId) }

    LaunchedEffect(vm.capturedBitmap) {
        if (vm.capturedBitmap != null) {
            val file = vm.export(context)
            context.startActivity(vm.shareIntent(context, file))
            vm.capturedBitmap = null
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(VCSurface)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = VCOnSurface) }

            Text("Export & Share", style = TitleMd, color = VCOnSurface, fontWeight = FontWeight.Bold)
        }
        Divider(color = VCBorderSubtle)

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp)) {
            // Optimization: Fixed height and scrollable preview area.
            // This prevents the screen from jumping when changing sheet sizes (A4 -> A3).
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp) // Fixed height for visual consistency
                    .background(VCWorkspaceSurface, RoundedCornerShape(20.dp))
                    .border(1.dp, VCBorderSubtle, RoundedCornerShape(20.dp))
                    .pointerInput(Unit) {
                        detectTransformGestures { _, _, zoom, _ ->
                            zoomScale = (zoomScale * zoom).coerceIn(0.5f, 5f)
                        }
                    }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                val sheetSize = vm.canvasState.sheetSize
                val designWidth = sheetSize.previewWidthPx

                // Inner box handles the actual scroll if the preview is larger than the fixed container
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .horizontalScroll(rememberScrollState())
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = designWidth.dp, height = (designWidth / sheetSize.aspectRatio).dp)
                            .align(Alignment.Center)
                            .graphicsLayer {
                                scaleX = zoomScale
                                scaleY = zoomScale
                            }
                    ) {
                        if (vm.isHtmlMode) {
                            // Use the saved HTML content from the design if available,
                            // otherwise fallback to the template asset.
                            val html = remember(vm.htmlContent) {
                                try {
                                    val content = vm.htmlContent ?: ""
                                    if (content.startsWith("<html", ignoreCase = true) || content.startsWith("<!DOCTYPE", ignoreCase = true)) {
                                        content
                                    } else {
                                        context.assets.open("templates/$content").bufferedReader().use { it.readText() }
                                    }
                                } catch (e: Exception) {
                                    "<html><body>Error loading design content</body></html>"
                                }
                            }
                            HtmlDesignCanvas(
                                htmlContent = html,
                                jsCommands = MutableStateFlow("").asSharedFlow(),
                                captureRequest = vm.captureRequest,
                                onCaptured = { vm.capturedBitmap = it },
                                onElementSelected = {},
                                onHtmlUpdated = {},
                                forceDesktop = sheetSize.isDesktopPreview,
                                viewportWidth = designWidth,
                                modifier = Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color.White)
                            )
                        } else {
                            DesignCanvas(
                                state = vm.canvasState,
                                selectedId = null,
                                onSelect = {},
                                onDragStart = {},
                                onElementMoved = { _, _, _ -> },
                                onElementResized = { _, _, _ -> },
                                aspectRatio = sheetSize.aspectRatio,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("Sheet Size", style = TitleMd, color = VCOnSurface, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                com.brochurecraft.app.data.model.SheetSize.values().forEach { size ->
                    val selected = size == vm.canvasState.sheetSize
                    FilterChip(
                        selected = selected,
                        onClick = { vm.setSheetSize(size) },
                        label = { Text(size.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VCPrimary.copy(alpha = 0.1f),
                            selectedLabelColor = VCPrimary
                        )
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("Format", style = TitleMd, color = VCOnSurface, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FormatOption("PDF", Icons.Filled.PictureAsPdf, vm.format == ExportFormat.PDF) { vm.format = ExportFormat.PDF }
                FormatOption("PNG", Icons.Filled.Image, vm.format == ExportFormat.PNG) { vm.format = ExportFormat.PNG }
                FormatOption("JPG", Icons.Filled.Image, vm.format == ExportFormat.JPG) { vm.format = ExportFormat.JPG }
            }

            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Quality", style = TitleMd, color = VCOnSurface, fontWeight = FontWeight.Bold)
                AssistChip(onClick = {}, label = { Text(vm.qualityLabel()) })
            }
            Slider(
                value = vm.qualityPercent,
                onValueChange = { vm.qualityPercent = it },
                colors = SliderDefaults.colors(thumbColor = VCPrimary, activeTrackColor = VCPrimary)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Web", style = BodySm, color = VCOnSurfaceVariant)
                Text("Standard", style = BodySm, color = VCOnSurfaceVariant)
                Text("Print", style = BodySm, color = VCOnSurfaceVariant)
            }

            Spacer(Modifier.height(10.dp))
            Divider(color = VCBorderSubtle)
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Include Bleed & Crop Marks", style = BodyLg, color = VCOnSurface)
                Switch(
                    checked = vm.includeBleed,
                    onCheckedChange = { vm.includeBleed = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = VCPrimary)
                )
            }

            Spacer(Modifier.height(10.dp))
            Button(
                onClick = {
                    if (vm.isHtmlMode) {
                        vm.requestCapture()
                    } else {
                        val file = vm.export(context)
                        context.startActivity(vm.shareIntent(context, file))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VCPrimary, contentColor = VCOnPrimary),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Icon(Icons.Filled.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Export Design", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }

            Spacer(Modifier.height(20.dp))
            Text("QUICK SHARE", style = LabelCaps, color = VCOnSurfaceVariant, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Spacer(Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                QuickShareIcon(Icons.Filled.Message) {
                    if (vm.isHtmlMode) vm.requestCapture()
                    else {
                        val file = vm.export(context)
                        context.startActivity(vm.shareIntent(context, file))
                    }
                }
                QuickShareIcon(Icons.Filled.CameraAlt) {
                    if (vm.isHtmlMode) vm.requestCapture()
                    else {
                        val file = vm.export(context)
                        context.startActivity(vm.shareIntent(context, file))
                    }
                }
                QuickShareIcon(Icons.Filled.Email) {
                    if (vm.isHtmlMode) vm.requestCapture()
                    else {
                        val file = vm.export(context)
                        context.startActivity(vm.shareIntent(context, file))
                    }
                }
                QuickShareIcon(Icons.Filled.Link) {
                    if (vm.isHtmlMode) vm.requestCapture()
                    else {
                        val file = vm.export(context)
                        context.startActivity(vm.shareIntent(context, file))
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RowScope.FormatOption(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, selected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .weight(1f)
            .background(if (selected) VCPrimaryContainer.copy(alpha = 0.15f) else VCWorkspaceSurface, RoundedCornerShape(14.dp))
            .border(if (selected) 2.dp else 1.dp, if (selected) VCPrimary else VCBorderSubtle, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(vertical = 14.dp)
    ) {
        Icon(icon, contentDescription = label, tint = if (selected) VCPrimary else VCOnSurfaceVariant)
        Spacer(Modifier.height(6.dp))
        Text(label, style = BodySm, color = if (selected) VCPrimary else VCOnSurfaceVariant, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun QuickShareIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(VCWorkspaceSurface, CircleShape)
            .border(1.dp, VCBorderSubtle, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = VCOnSurfaceVariant)
    }
}
