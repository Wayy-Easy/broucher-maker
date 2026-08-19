package com.brochurecraft.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brochurecraft.app.ui.theme.*
import com.brochurecraft.app.ui.viewmodel.LambdaViewModelFactory
import com.brochurecraft.app.ui.viewmodel.PremiumViewModel
import com.brochurecraft.app.util.rememberApp

private data class ProFeature(val icon: androidx.compose.ui.graphics.vector.ImageVector, val title: String, val body: String)

private val proFeatures = listOf(
    ProFeature(Icons.Filled.GridView, "Unlimited Premium Templates", "Access our full library of curated designs."),
    ProFeature(Icons.Filled.AutoFixHigh, "One-Click Background Remover", "Instantly isolate subjects from photos."),
    ProFeature(Icons.Filled.HighQuality, "Ultra HD & Print-Ready Export", "Export in CMYK PDF for professional printing."),
    ProFeature(Icons.Filled.VisibilityOff, "No Watermark", "Your designs, completely unbranded.")
)

@Composable
fun PremiumUpgradeScreen(onBack: () -> Unit) {
    val app = rememberApp()
    val vm: PremiumViewModel = viewModel(factory = LambdaViewModelFactory { PremiumViewModel(app.userPreferences) })
    val isPro by vm.isPro.collectAsState()
    var annual by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxSize().background(VCSurface)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = VCOnSurface) }
            Text("BrochureCraft", style = TitleMd, color = VCPrimary, fontWeight = FontWeight.Bold)
            Icon(Icons.Filled.Notifications, contentDescription = null, tint = VCPrimary)
        }

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(64.dp).background(VCPrimaryContainer.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.WorkspacePremium, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(32.dp))
            }
            Spacer(Modifier.height(14.dp))
            Text(
                "Unlock Your Creative Potential",
                style = HeadlineLgMobile,
                color = VCOnSurface,
                fontWeight = FontWeight.ExtraBold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Elevate your designs with BrochureCraft PRO. Get unlimited access to premium assets, advanced tools, and watermark-free exports.",
                style = BodyLg,
                color = VCOnSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier
                    .background(VCSurfaceContainerLow, RoundedCornerShape(24.dp))
                    .padding(4.dp)
            ) {
                ToggleTab("Monthly", !annual) { annual = false }
                ToggleTab("Annual · Save 20%", annual) { annual = true }
            }

            Spacer(Modifier.height(20.dp))
            // Basic plan
            PlanCard(
                title = "Basic",
                price = "$0",
                priceSuffix = "/forever",
                description = "Essential tools to get started.",
                features = listOf(
                    "Access to free templates" to true,
                    "Basic editing tools" to true,
                    "Standard resolution export" to true,
                    "Watermark on exports" to false
                ),
                highlighted = false
            ) {
                OutlinedButton(
                    onClick = {},
                    enabled = false,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) { Text(if (!isPro) "Current Plan" else "Basic", color = VCPrimary) }
            }

            Spacer(Modifier.height(16.dp))
            // Pro plan
            PlanCard(
                title = "PRO",
                price = if (annual) "$12" else "$15",
                priceSuffix = if (annual) "/month, billed annually" else "/month",
                description = "Everything you need for professional brochures.",
                features = proFeatures.map { it.title to true },
                highlighted = true,
                badge = "MOST POPULAR"
            ) {
                Button(
                    onClick = { vm.startFreeTrial(onBack) },
                    colors = ButtonDefaults.buttonColors(containerColor = VCPrimary, contentColor = VCOnPrimary),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text(if (isPro) "You're on PRO" else "Start Free Trial", fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "7 days free, then $144/year. Cancel anytime.",
                    style = BodySm, color = VCOnSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Lock, contentDescription = null, tint = VCOnSurfaceVariant, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    "Secure payment processing. Cancel anytime from your account settings.",
                    style = BodySm, color = VCOnSurfaceVariant
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RowScope.ToggleTab(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(if (selected) VCWorkspaceSurface else Color.Transparent, RoundedCornerShape(20.dp))
            .clickableSimple(onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(label, style = BodySm, color = if (selected) VCPrimary else VCOnSurfaceVariant, fontWeight = FontWeight.SemiBold)
    }
}

private fun Modifier.clickableSimple(onClick: () -> Unit): Modifier =
    this.clickable(onClick = onClick)

@Composable
private fun PlanCard(
    title: String,
    price: String,
    priceSuffix: String,
    description: String,
    features: List<Pair<String, Boolean>>,
    highlighted: Boolean,
    badge: String? = null,
    actions: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(VCWorkspaceSurface, RoundedCornerShape(20.dp))
            .border(if (highlighted) 2.dp else 1.dp, if (highlighted) VCPrimary else VCBorderSubtle, RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        badge?.let {
            Box(
                modifier = Modifier
                    .background(VCPrimary, RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(it, color = VCOnPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(10.dp))
        }
        Text(title, style = TitleMd, color = VCOnSurface, fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(price, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, color = VCOnSurface)
            Spacer(Modifier.width(4.dp))
            Text(priceSuffix, style = BodySm, color = VCOnSurfaceVariant, modifier = Modifier.padding(bottom = 6.dp))
        }
        Text(description, style = BodySm, color = VCOnSurfaceVariant)
        Spacer(Modifier.height(12.dp))
        Divider(color = VCBorderSubtle)
        Spacer(Modifier.height(12.dp))
        features.forEach { (label, included) ->
            Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (included) Icons.Filled.Check else Icons.Filled.Close,
                    contentDescription = null,
                    tint = if (included) VCTertiary else VCOutlineVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(label, style = BodySm, color = if (included) VCOnSurface else VCOutlineVariant)
            }
        }
        Spacer(Modifier.height(14.dp))
        actions()
    }
}
