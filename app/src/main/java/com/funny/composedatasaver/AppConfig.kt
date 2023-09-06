package com.funny.composedatasaver

import android.util.Log
import com.funny.composedatasaver.ui.ExampleBean
import com.funny.composedatasaver.ui.ThemeType
import com.funny.data_saver.core.DataSaverConverter
import com.funny.data_saver_mmkv.DataSaverMMKV
import com.tencent.mmkv.MMKV
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object AppConfig {
    val dataSaver by lazy {
        DataSaverMMKV(kv = MMKV.defaultMMKV(), senseExternalDataChange = true)
        // or DataSaverPreferences(appCtx.getSharedPreferences("default", 0))
        // or DataSaverDataStorePreferences(appCtx.dataStore)
    }

    init {
        registerAllTypeConverters()
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun registerAllTypeConverters() {
        // cause we want to save custom bean, we provide a converter to convert it into String
        DataSaverConverter.registerTypeConverters<ExampleBean?>(
            save = { bean -> Json.encodeToString(bean) },
            restore = { str ->
                Log.d("ExampleActivity", "restore ExampleBean? from string: $str")
                Json.decodeFromString(str)
            }
        )

        DataSaverConverter.registerTypeConverters<ThemeType>(
            save = ThemeType.Saver,
            restore = ThemeType.Restorer
        )
    }
}