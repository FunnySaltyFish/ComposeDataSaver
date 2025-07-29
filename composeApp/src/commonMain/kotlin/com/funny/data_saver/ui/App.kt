package com.funny.data_saver.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.funny.data_saver.AppConfig
import com.funny.data_saver.core.LocalDataSaver
import com.funny.data_saver.ui.theme.FunnyTheme
import kotlinx.serialization.ExperimentalSerializationApi

@OptIn(ExperimentalSerializationApi::class)
@Composable
fun App() {
    FunnyTheme {
        CompositionLocalProvider(LocalDataSaver provides AppConfig.dataSaver) {
            ExampleComposable()
        }
    }
}