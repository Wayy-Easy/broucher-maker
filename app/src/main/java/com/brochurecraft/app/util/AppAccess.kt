package com.brochurecraft.app.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.brochurecraft.app.BrochureCraftApp

@Composable
fun rememberApp(): BrochureCraftApp {
    val context = LocalContext.current.applicationContext
    return context as BrochureCraftApp
}
