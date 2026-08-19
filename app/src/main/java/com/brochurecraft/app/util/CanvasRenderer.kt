package com.brochurecraft.app.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.graphics.toColorInt
import com.brochurecraft.app.data.model.DesignCanvasState
import com.brochurecraft.app.data.model.ElementType
import com.brochurecraft.app.data.model.ShapeKind

/** Renders a [DesignCanvasState] onto a real android.graphics.Bitmap for
 * thumbnails and for PNG/JPG/PDF export - fully local, no network required. */
object CanvasRenderer {

    fun render(state: DesignCanvasState, widthPx: Int, heightPx: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawOnCanvas(canvas, state, widthPx, heightPx)
        return bitmap
    }

    fun drawOnCanvas(canvas: Canvas, state: DesignCanvasState, widthPx: Int, heightPx: Int) {
        val bg = safeColor(state.backgroundColorHex, Color.WHITE)
        canvas.drawColor(bg)

        val sorted = state.elements.sortedBy { it.zIndex }
        for (el in sorted) {
            val left = el.x * widthPx
            val top = el.y * heightPx
            val w = el.width * widthPx
            val h = el.height * heightPx

            canvas.save()
            canvas.rotate(el.rotation, left + w / 2f, top + h / 2f)

            when (el.type) {
                ElementType.SHAPE -> {
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = safeColor(el.fillColorHex, Color.LTGRAY)
                        style = Paint.Style.FILL
                    }
                    when (el.shapeKind) {
                        ShapeKind.CIRCLE -> canvas.drawOval(RectF(left, top, left + w, top + h), paint)
                        ShapeKind.LINE -> canvas.drawRect(left, top + h / 2, left + w, top + h / 2 + 4, paint)
                        ShapeKind.RECTANGLE -> {
                            val r = el.cornerRadiusDp
                            canvas.drawRoundRect(RectF(left, top, left + w, top + h), r, r, paint)
                        }
                    }
                }
                ElementType.TEXT -> {
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = safeColor(el.colorHex, Color.BLACK)
                        textSize = el.fontSizeSp * (widthPx / 400f) // scale roughly with canvas
                        isFakeBoldText = el.bold
                        textAlign = when (el.textAlign) {
                            "LEFT" -> Paint.Align.LEFT
                            "RIGHT" -> Paint.Align.RIGHT
                            else -> Paint.Align.CENTER
                        }
                    }
                    drawMultilineText(canvas, el.text, paint, left, top, w, h)
                }
                ElementType.IMAGE -> {
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = safeColor("#D8E3FB", Color.LTGRAY)
                    }
                    canvas.drawRoundRect(RectF(left, top, left + w, top + h), 12f, 12f, paint)
                    // Real bitmap decoding from el.imageUri happens in the editor layer
                    // (Coil AsyncImage) - export re-decodes via ExportManager when present.
                }
            }
            canvas.restore()
        }
    }

    private fun drawMultilineText(canvas: Canvas, text: String, paint: Paint, left: Float, top: Float, w: Float, h: Float) {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var current = StringBuilder()
        for (word in words) {
            val candidate = if (current.isEmpty()) word else "$current $word"
            if (paint.measureText(candidate) > w && current.isNotEmpty()) {
                lines.add(current.toString())
                current = StringBuilder(word)
            } else {
                current = StringBuilder(candidate)
            }
        }
        if (current.isNotEmpty()) lines.add(current.toString())

        val lineHeight = paint.fontSpacing
        val totalHeight = lineHeight * lines.size
        var y = top + (h - totalHeight) / 2f - paint.ascent()
        val x = when (paint.textAlign) {
            Paint.Align.LEFT -> left
            Paint.Align.RIGHT -> left + w
            else -> left + w / 2f
        }
        for (line in lines) {
            canvas.drawText(line, x, y, paint)
            y += lineHeight
        }
    }

    private fun safeColor(hex: String?, fallback: Int): Int =
        try {
            hex?.toColorInt() ?: fallback
        } catch (e: Exception) {
            fallback
        }
}
