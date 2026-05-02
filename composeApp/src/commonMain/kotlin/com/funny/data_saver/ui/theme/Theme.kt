package com.funny.data_saver.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.funny.cmaterialcolors.MaterialColors.Companion.Blue500
import com.funny.cmaterialcolors.MaterialColors.Companion.BlueA700
import moe.tlaster.precompose.PreComposeApp

private val DarkColorPalette = darkColorScheme(
    primary = Color(0xff3d557b),
    primaryContainer = BlueA700,
    secondary = Color(0xff2b3c56),
    onPrimary = Color.White,
    onSecondary = Color.White,
    background = Color(0xff303135),
    onBackground = Color.White,
    surface = Color(0xff2b3c56).copy(0.7f),
    onSurface = Color.White
)

private val LightColorPalette = lightColorScheme(
    primary = Blue500,
    primaryContainer = BlueA700,
    secondary = Color(0xff4785da),
    onPrimary = Color.White,
    onSecondary = Color.White,
    background = Color.White,
    surface = Color(0xffdde8f9),
    onSurface = Color(0xff4785da)
)

@Composable
fun FunnyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable() () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    PreComposeApp {
        MaterialTheme(
            colorScheme = colors,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}
