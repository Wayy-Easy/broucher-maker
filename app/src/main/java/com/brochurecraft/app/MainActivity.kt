package com.brochurecraft.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import com.brochurecraft.app.ui.nav.AppNavGraph
import com.brochurecraft.app.ui.theme.BrochureCraftTheme
import com.brochurecraft.app.ui.theme.VCSurface

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as BrochureCraftApp

        setContent {
            // DataStore reads resolve almost instantly, and the splash screen
            // (shown for ~1.4s regardless) comfortably covers that latency,
            // so a simple non-null default avoids any startup flicker.
            val onboardingDone by app.userPreferences.onboardingDone.collectAsState(initial = false)

            BrochureCraftTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = VCSurface) {
                    AppNavGraph(startAtOnboarding = !onboardingDone)
                }
            }
        }
    }
}
