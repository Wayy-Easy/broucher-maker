package com.brochurecraft.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brochurecraft.app.ui.theme.VCOnPrimary
import com.brochurecraft.app.ui.theme.VCPrimary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var animate by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (animate) 1f else 0.7f,
        animationSpec = tween(500),
        label = "splashScale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (animate) 1f else 0f,
        animationSpec = tween(600),
        label = "splashAlpha"
    )

    LaunchedEffect(Unit) {
        animate = true
        delay(1400)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VCPrimary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale; this.alpha = alpha }
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .background(VCOnPrimary, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = VCPrimary,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = "BrochureCraft",
                color = VCOnPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Design. Print. Inspire.",
                color = VCOnPrimary.copy(alpha = 0.85f),
                fontSize = 14.sp
            )
        }
    }
}
