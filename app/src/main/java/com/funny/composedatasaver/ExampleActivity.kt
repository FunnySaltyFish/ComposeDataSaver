package com.funny.composedatasaver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import com.funny.composedatasaver.ui.ExampleBean
import com.funny.composedatasaver.ui.ExampleComposable
import com.funny.composedatasaver.ui.theme.FunnyTheme
import com.funny.data_saver.core.DataSaverPreferences
import com.funny.data_saver.core.DataSaverPreferences.Companion.setContext
import com.funny.data_saver.core.LocalDataSaver
import com.funny.data_saver.core.registerTypeConverters
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
class ExampleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // init preferences
        val dataSaverPreferences = DataSaverPreferences().apply {
            setContext(context = applicationContext)
        }

        // cause we want to save custom bean, we provide a converter to convert it into String
        registerTypeConverters(ExampleBean::class.java) {
            val bean = it as ExampleBean
            Json.encodeToString(bean)
        }


        setContent {
            FunnyTheme {
                CompositionLocalProvider(LocalDataSaver provides dataSaverPreferences){
                    ExampleComposable()
                }
            }
        }
    }
}