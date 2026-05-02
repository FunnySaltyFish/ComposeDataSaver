package com.funny.data_saver

import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.window.ComposeViewport
import com.funny.data_saver.ui.App
import composedatasaver.composeapp.generated.resources.NotoSansSC_VF
import composedatasaver.composeapp.generated.resources.Res
import kotlinx.browser.document
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.preloadFont

@OptIn(ExperimentalComposeUiApi::class, ExperimentalResourceApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        val fontResolver = LocalFontFamilyResolver.current
        val fallbackFont by preloadFont(Res.font.NotoSansSC_VF)
        var fallbackFontReady by remember { mutableStateOf(false) }

        LaunchedEffect(fallbackFont) {
            val font = fallbackFont ?: return@LaunchedEffect
            fontResolver.preload(FontFamily(font))
            fallbackFontReady = true
        }

        if (fallbackFontReady) {
            App()
        } else {
            Text("Loading fonts...")
        }
    }
}
