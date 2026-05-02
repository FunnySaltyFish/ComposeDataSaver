package com.funny.data_saver.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

internal expect fun platformFontFamily(): FontFamily

private val AppFontFamily = platformFontFamily()

val Typography = Typography().run {
    copy(
        displayLarge = displayLarge.copy(fontFamily = AppFontFamily),
        displayMedium = displayMedium.copy(fontFamily = AppFontFamily),
        displaySmall = displaySmall.copy(fontFamily = AppFontFamily),
        headlineLarge = headlineLarge.copy(fontFamily = AppFontFamily),
        headlineMedium = headlineMedium.copy(fontFamily = AppFontFamily),
        headlineSmall = headlineSmall.copy(fontFamily = AppFontFamily),
        titleLarge = titleLarge.copy(fontFamily = AppFontFamily),
        titleMedium = titleMedium.copy(fontFamily = AppFontFamily),
        titleSmall = titleSmall.copy(fontFamily = AppFontFamily),
        bodyLarge = bodyLarge.copy(
            fontFamily = AppFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp
        ),
        bodyMedium = bodyMedium.copy(fontFamily = AppFontFamily),
        bodySmall = bodySmall.copy(fontFamily = AppFontFamily),
        labelLarge = labelLarge.copy(
            fontFamily = AppFontFamily,
            fontWeight = FontWeight.W500,
            fontSize = 18.sp
        ),
        labelMedium = labelMedium.copy(fontFamily = AppFontFamily),
        labelSmall = labelSmall.copy(fontFamily = AppFontFamily)
    )
}
