package com.brochurecraft.app.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

enum class ElementType { TEXT, IMAGE, SHAPE }
enum class ShapeKind { RECTANGLE, CIRCLE, LINE }

/**
 * @param previewWidthPx The CSS/layout width (in px == dp in our WebView setup) that the
 *   HTML template should be rendered at for this size. This is the single source of truth
 *   consumed by both the editor canvas and the export preview so a given [SheetSize] always
 *   renders identically in both places. Distinct values are required for each entry so that
 *   switching sizes visibly triggers the template's responsive breakpoints.
 * @param isDesktopPreview When true, the WebView emulates a desktop browser (desktop user
 *   agent) instead of a mobile one. Only "Desktop" should set this.
 */
@Serializable
enum class SheetSize(
    val label: String,
    val aspectRatio: Float,
    val previewWidthPx: Int,
    val isDesktopPreview: Boolean = false
) {
    A5("A5", 1f / 1.414f, 640),
    A4("A4", 1f / 1.414f, 900),
    A3("A3", 1f / 1.414f, 1200),
    MOBILE("Mobile", 9f / 16f, 420),
    TABLET("Tablet", 3f / 4f, 800),
    DESKTOP("Desktop", 16f / 9f, 1440, isDesktopPreview = true)
}

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
    val backgroundImageUri: String? = null,
    val sheetSize: SheetSize = SheetSize.A4,
    val htmlContent: String? = null
)

@Serializable
data class HtmlElementProperties(
    val tagName: String,
    val id: String? = null,
    val text: String? = null,
    val color: String? = null,
    val backgroundColor: String? = null,
    val fontSize: String? = null,
    val fontWeight: String? = null,
    val fontFamily: String? = null,
    val textAlign: String? = null,
    val lineHeight: String? = null,
    val padding: String? = null,
    val margin: String? = null,
    val borderRadius: String? = null,
    val boxShadow: String? = null,
    val src: String? = null,
    val objectFit: String? = null
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
