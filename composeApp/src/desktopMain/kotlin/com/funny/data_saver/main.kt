package com.funny.data_saver

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.funny.data_saver.ui.App
import kotlinx.serialization.ExperimentalSerializationApi
import moe.tlaster.precompose.ProvidePreComposeLocals

@OptIn(ExperimentalSerializationApi::class)
fun main() {
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Compose Data Saver"
        ) {
            ProvidePreComposeLocals {
                App()
            }
        }
    }
}
