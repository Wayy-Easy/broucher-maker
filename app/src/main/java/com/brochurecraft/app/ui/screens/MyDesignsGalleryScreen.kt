package com.brochurecraft.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brochurecraft.app.data.db.entity.DesignEntity
import com.brochurecraft.app.ui.theme.*
import com.brochurecraft.app.ui.viewmodel.GalleryTab
import com.brochurecraft.app.ui.viewmodel.GalleryViewModel
import com.brochurecraft.app.ui.viewmodel.LambdaViewModelFactory
import com.brochurecraft.app.util.rememberApp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MyDesignsGalleryScreen(onOpenDesign: (Long) -> Unit, onCreateNew: () -> Unit) {
    val app = rememberApp()
    val vm: GalleryViewModel = viewModel(factory = LambdaViewModelFactory { GalleryViewModel(app.designRepository) })
    val tab by vm.tab.collectAsState()
    val designs by vm.designs.collectAsState()
    var pendingDelete by remember { mutableStateOf<DesignEntity?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(VCSurface)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("BrochureCraft", style = TitleMd, color = VCPrimary, fontWeight = FontWeight.Bold)
                Icon(Icons.Filled.Notifications, contentDescription = null, tint = VCPrimary)
            }

            Row(
                modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GalleryTabChip("All Designs", tab == GalleryTab.ALL) { vm.setTab(GalleryTab.ALL) }
                GalleryTabChip("Recent", tab == GalleryTab.RECENT) { vm.setTab(GalleryTab.RECENT) }
                GalleryTabChip("Favorites", tab == GalleryTab.FAVORITES, icon = Icons.Filled.Favorite) { vm.setTab(GalleryTab.FAVORITES) }
            }

            if (designs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No designs here yet.", style = BodyLg, color = VCOnSurfaceVariant)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(20.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(designs, key = { it.id }) { design ->
                        DesignCard(
                            design = design,
                            onClick = { onOpenDesign(design.id) },
                            onToggleFavorite = { vm.toggleFavorite(design) },
                            onDelete = { pendingDelete = design }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onCreateNew,
            containerColor = VCPrimary,
            contentColor = VCOnPrimary,
            shape = CircleShape,
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "New design")
        }
    }

    pendingDelete?.let { design ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete \"${design.name}\"?") },
            text = { Text("This design will be permanently removed from your device.") },
            confirmButton = {
                TextButton(onClick = { vm.deleteDesign(design.id); pendingDelete = null }) {
                    Text("Delete", color = VCError)
                }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun GalleryTabChip(
    label: String,
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = icon?.let { { Icon(it, contentDescription = null, modifier = Modifier.size(16.dp)) } },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = VCPrimary,
            selectedLabelColor = VCOnPrimary,
            selectedLeadingIconColor = VCOnPrimary
        )
    )
}

@Composable
private fun DesignCard(
    design: DesignEntity,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    val df = remember { SimpleDateFormat("MMM d", Locale.getDefault()) }
    var menuOpen by remember { mutableStateOf(false) }

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(VCSurfaceContainer, RoundedCornerShape(16.dp))
                .border(1.dp, VCBorderSubtle, RoundedCornerShape(16.dp))
                .clickable { onClick() }
        ) {
            Icon(
                Icons.Filled.Description,
                contentDescription = null,
                tint = VCOutlineVariant,
                modifier = Modifier.align(Alignment.Center).size(36.dp)
            )
            Icon(
                imageVector = if (design.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = "Favorite",
                tint = if (design.isFavorite) Color(0xFFF59E0B) else VCOnSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clickable { onToggleFavorite() }
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(design.name, style = BodyLg, color = VCOnSurface, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text("Edited ${df.format(Date(design.updatedAt))}", style = BodySm, color = VCOnSurfaceVariant)
            }
            Box {
                Icon(
                    Icons.Filled.MoreVert,
                    contentDescription = "More",
                    tint = VCOnSurfaceVariant,
                    modifier = Modifier.clickable { menuOpen = true }
                )
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(text = { Text("Open") }, onClick = { menuOpen = false; onClick() })
                    DropdownMenuItem(
                        text = { Text(if (design.isFavorite) "Unfavorite" else "Favorite") },
                        onClick = { menuOpen = false; onToggleFavorite() }
                    )
                    DropdownMenuItem(text = { Text("Delete") }, onClick = { menuOpen = false; onDelete() })
                }
            }
        }
    }
}
