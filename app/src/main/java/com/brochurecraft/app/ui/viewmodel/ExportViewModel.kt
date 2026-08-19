package com.brochurecraft.app.ui.viewmodel

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brochurecraft.app.data.db.entity.DesignEntity
import com.brochurecraft.app.data.model.DesignCanvasState
import com.brochurecraft.app.data.model.DesignJson
import com.brochurecraft.app.data.repository.DesignRepository
import com.brochurecraft.app.util.ExportFormat
import com.brochurecraft.app.util.ExportManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class ExportViewModel(private val repo: DesignRepository) : ViewModel() {

    var design by mutableStateOf<DesignEntity?>(null)
        private set
    var canvasState by mutableStateOf(DesignCanvasState())
        private set

    var format by mutableStateOf(ExportFormat.PDF)
    var qualityPercent by mutableStateOf(1f) // 0f=Web,0.5f=Standard,1f=Print(300dpi)
    var includeBleed by mutableStateOf(true)

    private val _exportedFile = MutableStateFlow<File?>(null)
    val exportedFile: StateFlow<File?> = _exportedFile

    fun load(id: Long) {
        viewModelScope.launch {
            val entity = repo.getById(id)
            design = entity
            canvasState = DesignJson.decode(entity?.elementsJson)
        }
    }

    fun qualityLabel(): String = when {
        qualityPercent < 0.34f -> "Web (72dpi)"
        qualityPercent < 0.67f -> "Standard (150dpi)"
        else -> "High (300dpi)"
    }

    fun export(context: Context): File {
        val dims = if (qualityPercent < 0.34f) 540 to 756 else if (qualityPercent < 0.67f) 800 to 1120 else 1080 to 1512
        val file = ExportManager.export(
            context = context,
            state = canvasState,
            fileBaseName = design?.name ?: "brochurecraft_design",
            format = format,
            widthPx = dims.first,
            heightPx = dims.second
        )
        _exportedFile.value = file
        return file
    }

    fun shareIntent(context: Context, file: File): Intent = ExportManager.shareIntent(context, file)
}
