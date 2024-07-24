package com.funny.data_saver.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

// Comes From [FunnyTranslation](https://github.com/FunnySaltyFish/FunnyTranslation)
sealed class ThemeType(val id: Int) {
    data object StaticDefault: ThemeType(-1)
    data object DynamicNative : ThemeType(0)
    class DynamicFromImage(val color: Color) : ThemeType(1)
    class StaticFromColor(val color: Color): ThemeType(2)

    val isDynamic get() = this is DynamicNative || this is DynamicFromImage

    companion object {
        val Saver = { themeType: ThemeType ->
            when(themeType){
                StaticDefault -> "-1#0"
                DynamicNative -> "0#0"
                is DynamicFromImage -> "1#${themeType.color.toArgb()}"
                is StaticFromColor -> "2#${themeType.color.toArgb()}"
            }
        }

        val Restorer = { str: String ->
            val (id, color) = str.split("#")
            when(id){
                "-1" -> StaticDefault
                "0" -> DynamicNative
                "1" -> DynamicFromImage(Color(color.toInt()))
                "2" -> StaticFromColor(Color(color.toInt()))
                else -> throw IllegalArgumentException("Unknown ThemeType: $str")
            }
        }
    }
}