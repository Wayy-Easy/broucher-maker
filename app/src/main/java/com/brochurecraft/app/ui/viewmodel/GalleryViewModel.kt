package com.brochurecraft.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brochurecraft.app.data.db.entity.DesignEntity
import com.brochurecraft.app.data.repository.DesignRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class GalleryTab { ALL, RECENT, FAVORITES }

class GalleryViewModel(private val repo: DesignRepository) : ViewModel() {

    private val _tab = MutableStateFlow(GalleryTab.ALL)
    val tab: StateFlow<GalleryTab> = _tab

    val designs: StateFlow<List<DesignEntity>> =
        _tab.flatMapLatest { t ->
            when (t) {
                GalleryTab.ALL -> repo.observeAll()
                GalleryTab.RECENT -> repo.observeRecent(20)
                GalleryTab.FAVORITES -> repo.observeFavorites()
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setTab(t: GalleryTab) { _tab.value = t }

    fun toggleFavorite(design: DesignEntity) {
        viewModelScope.launch { repo.setFavorite(design.id, !design.isFavorite) }
    }

    fun deleteDesign(id: Long) {
        viewModelScope.launch { repo.delete(id) }
    }
}
