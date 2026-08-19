package com.brochurecraft.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

// NOTE: The Stitch design system specifies "Plus Jakarta Sans" (UI/body) and
// "JetBrains Mono" (labels/technical values). To keep this project buildable
// without bundling external font files, we use the platform default
// FontFamily.SansSerif / FontFamily.Monospace as stand-ins. Drop .ttf files
// into res/font and swap these FontFamily values to use the real typefaces.
val PlusJakartaSans = FontFamily.SansSerif
val JetBrainsMono = FontFamily.Monospace

// Design tokens -> Compose TextStyles (mirrors DESIGN.md "typography" block)
val DisplayLg = TextStyle(
    fontFamily = PlusJakartaSans,
    fontSize = 40.sp,
    fontWeight = FontWeight.ExtraBold,
    lineHeight = 48.sp,
    letterSpacing = (-0.02).em
)
val HeadlineLg = TextStyle(
    fontFamily = PlusJakartaSans,
    fontSize = 28.sp,
    fontWeight = FontWeight.Bold,
    lineHeight = 36.sp
)
val HeadlineLgMobile = TextStyle(
    fontFamily = PlusJakartaSans,
    fontSize = 24.sp,
    fontWeight = FontWeight.Bold,
    lineHeight = 32.sp
)
val TitleMd = TextStyle(
    fontFamily = PlusJakartaSans,
    fontSize = 18.sp,
    fontWeight = FontWeight.SemiBold,
    lineHeight = 24.sp
)
val BodyLg = TextStyle(
    fontFamily = PlusJakartaSans,
    fontSize = 16.sp,
    fontWeight = FontWeight.Normal,
    lineHeight = 24.sp
)
val BodySm = TextStyle(
    fontFamily = PlusJakartaSans,
    fontSize = 14.sp,
    fontWeight = FontWeight.Normal,
    lineHeight = 20.sp
)
val LabelCaps = TextStyle(
    fontFamily = JetBrainsMono,
    fontSize = 12.sp,
    fontWeight = FontWeight.Medium,
    lineHeight = 16.sp,
    letterSpacing = 0.05.em
)
val ButtonText = TextStyle(
    fontFamily = PlusJakartaSans,
    fontSize = 14.sp,
    fontWeight = FontWeight.SemiBold,
    lineHeight = 20.sp
)

val AppTypography = Typography(
    displayLarge = DisplayLg,
    headlineLarge = HeadlineLg,
    headlineMedium = HeadlineLgMobile,
    titleMedium = TitleMd,
    bodyLarge = BodyLg,
    bodyMedium = BodySm,
    labelSmall = LabelCaps,
    labelLarge = ButtonText
)
