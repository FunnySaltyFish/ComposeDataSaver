package com.funny.data_saver

import com.funny.data_saver.core.DataSaverInterface

actual object AppConfig {
    init {
        registerAllTypeConverters()
    }

    actual val dataSaver: DataSaverInterface
        get() = TODO("Not yet implemented")
}