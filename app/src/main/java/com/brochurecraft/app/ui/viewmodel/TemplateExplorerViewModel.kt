package com.brochurecraft.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brochurecraft.app.data.db.entity.TemplateEntity
import com.brochurecraft.app.data.model.SeedData
import com.brochurecraft.app.data.repository.TemplateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

class TemplateExplorerViewModel(private val repo: TemplateRepository) : ViewModel() {

    val categories = listOf("All") + SeedData.businessCategories

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory

    val templates: StateFlow<List<TemplateEntity>> =
        combine(_query, _selectedCategory) { q, c -> q to c }
            .flatMapLatest { (q, c) ->
                when {
                    q.isNotBlank() -> repo.search(q)
                    c == "All" -> repo.observeAll()
                    else -> repo.observeByCategory(c)
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setQuery(q: String) { _query.value = q }
    fun setCategory(c: String) { _selectedCategory.value = c }
}
