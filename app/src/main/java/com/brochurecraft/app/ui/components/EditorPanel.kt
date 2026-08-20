package com.brochurecraft.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.brochurecraft.app.data.db.entity.BrandKitEntity
import com.brochurecraft.app.data.model.DesignElement
import com.brochurecraft.app.data.model.ElementType
import com.brochurecraft.app.data.model.HtmlElementProperties
import com.brochurecraft.app.data.model.ShapeKind
import com.brochurecraft.app.ui.theme.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

private val quickColors = listOf("#111C2D", "#4648D4", "#B4136D", "#006C49", "#F59E0B", "#BA1A1A", "#FFFFFF")

@Composable
fun ElementPropertiesPanel(
    element: DesignElement,
    onTextChange: (String) -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onColorChange: (String) -> Unit,
    onBoldToggle: () -> Unit,
    onBringForward: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(VCWorkspaceSurface)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Edit Element", style = TitleMd, color = VCOnSurface)
            Row {
                IconButton(onClick = onBringForward) { Icon(Icons.Filled.FlipToFront, contentDescription = "Bring forward", tint = VCOnSurfaceVariant) }
                IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = VCError) }
            }
        }

        if (element.type == ElementType.TEXT) {
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = element.text,
                onValueChange = onTextChange,
                label = { Text("Text") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
            Spacer(Modifier.height(10.dp))
            Text("Font size: ${element.fontSizeSp.toInt()}sp", style = BodySm, color = VCOnSurfaceVariant)
            Slider(
                value = element.fontSizeSp,
                onValueChange = onFontSizeChange,
                valueRange = 10f..48f,
                colors = SliderDefaults.colors(thumbColor = VCPrimary, activeTrackColor = VCPrimary)
            )
            Spacer(Modifier.height(4.dp))
            AssistChip(
                onClick = onBoldToggle,
                label = { Text(if (element.bold) "Bold: On" else "Bold: Off") }
            )
            Spacer(Modifier.height(10.dp))
        }

        Text("Color", style = BodySm, color = VCOnSurfaceVariant)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            quickColors.forEach { hex ->
                val isActive = (element.type == ElementType.TEXT && element.colorHex == hex) ||
                    (element.type != ElementType.TEXT && element.fillColorHex == hex)
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color(android.graphics.Color.parseColor(hex)), CircleShape)
                        .border(
                            width = if (isActive) 2.dp else 1.dp,
                            color = if (isActive) VCPrimary else VCBorderSubtle,
                            shape = CircleShape
                        )
                        .clickable { onColorChange(hex) }
                )
            }
        }
    }
}

@Composable
fun HtmlElementPropertiesPanel(
    propertiesJson: String,
    onStyleChange: (String, String) -> Unit,
    onTextChange: (String) -> Unit,
    onImageChange: (String) -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    val props = remember(propertiesJson) {
        try {
            Json { ignoreUnknownKeys = true }.decodeFromString<HtmlElementProperties>(propertiesJson)
        } catch (e: Exception) {
            null
        }
    } ?: return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(VCWorkspaceSurface)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Edit ${props.tagName}", style = TitleMd, color = VCOnSurface)
            Row {
                IconButton(onClick = onMoveUp) { Icon(Icons.Filled.ArrowUpward, contentDescription = "Move Up", tint = VCOnSurfaceVariant) }
                IconButton(onClick = onMoveDown) { Icon(Icons.Filled.ArrowDownward, contentDescription = "Move Down", tint = VCOnSurfaceVariant) }
                IconButton(onClick = onDuplicate) { Icon(Icons.Filled.ContentCopy, contentDescription = "Duplicate", tint = VCOnSurfaceVariant) }
                IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = VCError) }
            }
        }

        Spacer(Modifier.height(8.dp))

        if (props.text != null) {
            OutlinedTextField(
                value = props.text,
                onValueChange = onTextChange,
                label = { Text("Content") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
            Spacer(Modifier.height(12.dp))
        }

        if (props.src != null) {
            Text("Image source", style = BodySm, color = VCOnSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onImageChange("PICK_LOCAL") },
                    colors = ButtonDefaults.buttonColors(containerColor = VCPrimary),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Upload")
                }
                
                OutlinedButton(
                    onClick = { /* Could open a dialog for URL input */ },
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Paste URL")
                }
            }
            Spacer(Modifier.height(12.dp))
            
            Text("Object Fit", style = BodySm, color = VCOnSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = { onStyleChange("objectFit", "cover") }, label = { Text("Cover") })
                AssistChip(onClick = { onStyleChange("objectFit", "contain") }, label = { Text("Contain") })
            }
            Spacer(Modifier.height(12.dp))
        }

        Text("Colors", style = BodySm, color = VCOnSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
             val quickColors = listOf("#111C2D", "#4648D4", "#B4136D", "#006C49", "#F59E0B", "#BA1A1A", "#FFFFFF")
            quickColors.forEach { hex ->
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color(android.graphics.Color.parseColor(hex)), CircleShape)
                        .border(1.dp, VCBorderSubtle, CircleShape)
                        .clickable { 
                            if (props.tagName == "IMG" || (props.backgroundColor != null && props.backgroundColor != "rgba(0, 0, 0, 0)" && props.backgroundColor != "#00000000")) {
                                onStyleChange("backgroundColor", hex)
                            } else {
                                onStyleChange("color", hex)
                            }
                        }
                )
            }
        }
        
        Spacer(Modifier.height(12.dp))
        Text("Typography", style = BodySm, color = VCOnSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
             AssistChip(
                 onClick = { onStyleChange("fontWeight", if (props.fontWeight == "bold" || props.fontWeight == "700") "normal" else "bold") },
                 label = { Text("Bold") }
             )
             AssistChip(
                 onClick = { onStyleChange("fontStyle", if (props.fontSize == "italic") "normal" else "italic") },
                 label = { Text("Italic") }
             )
        }
    }
}

