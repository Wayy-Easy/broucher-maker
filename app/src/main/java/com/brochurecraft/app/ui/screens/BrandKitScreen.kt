package com.brochurecraft.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.brochurecraft.app.data.db.entity.BrandKitEntity
import com.brochurecraft.app.ui.theme.*
import com.brochurecraft.app.ui.viewmodel.BrandKitViewModel
import com.brochurecraft.app.ui.viewmodel.LambdaViewModelFactory
import com.brochurecraft.app.util.rememberApp
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Composable
fun BrandKitScreen() {
    val app = rememberApp()
    val context = LocalContext.current
    val vm: BrandKitViewModel = viewModel(factory = LambdaViewModelFactory { BrandKitViewModel(app.brandKitRepository) })
    val brandKit by vm.brandKit.collectAsState()

    var businessName by remember { mutableStateOf("") }
    var tagline by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var logoUri by remember { mutableStateOf<String?>(null) }
    var colors by remember { mutableStateOf(listOf("#B4132D", "#F5A623", "#111C2D", "#EAF2EC")) }
    var colorLabels by remember { mutableStateOf(listOf("Brand Red", "Accent", "Dark", "Light")) }
    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(brandKit) {
        brandKit?.let {
            if (!initialized) {
                businessName = it.businessName
                tagline = it.tagline
                phone = it.phoneNumber
                whatsapp = it.whatsapp
                address = it.address
                logoUri = it.logoUri
                colors = try { Json.decodeFromString(it.colorsJson) } catch (e: Exception) { colors }
                colorLabels = try { Json.decodeFromString(it.colorLabelsJson) } catch (e: Exception) { colorLabels }
                initialized = true
            }
        }
    }

    val logoPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: Exception) { }
            logoUri = uri.toString()
        }
    }

    fun persist() {
        vm.save(
            BrandKitEntity(
                businessName = businessName,
                tagline = tagline,
                logoUri = logoUri,
                colorsJson = Json.encodeToString(colors),
                colorLabelsJson = Json.encodeToString(colorLabels),
                phoneNumber = phone,
                whatsapp = whatsapp,
                address = address
            )
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(VCSurface)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("BrochureCraft", style = TitleMd, color = VCPrimary, fontWeight = FontWeight.Bold)
            Icon(Icons.Filled.Notifications, contentDescription = null, tint = VCPrimary)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Text("Brand Kit", style = HeadlineLgMobile, color = VCOnSurface, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(6.dp))
            Text(
                "Set up your core brand assets to generate consistent designs instantly.",
                style = BodyLg, color = VCOnSurfaceVariant
            )
            Spacer(Modifier.height(20.dp))

            SectionCard(title = "Restaurant Logo", icon = Icons.Filled.Image) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(VCSurfaceContainerLow, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (logoUri != null) {
                        AsyncImage(model = logoUri, contentDescription = "Logo", modifier = Modifier.fillMaxSize())
                    } else {
                        Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, tint = VCOutline, modifier = Modifier.size(36.dp))
                    }
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { logoPicker.launch(arrayOf("image/*")) },
                    colors = ButtonDefaults.buttonColors(containerColor = VCPrimary, contentColor = VCOnPrimary),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Replace Logo", fontWeight = FontWeight.SemiBold) }
                Spacer(Modifier.height(6.dp))
                Text(
                    "Upload a high-res PNG or SVG with a transparent background.",
                    style = BodySm, color = VCOnSurfaceVariant
                )
            }

            SectionCard(title = "Identity", icon = Icons.Filled.Badge) {
                LabeledField("Business Name", businessName) { businessName = it }
                Spacer(Modifier.height(12.dp))
                LabeledField("Tagline", tagline) { tagline = it }
            }

            SectionCard(title = "Primary Colors", icon = Icons.Filled.Palette) {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    colors.forEachIndexed { i, hex ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color(android.graphics.Color.parseColor(hex)), RoundedCornerShape(10.dp))
                                    .border(1.dp, VCBorderSubtle, RoundedCornerShape(10.dp))
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(colorLabels.getOrElse(i) { "Color" }, style = BodySm, color = VCOnSurfaceVariant)
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text("These colors will be prioritized in your templates.", style = BodySm, color = VCOnSurfaceVariant)
            }

            SectionCard(title = "Contact Information", icon = Icons.Filled.ContactPage) {
                LabeledField("Phone Number", phone, leading = Icons.Filled.Phone) { phone = it }
                Spacer(Modifier.height(12.dp))
                LabeledField("WhatsApp", whatsapp, leading = Icons.Filled.Message) { whatsapp = it }
                Spacer(Modifier.height(12.dp))
                LabeledField("Address", address, leading = Icons.Filled.LocationOn) { address = it }
            }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { persist() },
                colors = ButtonDefaults.buttonColors(containerColor = VCPrimary, contentColor = VCOnPrimary),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Save Brand Kit", fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .background(VCWorkspaceSurface, RoundedCornerShape(16.dp))
            .border(1.dp, VCBorderSubtle, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = VCPrimary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(title, style = TitleMd, color = VCOnSurface, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun LabeledField(
    label: String,
    value: String,
    leading: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onChange: (String) -> Unit
) {
    Column {
        Text(label, style = BodySm, color = VCOnSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            leadingIcon = leading?.let { { Icon(it, contentDescription = null, modifier = Modifier.size(18.dp)) } },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
