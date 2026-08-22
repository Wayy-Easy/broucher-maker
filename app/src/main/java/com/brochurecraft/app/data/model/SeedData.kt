package com.brochurecraft.app.data.model

import com.brochurecraft.app.data.db.entity.DesignEntity
import com.brochurecraft.app.data.db.entity.TemplateEntity

/** Local seed content so the app has real, browsable data on first launch (no network needed). */
object SeedData {

    val businessCategories = listOf(
        "Restaurant", "Cafe", "Hotel", "Bakery", "Cloud Kitchen", "Catering"
    )

    fun templates(): List<TemplateEntity> = listOf(
        TemplateEntity(
            name = "Appetizer Special", category = "Restaurant", subtitle = "Elegant Menu",
            isPro = true, badgeText = "HTML", accentColorHex = "#7e181e",
            elementsJson = "html:template_1.html"
        ),
        TemplateEntity(
            name = "Gusto Fine Dining", category = "Restaurant", subtitle = "Luxury Menu",
            isPro = false, badgeText = "HTML", accentColorHex = "#540B1E",
            elementsJson = "html:template_2.html"
        ),
        TemplateEntity(
            name = "Healthy Life", category = "Cafe", subtitle = "Wellness Menu",
            isPro = true, badgeText = "HTML", accentColorHex = "#a84c38",
            elementsJson = "html:template_3.html"
        ),
        TemplateEntity(
            name = "Bercelle Menu", category = "Restaurant", subtitle = "Green Cuisine",
            isPro = false, badgeText = "HTML", accentColorHex = "#297a38",
            elementsJson = "html:template_4.html"
        ),
        TemplateEntity(
            name = "Wellness Promo", category = "Catering", subtitle = "Health Event",
            isPro = true, badgeText = "HTML", accentColorHex = "#4b5563",
            elementsJson = "html:template_5.html"
        ),
        TemplateEntity(
            name = "Royal Gusto", category = "Restaurant", subtitle = "Royal Experience",
            isPro = true, badgeText = "HTML", accentColorHex = "#4A0E23",
            elementsJson = "html:template_6.html"
        ),
        TemplateEntity(
            name = "Signature Selections", category = "Hotel", subtitle = "Hotel Brochure",
            isPro = false, badgeText = "HTML", accentColorHex = "#1f2937",
            elementsJson = "html:template_7.html"
        ),
        TemplateEntity(
            name = "Delicious Menu", category = "Restaurant", subtitle = "Special Food",
            isPro = false, badgeText = "HTML", accentColorHex = "#114a31",
            elementsJson = "html:template_8.html"
        )
    )

    fun sampleDesigns(): List<DesignEntity> = listOf(
        DesignEntity(
            name = "My First Menu",
            category = "Restaurant",
            elementsJson = "html:template_2.html",
            isFavorite = true
        )
    )
}
