package com.brochurecraft.app.ui.nav

import android.net.Uri

object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING_WELCOME = "onboarding_welcome"
    const val ONBOARDING_BUSINESS = "onboarding_business"
    const val HOME = "home"
    const val TEMPLATES = "templates"
    const val GALLERY = "gallery"
    const val BRAND_KIT = "brand_kit"
    const val PROFILE = "profile"
    const val PREMIUM = "premium"

    const val EDITOR = "editor?designId={designId}&templateId={templateId}&name={name}"
    fun editorForNewBlank(name: String = "Untitled Design") =
        "editor?designId=-1&templateId=-1&name=${Uri.encode(name)}"
    fun editorForDesign(designId: Long) =
        "editor?designId=$designId&templateId=-1&name="
    fun editorForTemplate(templateId: Long, name: String) =
        "editor?designId=-1&templateId=$templateId&name=${Uri.encode(name)}"

    const val EXPORT = "export/{designId}"
    fun export(designId: Long) = "export/$designId"
}

/** Bottom navigation destinations, matching the 5-tab bar in the Stitch designs. */
enum class BottomDestination(val route: String, val label: String) {
    HOME(Routes.HOME, "Home"),
    TEMPLATES(Routes.TEMPLATES, "Templates"),
    MY_DESIGNS(Routes.GALLERY, "My Designs"),
    BRAND(Routes.BRAND_KIT, "Brand"),
    PROFILE(Routes.PROFILE, "Profile")
}
