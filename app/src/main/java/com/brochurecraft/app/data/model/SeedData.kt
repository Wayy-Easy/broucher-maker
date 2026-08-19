package com.brochurecraft.app.data.model

import com.brochurecraft.app.data.db.entity.DesignEntity
import com.brochurecraft.app.data.db.entity.TemplateEntity

/** Local seed content so the app has real, browsable data on first launch (no network needed). */
object SeedData {

    val businessCategories = listOf(
        "Restaurant", "Cafe", "Hotel", "Bakery", "Cloud Kitchen", "Catering"
    )

    private fun titleTextElements(
        title: String,
        subtitle: String,
        badge: String?,
        accent: String
    ): DesignCanvasState {
        val elements = mutableListOf(
            DesignElement(
                id = "title",
                type = ElementType.TEXT,
                x = 0.08f, y = 0.55f, width = 0.84f, height = 0.16f,
                text = title, fontSizeSp = 30f, bold = true,
                colorHex = "#111C2D", textAlign = "CENTER", zIndex = 1
            ),
            DesignElement(
                id = "subtitle",
                type = ElementType.TEXT,
                x = 0.1f, y = 0.72f, width = 0.8f, height = 0.14f,
                text = subtitle, fontSizeSp = 15f,
                colorHex = "#464554", textAlign = "CENTER", zIndex = 1
            ),
            DesignElement(
                id = "accent_bar",
                type = ElementType.SHAPE,
                x = 0.42f, y = 0.87f, width = 0.16f, height = 0.006f,
                fillColorHex = accent, shapeKind = ShapeKind.RECTANGLE, zIndex = 1
            )
        )
        if (badge != null) {
            elements.add(
                DesignElement(
                    id = "badge",
                    type = ElementType.TEXT,
                    x = 0.06f, y = 0.06f, width = 0.5f, height = 0.06f,
                    text = badge, fontSizeSp = 12f, bold = true,
                    colorHex = "#FFFFFF", textAlign = "LEFT", zIndex = 2,
                    fontFamily = "JetBrainsMono"
                )
            )
        }
        return DesignCanvasState(elements = elements, backgroundColorHex = "#FFFFFF")
    }

    fun templates(): List<TemplateEntity> = listOf(
        TemplateEntity(
            name = "Artisan Pizzeria", category = "Restaurant", subtitle = "Restaurant Menu",
            isPro = true, badgeText = "PRO", accentColorHex = "#B4136D",
            elementsJson = DesignJson.encode(
                titleTextElements("LUNA", "Ristorante Italiano · Antipasti · Pasta · Dolci", "PRO", "#B4136D")
            )
        ),
        TemplateEntity(
            name = "Morning Brew", category = "Cafe", subtitle = "Cafe Menu",
            isPro = false, badgeText = null, accentColorHex = "#006C49",
            elementsJson = DesignJson.encode(
                titleTextElements("Artisan Brew", "Specialty Coffee · Filter & Cold · Espresso Drinks", null, "#006C49")
            )
        ),
        TemplateEntity(
            name = "Grand Horizon", category = "Hotel", subtitle = "Hotel Brochure",
            isPro = true, badgeText = "PRO", accentColorHex = "#4648D4",
            elementsJson = DesignJson.encode(
                titleTextElements("Grand Horizon", "A Timeless Stay in the Heart of the City", "PRO", "#4648D4")
            )
        ),
        TemplateEntity(
            name = "Sweet Crust", category = "Bakery", subtitle = "Bakery Menu",
            isPro = false, badgeText = null, accentColorHex = "#F59E0B",
            elementsJson = DesignJson.encode(
                titleTextElements("The Daily Crumb", "Fresh From The Oven · Croissants & Pastries", null, "#F59E0B")
            )
        ),
        TemplateEntity(
            name = "Bold Burger Promo", category = "Restaurant", subtitle = "A4 Flyer · Print Ready",
            isPro = false, badgeText = "Weekend Special", accentColorHex = "#4648D4",
            elementsJson = DesignJson.encode(
                titleTextElements("Weekend Special", "Experience our award-winning signature smash burger. Limited time offer this Friday through Sunday.", "Weekend Special", "#F59E0B")
            )
        ),
        TemplateEntity(
            name = "Artisan Pizza Post", category = "Restaurant", subtitle = "Instagram Post",
            isPro = false, badgeText = "50% OFF", accentColorHex = "#B4136D",
            elementsJson = DesignJson.encode(
                titleTextElements("Artisan Pizza", "Wood-fired, hand tossed, made fresh daily", "50% OFF", "#B4136D")
            )
        ),
        TemplateEntity(
            name = "Cloud Kitchen Combo", category = "Cloud Kitchen", subtitle = "Combo Offer",
            isPro = false, badgeText = null, accentColorHex = "#006C49",
            elementsJson = DesignJson.encode(
                titleTextElements("Combo Deal", "Two meals, one great price. Order in minutes.", null, "#006C49")
            )
        ),
        TemplateEntity(
            name = "Elegant Catering", category = "Catering", subtitle = "Event Catering",
            isPro = true, badgeText = "PRO", accentColorHex = "#4648D4",
            elementsJson = DesignJson.encode(
                titleTextElements("Elegant Catering", "Bespoke menus for weddings & corporate events", "PRO", "#4648D4")
            )
        )
    )

    fun sampleDesigns(): List<DesignEntity> = listOf(
        DesignEntity(
            name = "Diwali Special Menu",
            category = "Restaurant",
            elementsJson = DesignJson.encode(
                titleTextElements("Diwali Special", "Festive thali menu · limited week", null, "#F59E0B")
            ),
            isFavorite = false
        ),
        DesignEntity(
            name = "Bistro Menu Fall",
            category = "Restaurant",
            elementsJson = DesignJson.encode(
                titleTextElements("The Artisan Table", "Starters · Main Plates · Desserts · Wines", null, "#111C2D")
            ),
            isFavorite = true
        )
    )
}
