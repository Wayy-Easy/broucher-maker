package com.brochurecraft.app.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.brochurecraft.app.data.model.DesignCanvasState
import java.io.File
import java.io.FileOutputStream

enum class ExportFormat { PDF, PNG, JPG }

object ExportManager {

    fun export(
        context: Context,
        state: DesignCanvasState,
        fileBaseName: String,
        format: ExportFormat,
        widthPx: Int = 1080,
        heightPx: Int = 1512,
        htmlBitmap: Bitmap? = null,
        quality: Int = 100
    ): File {
        val exportsDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val safeName = fileBaseName.ifBlank { "design" }.replace(Regex("[^A-Za-z0-9_-]"), "_")

        return when (format) {
            ExportFormat.PNG -> {
                val bmp = htmlBitmap ?: CanvasRenderer.render(state, widthPx, heightPx)
                val file = File(exportsDir, "$safeName.png")
                FileOutputStream(file).use { bmp.compress(Bitmap.CompressFormat.PNG, quality, it) }
                file
            }
            ExportFormat.JPG -> {
                val bmp = htmlBitmap ?: CanvasRenderer.render(state, widthPx, heightPx)
                val file = File(exportsDir, "$safeName.jpg")
                FileOutputStream(file).use { bmp.compress(Bitmap.CompressFormat.JPEG, quality, it) }
                file
            }
            ExportFormat.PDF -> {
                val file = File(exportsDir, "$safeName.pdf")
                val document = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(widthPx, heightPx, 1).create()
                val page = document.startPage(pageInfo)
                if (htmlBitmap != null) {
                    page.canvas.drawBitmap(htmlBitmap, null, RectF(0f, 0f, widthPx.toFloat(), heightPx.toFloat()), null)
                } else {
                    CanvasRenderer.drawOnCanvas(page.canvas, state, widthPx, heightPx)
                }
                document.finishPage(page)
                FileOutputStream(file).use { document.writeTo(it) }
                document.close()
                file
            }
        }
    }

    fun shareIntent(context: Context, file: File): Intent {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val mime = when (file.extension.lowercase()) {
            "pdf" -> "application/pdf"
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            else -> "*/*"
        }
        val send = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return Intent.createChooser(send, "Share design")
    }

    fun thumbnailFile(context: Context, designId: Long, state: DesignCanvasState): String {
        val dir = File(context.filesDir, "thumbnails").apply { mkdirs() }
        val file = File(dir, "design_$designId.png")
        val bmp = CanvasRenderer.render(state, 360, 504)
        FileOutputStream(file).use { bmp.compress(Bitmap.CompressFormat.PNG, 90, it) }
        return file.absolutePath
    }

    fun saveThumbnail(context: Context, designId: Long, bitmap: Bitmap): String {
        val dir = File(context.filesDir, "thumbnails").apply { mkdirs() }
        val file = File(dir, "design_$designId.png")
        val scaled = Bitmap.createScaledBitmap(bitmap, 360, (360 * (bitmap.height.toFloat() / bitmap.width)).toInt(), true)
        FileOutputStream(file).use { scaled.compress(Bitmap.CompressFormat.PNG, 90, it) }
        return file.absolutePath
    }
}
