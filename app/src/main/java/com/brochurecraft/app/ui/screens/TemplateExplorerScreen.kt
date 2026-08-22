package com.brochurecraft.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brochurecraft.app.data.db.entity.TemplateEntity
import com.brochurecraft.app.ui.components.HtmlDesignCanvas
import com.brochurecraft.app.ui.theme.*
import com.brochurecraft.app.ui.viewmodel.LambdaViewModelFactory
import com.brochurecraft.app.ui.viewmodel.TemplateExplorerViewModel
import com.brochurecraft.app.util.rememberApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import androidx.compose.ui.platform.LocalContext

@Composable
fun TemplateExplorerScreen(onOpenTemplate: (Long, String) -> Unit) {
    val app = rememberApp()
    val vm: TemplateExplorerViewModel = viewModel(
        factory = LambdaViewModelFactory { TemplateExplorerViewModel(app.templateRepository) }
    )
    val query by vm.query.collectAsState()
    val selectedCategory by vm.selectedCategory.collectAsState()
    val templates by vm.templates.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(VCSurface)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("BrochureCraft", style = TitleMd, color = VCPrimary, fontWeight = FontWeight.Bold)
        }

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = vm::setQuery,
                placeholder = { Text("Pizza, Burger, Hotel...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                vm.categories.forEach { cat ->
                    val selected = cat == selectedCategory
                    FilterChip(
                        selected = selected,
                        onClick = { vm.setCategory(cat) },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VCPrimaryContainer.copy(alpha = 0.25f),
                            selectedLabelColor = VCPrimary
                        )
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(templates) { t -> TemplateCard(t) { onOpenTemplate(t.id, t.name) } }
        }
    }
}

@Composable
private fun TemplateCard(template: TemplateEntity, onClick: () -> Unit) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    Color(android.graphics.Color.parseColor(template.accentColorHex)).copy(alpha = 0.85f),
                    RoundedCornerShape(16.dp)
                )
                .border(1.dp, VCBorderSubtle, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
        ) {
            if (template.elementsJson.startsWith("html:")) {
                val html = remember(template.elementsJson) {
                    try {
                        val assetName = template.elementsJson.removePrefix("html:")
                        context.assets.open("templates/$assetName").bufferedReader().use { it.readText() }
                    } catch (e: Exception) {
                        null
                    }
                }
                if (html != null) {
                    Box(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                        key(template.id) {
                            BoxWithConstraints(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                                val boxScope = this
                                val availableWidthPx = boxScope.constraints.maxWidth
                                val density = androidx.compose.ui.platform.LocalDensity.current
                                val designWidth = 1000
                                val designWidthPx = with(density) { designWidth.dp.toPx() }
                                val scaleFactor = availableWidthPx.toFloat() / designWidthPx

                                Box(
                                    modifier = Modifier
                                        .size(width = designWidth.dp, height = (designWidth / 0.75f).dp)
                                        .graphicsLayer {
                                            scaleX = scaleFactor
                                            scaleY = scaleFactor
                                            transformOrigin = TransformOrigin(0f, 0f)
                                        }
                                ) {
                                    HtmlDesignCanvas(
                                        htmlContent = html,
                                        jsCommands = remember { MutableStateFlow("").asSharedFlow() },
                                        onElementSelected = {},
                                        onHtmlUpdated = {},
                                        isReadOnly = true,
                                        viewportWidth = designWidth,
                                        modifier = Modifier.fillMaxSize().background(Color.White)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (template.isPro) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(Color(0xFFF59E0B), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.WorkspacePremium, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(3.dp))
                    Text("PRO", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            template.badgeText?.let { badge ->
                if (!template.isPro || badge != "PRO") {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(badge, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // OVERLAY BOX: This is the most important part.
            // It covers the entire card area, intercepting all clicks
            // and preventing them from reaching the WebView.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onClick() }
                    .zIndex(1f)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(template.name, style = BodyLg, color = VCOnSurface, fontWeight = FontWeight.SemiBold)
        Text(template.subtitle, style = BodySm, color = VCOnSurfaceVariant)
    }
}
