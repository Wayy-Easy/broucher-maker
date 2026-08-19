package com.brochurecraft.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "templates")
data class TemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,          // Restaurant, Cafe, Hotel, Bakery, Cloud Kitchen, Catering
    val subtitle: String,          // e.g. "Restaurant Menu"
    val isPro: Boolean = false,
    val badgeText: String? = null, // e.g. "Weekend Special", "50% OFF"
    val accentColorHex: String = "#4648D4",
    val elementsJson: String
)
