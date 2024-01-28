package com.funny.data_saver

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.funny.data_saver.AppConfig
import com.funny.data_saver.ui.ExampleComposable
import com.funny.data_saver.ui.theme.FunnyTheme
import com.funny.data_saver.core.LocalDataSaver
import kotlinx.serialization.ExperimentalSerializationApi

val Context.dataStore : DataStore<Preferences> by preferencesDataStore("dataStore")

@OptIn(ExperimentalSerializationApi::class)
class ExampleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dataSaverMMKV = AppConfig.dataSaver
        // or DataSaverMMKV(MMKV.defaultMMKV())
        // you can use DefaultDataSaverMMKV like `DefaultDataSaverMMKV.readData(key, default)` and `DefaultDataSaverMMKV.saveData(key, value) anywhere`

        // if you want to use [DataStorePreference](https://developer.android.google.cn/jetpack/androidx/releases/datastore) to save data
        // val dataSaverDataStorePreferences = DataSaverDataStorePreferences(applicationContext.dataStore)

        setContent {
            FunnyTheme {
                CompositionLocalProvider(LocalDataSaver provides dataSaverMMKV){
                    // or LocalDataSaver provides dataSaverMMKV
                    // or LocalDataSaver provides dataSaverDataStorePreferences
                    // or your Class instance
                    ExampleComposable()
                }
            }
        }
    }
}

