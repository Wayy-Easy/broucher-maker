package com.brochurecraft.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brochurecraft.app.data.prefs.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PremiumViewModel(private val prefs: UserPreferences) : ViewModel() {

    val isPro: StateFlow<Boolean> =
        prefs.isPro.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /** Simulates a local "purchase" - no real billing SDK is wired up, this just
     * flips the locally persisted plan flag so PRO features unlock in the app. */
    fun startFreeTrial(onDone: () -> Unit) {
        viewModelScope.launch {
            prefs.setPro(true)
            onDone()
        }
    }
}
