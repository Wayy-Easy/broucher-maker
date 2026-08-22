package com.brochurecraft.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brochurecraft.app.ui.theme.VCBorderSubtle
import com.brochurecraft.app.ui.theme.VCOnSurfaceVariant
import com.brochurecraft.app.ui.theme.VCPrimary
import com.brochurecraft.app.ui.theme.VCPrimaryContainer
import com.brochurecraft.app.ui.theme.VCWorkspaceSurface

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState

enum class EditorTool(val label: String, val icon: ImageVector) {
    LAYOUT("Layout", Icons.Filled.AspectRatio),
    TEXT("Text", Icons.Filled.TextFields),
    IMAGES("Images", Icons.Filled.Image),
    ELEMENTS("Elements", Icons.Filled.Category),
    BRAND("Brand", Icons.Filled.AutoAwesome)
}

@Composable
fun EditorToolTray(selected: EditorTool?, onSelect: (EditorTool) -> Unit) {
    Column(modifier = Modifier.background(VCWorkspaceSurface)) {
        Divider(color = VCBorderSubtle)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EditorTool.entries.forEach { tool ->
                val isSelected = tool == selected
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onSelect(tool) }
                        .background(
                            if (isSelected) VCPrimaryContainer.copy(alpha = 0.18f) else androidx.compose.ui.graphics.Color.Transparent,
                            androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        tool.icon,
                        contentDescription = tool.label,
                        tint = if (isSelected) VCPrimary else VCOnSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        tool.label,
                        fontSize = 11.sp,
                        color = if (isSelected) VCPrimary else VCOnSurfaceVariant,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
