package com.funny.data_saver

import com.funny.data_saver.core.DataSaverInterface
import com.funny.data_saver.core.DataSaverProperties

actual object AppConfig {
    init {
        registerAllTypeConverters()
    }

    private const val filename = "data_saver.properties"
    private val userHome = System.getProperty("user.home")
    private const val projectName = "ComposeDataSaver"


    actual val dataSaver: DataSaverInterface = DataSaverProperties("$userHome/$projectName/$filename")
}