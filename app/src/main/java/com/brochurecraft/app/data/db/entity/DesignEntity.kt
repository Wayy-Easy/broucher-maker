package com.brochurecraft.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "designs")
data class DesignEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String = "General",
    val elementsJson: String,
    val canvasWidthPx: Int = 1080,
    val canvasHeightPx: Int = 1512,
    val thumbnailPath: String? = null,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val sourceTemplateId: Long? = null
)
