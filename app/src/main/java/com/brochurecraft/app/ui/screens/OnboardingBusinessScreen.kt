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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brochurecraft.app.data.model.SeedData
import com.brochurecraft.app.ui.theme.*
import com.brochurecraft.app.ui.viewmodel.LambdaViewModelFactory
import com.brochurecraft.app.ui.viewmodel.OnboardingViewModel
import com.brochurecraft.app.util.rememberApp

private fun iconFor(category: String): ImageVector = when (category) {
    "Restaurant" -> Icons.Filled.Restaurant
    "Cafe" -> Icons.Filled.Coffee
    "Hotel" -> Icons.Filled.Hotel
    "Bakery" -> Icons.Filled.BakeryDining
    "Cloud Kitchen" -> Icons.Filled.Cloud
    else -> Icons.Filled.RoomService
}

@Composable
fun OnboardingBusinessScreen(onDone: () -> Unit) {
    val app = rememberApp()
    val vm: OnboardingViewModel = viewModel(
        factory = LambdaViewModelFactory { OnboardingViewModel(app.userPreferences) }
    )
    var selected by remember { mutableStateOf("Restaurant") }

    Column(modifier = Modifier.fillMaxSize().background(VCSurface)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = VCPrimary)
                Spacer(Modifier.width(6.dp))
                Text("BrochureCraft", style = TitleMd, color = VCPrimary, fontWeight = FontWeight.Bold)
            }
            Text("Step 3 of 3", style = BodySm, color = VCOnSurfaceVariant)
        }
        Divider(color = VCBorderSubtle)

        Column(modifier = Modifier.weight(1f).padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(16.dp))
            Text(
                "What type of business do you have?",
                style = HeadlineLgMobile.copy(fontSize = 30.sp, lineHeight = 36.sp),
                color = VCOnSurface,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "Select your primary focus so we can personalize your template gallery.",
                style = BodyLg,
                color = VCOnSurfaceVariant
            )
            Spacer(Modifier.height(20.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(SeedData.businessCategories) { category ->
                    val isSelected = category == selected
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .background(VCWorkspaceSurface, RoundedCornerShape(16.dp))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) VCPrimary else VCBorderSubtle,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { selected = category }
                            .padding(12.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(VCSurfaceContainerLow, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(iconFor(category), contentDescription = null, tint = VCOnSurface)
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(category, style = TitleMd, color = VCOnSurface)
                    }
                }
            }
        }

        Column(modifier = Modifier.padding(20.dp)) {
            Divider(color = VCPrimary, thickness = 2.dp, modifier = Modifier.padding(bottom = 16.dp))
            Button(
                onClick = {
                    vm.completeOnboarding(businessType = selected, businessName = "") { onDone() }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VCPrimary, contentColor = VCOnPrimary),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("Get Started", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }
}
