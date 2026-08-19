package com.brochurecraft.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brochurecraft.app.data.db.entity.DesignEntity
import com.brochurecraft.app.data.db.entity.TemplateEntity
import com.brochurecraft.app.ui.theme.*
import com.brochurecraft.app.ui.viewmodel.HomeViewModel
import com.brochurecraft.app.ui.viewmodel.LambdaViewModelFactory
import com.brochurecraft.app.util.rememberApp
import java.text.SimpleDateFormat
import java.util.*

private data class QuickCreate(val label: String, val icon: ImageVector, val color: Color, val category: String)

private val quickCreates = listOf(
    QuickCreate("Restaurant Menu", Icons.Filled.Restaurant, VCPrimaryContainer, "Restaurant"),
    QuickCreate("Food Offer", Icons.Filled.LocalPizza, VCSecondaryContainer, "Offer"),
    QuickCreate("Combo Offer", Icons.Filled.CardGiftcard, VCTertiaryContainer, "Combo"),
    QuickCreate("Hotel Offer", Icons.Filled.Hotel, VCPrimaryContainer, "Hotel"),
)

@Composable
fun HomeDashboardScreen(
    onCreateNew: () -> Unit,
    onOpenDesign: (Long) -> Unit,
    onOpenTemplate: (Long, String) -> Unit,
    onSeeAllTemplates: () -> Unit,
    onOpenProfile: () -> Unit
) {
    val app = rememberApp()
    val vm: HomeViewModel = viewModel(
        factory = LambdaViewModelFactory {
            HomeViewModel(app.designRepository, app.templateRepository, app.userPreferences)
        }
    )
    val recentDesigns by vm.recentDesigns.collectAsState()
    val featured by vm.featuredTemplates.collectAsState()
    val businessName by vm.businessName.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(VCSurface)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(36.dp).background(VCPrimaryContainer, CircleShape).clickable { onOpenProfile() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Person, contentDescription = "Profile", tint = VCOnPrimary, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text("BrochureCraft", style = TitleMd, color = VCPrimary, fontWeight = FontWeight.Bold)
            }
            Icon(Icons.Filled.Notifications, contentDescription = "Notifications", tint = VCPrimary)
        }

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            Text("Good Morning,", style = BodyLg, color = VCOnSurfaceVariant)
            Text(businessName, style = HeadlineLgMobile, color = VCOnSurface, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(VCPrimary, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Text("Ready to inspire?", style = TitleMd, color = VCOnPrimary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Craft stunning menus and offers for your audience.",
                        style = BodySm,
                        color = VCOnPrimary.copy(alpha = 0.9f)
                    )
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = onCreateNew,
                        colors = ButtonDefaults.buttonColors(containerColor = VCOnPrimary, contentColor = VCPrimary),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Create New Design", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("Quick Create", style = TitleMd, color = VCOnSurface, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.heightIn(max = 220.dp)
            ) {
                items(quickCreates) { qc ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(VCWorkspaceSurface, RoundedCornerShape(16.dp))
                            .border(1.dp, VCBorderSubtle, RoundedCornerShape(16.dp))
                            .clickable { onCreateNew() }
                            .padding(16.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(44.dp).background(qc.color.copy(alpha = 0.25f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(qc.icon, contentDescription = null, tint = VCPrimary)
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(qc.label, style = BodySm, color = VCOnSurface, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Featured Templates", style = TitleMd, color = VCOnSurface, fontWeight = FontWeight.Bold)
                Text("See All", style = BodySm, color = VCPrimary, modifier = Modifier.clickable { onSeeAllTemplates() })
            }
            Spacer(Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(featured) { template -> FeaturedTemplateCard(template) { onOpenTemplate(template.id, template.name) } }
            }

            Spacer(Modifier.height(24.dp))
            Text("Recent Designs", style = TitleMd, color = VCOnSurface, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                recentDesigns.forEach { design -> RecentDesignRow(design) { onOpenDesign(design.id) } }
                if (recentDesigns.isEmpty()) {
                    Text("No designs yet. Tap Create New Design to start!", style = BodySm, color = VCOnSurfaceVariant)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FeaturedTemplateCard(template: TemplateEntity, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(180.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(Color(android.graphics.Color.parseColor(template.accentColorHex)).copy(alpha = 0.85f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.BottomStart
        ) {
            template.badgeText?.let {
                Box(
                    modifier = Modifier
                        .padding(10.dp)
                        .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(it, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(template.name, style = BodySm, color = VCOnSurface, fontWeight = FontWeight.SemiBold)
        Text(template.subtitle, style = BodySm, color = VCOnSurfaceVariant)
    }
}

@Composable
private fun RecentDesignRow(design: DesignEntity, onClick: () -> Unit) {
    val df = remember { SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(VCWorkspaceSurface, RoundedCornerShape(14.dp))
            .border(1.dp, VCBorderSubtle, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(48.dp).background(VCSurfaceContainer, RoundedCornerShape(10.dp))
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(design.name, style = BodyLg, color = VCOnSurface, fontWeight = FontWeight.SemiBold)
            Text("Edited ${df.format(Date(design.updatedAt))}", style = BodySm, color = VCOnSurfaceVariant)
        }
        Icon(Icons.Filled.MoreVert, contentDescription = null, tint = VCOnSurfaceVariant)
    }
}
