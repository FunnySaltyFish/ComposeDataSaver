package com.funny.data_saver

import com.funny.data_saver.core.DataSaverConverter
import com.funny.data_saver.core.DataSaverInterface
import com.funny.data_saver.kmp.Log
import com.funny.data_saver.ui.ExampleBean
import com.funny.data_saver.ui.ThemeType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

expect object AppConfig {
    val dataSaver: DataSaverInterface
}

internal fun registerAllTypeConverters() {
    // cause we want to save custom bean, we provide a converter to convert it into String
    DataSaverConverter.registerTypeConverters<ExampleBean?>(
        save = { bean -> Json.encodeToString(bean) },
        restore = { str ->
            Log.d("AppConfig", "restore ExampleBean? from string: $str")
            Json.decodeFromString(str)
        }
    )

    DataSaverConverter.registerTypeConverters<ThemeType>(
        save = ThemeType.Saver,
        restore = ThemeType.Restorer
    )

    DataSaverConverter.registerTypeConverters<List<ExampleBean>>(
        save = { list -> Json.encodeToString(list) },
        restore = { str -> Json.decodeFromString(str) }
    )
}