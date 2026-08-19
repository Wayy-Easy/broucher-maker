package com.brochurecraft.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "brand_kit")
data class BrandKitEntity(
    @PrimaryKey val id: Int = 1, // singleton row
    val businessName: String = "",
    val tagline: String = "",
    val logoUri: String? = null,
    val colorsJson: String = "[\"#B4132D\",\"#F5A623\",\"#111C2D\",\"#EAF2EC\"]",
    val colorLabelsJson: String = "[\"Brand Red\",\"Accent\",\"Dark\",\"Light\"]",
    val phoneNumber: String = "",
    val whatsapp: String = "",
    val address: String = ""
)
