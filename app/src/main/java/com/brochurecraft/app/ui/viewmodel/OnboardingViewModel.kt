package com.brochurecraft.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brochurecraft.app.data.prefs.UserPreferences
import kotlinx.coroutines.launch

class OnboardingViewModel(private val prefs: UserPreferences) : ViewModel() {

    fun completeOnboarding(businessType: String, businessName: String, onDone: () -> Unit) {
        viewModelScope.launch {
            prefs.setBusinessType(businessType)
            if (businessName.isNotBlank()) prefs.setBusinessName(businessName)
            prefs.setOnboardingDone(true)
            onDone()
        }
    }
}