@Composable
fun ToolActionPanel(
    tool: EditorTool,
    brandKit: BrandKitEntity?,
    onAddText: (String, Float, Boolean) -> Unit,
    onAddShape: (ShapeKind) -> Unit,
    onPickImage: () -> Unit,
    onAddLogo: () -> Unit,
    onApplyBrandColor: (String) -> Unit,
    onSwitchTemplate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(VCWorkspaceSurface)
            .padding(14.dp)
    ) {
        when (tool) {
            EditorTool.TEXT -> {
                Text("Add Text", style = TitleMd, color = VCOnSurface)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TextStylePreset("Heading") { onAddText("Add a heading", 28f, true) }
                    TextStylePreset("Subheading") { onAddText("Add a subheading", 20f, true) }
                    TextStylePreset("Body text") { onAddText("Add body text", 15f, false) }
                }
            }
            EditorTool.IMAGES -> {
                Text("Images", style = TitleMd, color = VCOnSurface)
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = onPickImage,
                    colors = ButtonDefaults.buttonColors(containerColor = VCPrimary, contentColor = VCOnPrimary),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Choose from gallery")
                }
            }
            EditorTool.ELEMENTS -> {
                Text("Shapes", style = TitleMd, color = VCOnSurface)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    ShapeButton(Icons.Filled.Rectangle, "Rectangle") { onAddShape(ShapeKind.RECTANGLE) }
                    ShapeButton(Icons.Filled.Circle, "Circle") { onAddShape(ShapeKind.CIRCLE) }
                    ShapeButton(Icons.Filled.HorizontalRule, "Line") { onAddShape(ShapeKind.LINE) }
                }
            }
            EditorTool.BRAND -> {
                Text("Brand Kit", style = TitleMd, color = VCOnSurface)
                Spacer(Modifier.height(10.dp))
                if (brandKit?.logoUri != null) {
                    OutlinedButton(onClick = onAddLogo, shape = RoundedCornerShape(24.dp)) {
                        Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Insert Logo")
                    }
                    Spacer(Modifier.height(10.dp))
                }
                Text("Brand colors", style = BodySm, color = VCOnSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                val colors = try {
                    Json.decodeFromString<List<String>>(brandKit?.colorsJson ?: "[]")
                } catch (e: Exception) { emptyList() }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    colors.forEach { hex ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color(android.graphics.Color.parseColor(hex)), CircleShape)
                                .border(1.dp, VCBorderSubtle, CircleShape)
                                .clickable { onApplyBrandColor(hex) }
                        )
                    }
                }
            }
            EditorTool.TEMPLATES -> {
                Text("Templates", style = TitleMd, color = VCOnSurface)
                Spacer(Modifier.height(10.dp))
                OutlinedButton(onClick = onSwitchTemplate, shape = RoundedCornerShape(24.dp)) {
                    Text("Browse template gallery")
                }
            }
        }
    }
}

@Composable
private fun TextStylePreset(label: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, shape = RoundedCornerShape(20.dp)) {
        Text(label)
    }
}

@Composable
private fun ShapeButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Box(
            modifier = Modifier.size(48.dp).background(VCSurfaceContainerLow, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = VCPrimary)
        }
        Spacer(Modifier.height(4.dp))
        Text(label, style = BodySm, color = VCOnSurfaceVariant)
    }
}
