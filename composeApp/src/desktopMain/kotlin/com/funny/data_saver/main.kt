package com.funny.data_saver

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.funny.data_saver.core.LocalDataSaver
import com.funny.data_saver.ui.ExampleComposable
import com.funny.data_saver.ui.theme.FunnyTheme
import kotlinx.serialization.ExperimentalSerializationApi

@OptIn(ExperimentalSerializationApi::class)
fun main() {
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Compose Data Saver"
        ) {
            FunnyTheme {
                CompositionLocalProvider(LocalDataSaver provides AppConfig.dataSaver){
                    ExampleComposable()
                }
            }
        }
    }
}
