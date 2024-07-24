package com.funny.data_saver

import com.funny.data_saver.core.DataSaverInterface
import com.funny.data_saver.core.DataSaverProperties

actual object AppConfig {
    init {
        registerAllTypeConverters()
    }

    private val userHome = System.getProperty("user.home")
    private const val projectName = "ComposeDataSaver"

    // File Path Example(Windows): C:/Users/username/ComposeDataSaver/data_saver.properties
    actual val dataSaver: DataSaverInterface = DataSaverProperties("$userHome/$projectName/data_saver.properties")
        // DataSaverEncryptedProperties("$userHome/$projectName/data_saver_encrypted.properties", "FunnySaltyFish")
}