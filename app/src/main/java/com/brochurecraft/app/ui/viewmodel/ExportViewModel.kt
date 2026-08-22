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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.File

class ExportViewModel(private val repo: DesignRepository) : ViewModel() {

    var design by mutableStateOf<DesignEntity?>(null)
        private set
    var canvasState by mutableStateOf(DesignCanvasState())
        private set

    var isHtmlMode by mutableStateOf(false)
        private set
    var htmlContent by mutableStateOf<String?>(null)
        private set

    private val _captureRequest = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val captureRequest: SharedFlow<Unit> = _captureRequest.asSharedFlow()

    var capturedBitmap by mutableStateOf<android.graphics.Bitmap?>(null)

    var format by mutableStateOf(ExportFormat.PDF)
    var qualityPercent by mutableStateOf(1f) // 0f=Web,0.5f=Standard,1f=Print(300dpi)
    var includeBleed by mutableStateOf(true)

    private val _exportedFile = MutableStateFlow<File?>(null)
    val exportedFile: StateFlow<File?> = _exportedFile

    fun load(id: Long) {
        viewModelScope.launch {
            val entity = repo.getById(id)
            design = entity
            val json = entity?.elementsJson
            if (json != null) {
                if (json.startsWith("html:")) {
                    // Support legacy format
                    isHtmlMode = true
                    val assetName = json.removePrefix("html:")
                    htmlContent = assetName
                    canvasState = DesignCanvasState(htmlContent = assetName)
                } else {
                    val state = DesignJson.decode(json)
                    canvasState = state
                    isHtmlMode = state.htmlContent != null
                    htmlContent = state.htmlContent
                }
            }
        }
    }

    fun setSheetSize(size: com.brochurecraft.app.data.model.SheetSize) {
        canvasState = canvasState.copy(sheetSize = size)
    }

    fun qualityLabel(): String = when {
        qualityPercent < 0.34f -> "Web (72dpi)"
        qualityPercent < 0.67f -> "Standard (150dpi)"
        else -> "High (300dpi)"
    }

    fun requestCapture() {
        viewModelScope.launch { _captureRequest.emit(Unit) }
    }

    fun export(context: Context): File {
        val baseWidth = 1080f
        val aspectRatio = canvasState.sheetSize.aspectRatio
        val baseHeight = baseWidth / aspectRatio
        
        val scale = if (qualityPercent < 0.34f) 0.5f else if (qualityPercent < 0.67f) 0.75f else 1.0f
        val widthPx = (baseWidth * scale).toInt()
        val heightPx = (baseHeight * scale).toInt()

        val file = ExportManager.export(
            context = context,
            state = canvasState,
            fileBaseName = design?.name ?: "brochurecraft_design",
            format = format,
            widthPx = widthPx,
            heightPx = heightPx,
            htmlBitmap = capturedBitmap,
            quality = (qualityPercent * 100).toInt().coerceIn(1, 100)
        )
        _exportedFile.value = file
        return file
    }

    fun shareIntent(context: Context, file: File): Intent = ExportManager.shareIntent(context, file)
}
