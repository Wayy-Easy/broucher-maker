package com.brochurecraft.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brochurecraft.app.ui.nav.BottomDestination
import com.brochurecraft.app.ui.theme.VCBorderSubtle
import com.brochurecraft.app.ui.theme.VCOnSurfaceVariant
import com.brochurecraft.app.ui.theme.VCPrimary
import com.brochurecraft.app.ui.theme.VCWorkspaceSurface

private fun iconFor(dest: BottomDestination, selected: Boolean): ImageVector = when (dest) {
    BottomDestination.HOME -> if (selected) Icons.Filled.Home else Icons.Outlined.Home
    BottomDestination.TEMPLATES -> if (selected) Icons.Filled.GridView else Icons.Outlined.GridView
    BottomDestination.MY_DESIGNS -> if (selected) Icons.Filled.Folder else Icons.Outlined.FolderOpen
    BottomDestination.BRAND -> if (selected) Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome
    BottomDestination.PROFILE -> if (selected) Icons.Filled.Person else Icons.Outlined.Person
}

@Composable
fun BrochureCraftBottomBar(
    current: BottomDestination,
    onSelect: (BottomDestination) -> Unit
) {
    Column(modifier = Modifier.background(VCWorkspaceSurface.copy(alpha = 0.97f))) {
        Divider(color = VCBorderSubtle, thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomDestination.values().forEach { dest ->
                val selected = dest == current
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onSelect(dest) }
                        .padding(vertical = 4.dp, horizontal = 6.dp)
                ) {
                    Icon(
                        imageVector = iconFor(dest, selected),
                        contentDescription = dest.label,
                        tint = if (selected) VCPrimary else VCOnSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = dest.label,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) VCPrimary else VCOnSurfaceVariant
                    )
                }
            }
        }
    }
}
