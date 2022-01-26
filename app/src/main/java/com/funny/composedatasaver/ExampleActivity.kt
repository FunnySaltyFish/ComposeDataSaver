package com.funny.composedatasaver

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.funny.composedatasaver.ui.ExampleBean
import com.funny.composedatasaver.ui.ExampleComposable
import com.funny.composedatasaver.ui.theme.FunnyTheme
import com.funny.data_saver.core.DataSaverPreferences
import com.funny.data_saver.core.DataSaverPreferences.Companion.setContext
import com.funny.data_saver.core.LocalDataSaver
import com.funny.data_saver.core.registerTypeConverters
import com.funny.data_saver_data_store.DataSaverDataStorePreferences
import com.funny.data_saver_data_store.DataSaverDataStorePreferences.Companion.setDataStorePreferences
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val Context.dataStore : DataStore<Preferences> by preferencesDataStore("dataStore")

@OptIn(ExperimentalSerializationApi::class)
class ExampleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // init preferences
        val dataSaverPreferences = DataSaverPreferences().apply {
            setContext(context = applicationContext)
        }

        // If you want to use [MMKV](https://github.com/Tencent/MMKV) to save data
//        MMKV.initialize(applicationContext)
//        val dataSaverMMKV = DataSaverMMKV().apply {
//            setKV(newKV = MMKV.defaultMMKV())
//        }

        // if you want to use [DataStorePreference](https://developer.android.google.cn/jetpack/androidx/releases/datastore) to save data
//        val dataSaverDataStorePreferences = DataSaverDataStorePreferences().apply {
//            setDataStorePreferences(applicationContext.dataStore)
//        }

        // cause we want to save custom bean, we provide a converter to convert it into String
        registerTypeConverters(ExampleBean::class.java) {
            val bean = it as ExampleBean
            Json.encodeToString(bean)
        }

        setContent {
            FunnyTheme {
                CompositionLocalProvider(LocalDataSaver provides dataSaverPreferences){
                    // or LocalDataSaver provides dataSaverMMKV
                    // or LocalDataSaver provides dataSaverDataStorePreferences
                    // or your Class instance
                    ExampleComposable()
                }
            }
        }
    }
}