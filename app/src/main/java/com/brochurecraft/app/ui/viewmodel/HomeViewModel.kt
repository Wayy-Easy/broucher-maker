package com.brochurecraft.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brochurecraft.app.data.db.entity.DesignEntity
import com.brochurecraft.app.data.db.entity.TemplateEntity
import com.brochurecraft.app.data.prefs.UserPreferences
import com.brochurecraft.app.data.repository.DesignRepository
import com.brochurecraft.app.data.repository.TemplateRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    designRepository: DesignRepository,
    templateRepository: TemplateRepository,
    prefs: UserPreferences
) : ViewModel() {

    val recentDesigns: StateFlow<List<DesignEntity>> =
        designRepository.observeRecent(6)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val featuredTemplates: StateFlow<List<TemplateEntity>> =
        templateRepository.observeFeatured(4)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val businessName: StateFlow<String> =
        prefs.businessName.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "My Business")
}
