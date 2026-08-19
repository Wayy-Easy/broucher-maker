package com.brochurecraft.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brochurecraft.app.data.db.entity.BrandKitEntity
import com.brochurecraft.app.data.repository.BrandKitRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BrandKitViewModel(private val repo: BrandKitRepository) : ViewModel() {

    val brandKit: StateFlow<BrandKitEntity?> =
        repo.observe().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun save(brandKit: BrandKitEntity) {
        viewModelScope.launch { repo.save(brandKit) }
    }
}
