package com.brochurecraft.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brochurecraft.app.ui.theme.*
import com.brochurecraft.app.ui.viewmodel.LambdaViewModelFactory
import com.brochurecraft.app.ui.viewmodel.PremiumViewModel
import com.brochurecraft.app.util.rememberApp

@Composable
fun ProfileScreen(onOpenPremium: () -> Unit, onOpenBrandKit: () -> Unit) {
    val app = rememberApp()
    val vm: PremiumViewModel = viewModel(factory = LambdaViewModelFactory { PremiumViewModel(app.userPreferences) })
    val isPro by vm.isPro.collectAsState()
    val businessName by app.userPreferences.businessName.collectAsState(initial = "My Business")
    val businessType by app.userPreferences.businessType.collectAsState(initial = "Restaurant")

    Column(modifier = Modifier.fillMaxSize().background(VCSurface)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Profile", style = TitleMd, color = VCPrimary, fontWeight = FontWeight.Bold)
        }

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(64.dp).background(VCPrimaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = VCOnPrimary, modifier = Modifier.size(32.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(businessName, style = TitleMd, color = VCOnSurface, fontWeight = FontWeight.Bold)
                    Text(businessType, style = BodySm, color = VCOnSurfaceVariant)
                }
            }

            Spacer(Modifier.height(16.dp))
            if (!isPro) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(VCPrimary, RoundedCornerShape(16.dp))
                        .clickable { onOpenPremium() }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.WorkspacePremium, contentDescription = null, tint = VCOnPrimary)
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Upgrade to PRO", style = TitleMd, color = VCOnPrimary, fontWeight = FontWeight.Bold)
                        Text("Unlock premium templates & exports", style = BodySm, color = VCOnPrimary.copy(alpha = 0.85f))
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = VCOnPrimary)
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(VCTertiaryContainer.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Verified, contentDescription = null, tint = VCTertiary)
                    Spacer(Modifier.width(10.dp))
                    Text("You're a PRO member", style = TitleMd, color = VCOnSurface, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(20.dp))
            ProfileMenuItem(Icons.Filled.Palette, "Brand Kit", onOpenBrandKit)
            ProfileMenuItem(Icons.Filled.Notifications, "Notifications") {}
            ProfileMenuItem(Icons.Filled.Language, "Language") {}
            ProfileMenuItem(Icons.Filled.HelpOutline, "Help & Support") {}
            ProfileMenuItem(Icons.Filled.Logout, "Sign Out") {}
        }
    }
}

@Composable
private fun ProfileMenuItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = VCOnSurfaceVariant, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(14.dp))
        Text(label, style = BodyLg, color = VCOnSurface, modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = VCOutlineVariant)
    }
    Divider(color = VCBorderSubtle)
}
