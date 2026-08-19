package com.brochurecraft.app.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brochurecraft.app.data.db.entity.DesignEntity
import com.brochurecraft.app.data.model.DesignCanvasState
import com.brochurecraft.app.data.model.DesignElement
import com.brochurecraft.app.data.model.DesignJson
import com.brochurecraft.app.data.model.ElementType
import com.brochurecraft.app.data.model.ShapeKind
import com.brochurecraft.app.data.repository.DesignRepository
import com.brochurecraft.app.data.repository.TemplateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class EditorViewModel(
    private val designRepo: DesignRepository,
    private val templateRepo: TemplateRepository
) : ViewModel() {

    var designId: Long? = null
        private set
    var designName by mutableStateOf("Untitled Design")
        private set

    var canvasState by mutableStateOf(DesignCanvasState())
        private set

    var selectedElementId by mutableStateOf<String?>(null)
        private set

    private val undoStack = ArrayDeque<DesignCanvasState>()
    private val redoStack = ArrayDeque<DesignCanvasState>()

    private val _saveEvent = MutableStateFlow<Long?>(null)
    val saveEvent: StateFlow<Long?> = _saveEvent

    private val _loaded = MutableStateFlow(false)
    val loaded: StateFlow<Boolean> = _loaded

    fun loadDesign(id: Long) {
        if (_loaded.value && designId == id) return
        viewModelScope.launch {
            val entity = designRepo.getById(id)
            if (entity != null) {
                designId = entity.id
                designName = entity.name
                canvasState = DesignJson.decode(entity.elementsJson)
            }
            _loaded.value = true
        }
    }

    fun loadFromTemplate(templateId: Long, name: String) {
        if (_loaded.value) return
        viewModelScope.launch {
            val template = templateRepo.getById(templateId)
            if (template != null) {
                designName = name.ifBlank { template.name }
                canvasState = DesignJson.decode(template.elementsJson)
            }
            _loaded.value = true
        }
    }

    fun startBlank(name: String) {
        if (_loaded.value) return
        designName = name.ifBlank { "Untitled Design" }
        canvasState = DesignCanvasState(
            elements = listOf(
                DesignElement(
                    id = UUID.randomUUID().toString(),
                    type = ElementType.TEXT,
                    x = 0.1f, y = 0.42f, width = 0.8f, height = 0.14f,
                    text = "Your Headline Here", fontSizeSp = 28f, bold = true,
                    colorHex = "#111C2D", textAlign = "CENTER"
                )
            ),
            backgroundColorHex = "#FFFFFF"
        )
        _loaded.value = true
    }

    fun setName(name: String) { designName = name }

    private fun pushUndo() {
        undoStack.addLast(canvasState)
        if (undoStack.size > 30) undoStack.removeFirst()
        redoStack.clear()
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        redoStack.addLast(canvasState)
        canvasState = undoStack.removeLast()
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        undoStack.addLast(canvasState)
        canvasState = redoStack.removeLast()
    }

    fun selectElement(id: String?) { selectedElementId = id }

    fun addTextElement(text: String = "New Text") {
        pushUndo()
        val newEl = DesignElement(
            id = UUID.randomUUID().toString(),
            type = ElementType.TEXT,
            x = 0.15f, y = 0.45f, width = 0.7f, height = 0.1f,
            text = text, fontSizeSp = 20f,
            colorHex = "#111C2D", textAlign = "CENTER",
            zIndex = (canvasState.elements.maxOfOrNull { it.zIndex } ?: 0) + 1
        )
        canvasState = canvasState.copy(elements = canvasState.elements + newEl)
        selectedElementId = newEl.id
    }

    fun addShapeElement(kind: ShapeKind = ShapeKind.RECTANGLE) {
        pushUndo()
        val newEl = DesignElement(
            id = UUID.randomUUID().toString(),
            type = ElementType.SHAPE,
            x = 0.3f, y = 0.4f, width = 0.4f, height = 0.2f,
            shapeKind = kind, fillColorHex = "#4648D4",
            zIndex = (canvasState.elements.maxOfOrNull { it.zIndex } ?: 0) + 1
        )
        canvasState = canvasState.copy(elements = canvasState.elements + newEl)
        selectedElementId = newEl.id
    }

    fun addImageElement(uri: String) {
        pushUndo()
        val newEl = DesignElement(
            id = UUID.randomUUID().toString(),
            type = ElementType.IMAGE,
            x = 0.1f, y = 0.1f, width = 0.8f, height = 0.35f,
            imageUri = uri,
            zIndex = (canvasState.elements.maxOfOrNull { it.zIndex } ?: 0) + 1
        )
        canvasState = canvasState.copy(elements = canvasState.elements + newEl)
        selectedElementId = newEl.id
    }

    /** Live-drag update (no undo snapshot per-frame - only on drag start). */
    fun updateElementLive(id: String, transform: (DesignElement) -> DesignElement) {
        canvasState = canvasState.copy(
            elements = canvasState.elements.map { if (it.id == id) transform(it) else it }
        )
    }

    fun beginDragSnapshot() = pushUndo()

    fun updateSelectedText(text: String, fontSizeSp: Float? = null, colorHex: String? = null, bold: Boolean? = null) {
        val id = selectedElementId ?: return
        pushUndo()
        canvasState = canvasState.copy(
            elements = canvasState.elements.map {
                if (it.id == id) it.copy(
                    text = text,
                    fontSizeSp = fontSizeSp ?: it.fontSizeSp,
                    colorHex = colorHex ?: it.colorHex,
                    bold = bold ?: it.bold
                ) else it
            }
        )
    }

    fun updateShapeColor(colorHex: String) {
        val id = selectedElementId ?: return
        pushUndo()
        canvasState = canvasState.copy(
            elements = canvasState.elements.map {
                if (it.id == id) it.copy(fillColorHex = colorHex) else it
            }
        )
    }

    fun deleteSelected() {
        val id = selectedElementId ?: return
        pushUndo()
        canvasState = canvasState.copy(elements = canvasState.elements.filterNot { it.id == id })
        selectedElementId = null
    }

    fun bringForward() {
        val id = selectedElementId ?: return
        pushUndo()
        canvasState = canvasState.copy(
            elements = canvasState.elements.map { if (it.id == id) it.copy(zIndex = it.zIndex + 1) else it }
        )
    }

    fun setBackgroundColor(hex: String) {
        pushUndo()
        canvasState = canvasState.copy(backgroundColorHex = hex)
    }

    fun selectedElement(): DesignElement? = canvasState.elements.find { it.id == selectedElementId }

    fun save(thumbnailPath: String?) {
        viewModelScope.launch {
            val json = DesignJson.encode(canvasState)
            val id = designId
            val newId = if (id == null) {
                designRepo.save(
                    DesignEntity(
                        name = designName,
                        elementsJson = json,
                        thumbnailPath = thumbnailPath
                    )
                )
            } else {
                val existing = designRepo.getById(id)
                if (existing != null) {
                    designRepo.update(
                        existing.copy(
                            name = designName,
                            elementsJson = json,
                            thumbnailPath = thumbnailPath ?: existing.thumbnailPath
                        )
                    )
                }
                id
            }
            designId = newId
            _saveEvent.value = newId
        }
    }
}
