package com.brochurecraft.app.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

enum class ElementType { TEXT, IMAGE, SHAPE }
enum class ShapeKind { RECTANGLE, CIRCLE, LINE }

/**
 * A single element placed on the design canvas (text box, image, or shape).
 * Position/size are stored as fractions (0f..1f) of the canvas so designs
 * remain resolution independent between the editor preview and export.
 */
@Serializable
data class DesignElement(
    val id: String,
    val type: ElementType,
    var x: Float,               // left, fraction of canvas width
    var y: Float,               // top, fraction of canvas height
    var width: Float,           // fraction of canvas width
    var height: Float,          // fraction of canvas height
    var rotation: Float = 0f,
    var zIndex: Int = 0,
    // text
    var text: String = "",
    var fontSizeSp: Float = 16f,
    var colorHex: String = "#111C2D",
    var bold: Boolean = false,
    var italic: Boolean = false,
    var textAlign: String = "CENTER", // LEFT, CENTER, RIGHT
    var fontFamily: String = "PlusJakartaSans",
    // image
    var imageUri: String? = null,
    // shape
    var shapeKind: ShapeKind = ShapeKind.RECTANGLE,
    var fillColorHex: String = "#4648D4",
    var cornerRadiusDp: Float = 8f
)

@Serializable
data class DesignCanvasState(
    val elements: List<DesignElement> = emptyList(),
    val backgroundColorHex: String = "#FFFFFF",
    val backgroundImageUri: String? = null
)

object DesignJson {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    fun encode(state: DesignCanvasState): String = json.encodeToString(state)
    fun decode(raw: String?): DesignCanvasState =
        if (raw.isNullOrBlank()) DesignCanvasState() else try {
            json.decodeFromString(raw)
        } catch (e: Exception) {
            DesignCanvasState()
        }
}
