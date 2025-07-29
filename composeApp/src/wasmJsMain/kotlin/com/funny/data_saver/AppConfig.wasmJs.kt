package com.funny.data_saver

import com.funny.data_saver.core.DataSaverInterface
import com.funny.data_saver.core.DataSaverLocalStorage

actual object AppConfig {
    init {
        registerAllTypeConverters()
    }

    actual val dataSaver: DataSaverInterface = DataSaverLocalStorage()
}