package com.funny.data_saver

import com.funny.data_saver.core.DataSaverInterface
import com.funny.data_saver_mmkv.DefaultDataSaverMMKV

actual object AppConfig {
    init {
        registerAllTypeConverters()
    }

    actual val dataSaver: DataSaverInterface = DefaultDataSaverMMKV
}