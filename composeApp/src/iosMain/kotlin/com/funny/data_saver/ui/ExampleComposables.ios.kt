package com.funny.data_saver.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalInspectionMode
import com.funny.data_saver.core.DataSaverInMemory
import com.funny.data_saver.core.DataSaverInterface
import com.funny.data_saver.core.DataSaverNSUserDefaults
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter

@Composable
actual fun ParcelableExample() {
    ExampleCard(
        title = "Saving Parcelable",
        description = "当前平台不提供 Parcelable 持久化示例。"
    ) {
        Text("如需体验 Parcelable 持久化，请在 Android 端运行。")
    }
}

@ReadOnlyComposable
@Composable
internal actual fun getSensorExternalDataSaver(): DataSaverInterface {
    return if (LocalInspectionMode.current)
        DataSaverInMemory(true)
    else sensorExternalNS
}

private val sensorExternalNS by lazy {
    DataSaverNSUserDefaults(senseExternalDataChange = true)
}

private val logTimeFormatter by lazy {
    NSDateFormatter().apply {
        dateFormat = "HH:mm:ss.SSS"
    }
}

internal actual fun currentLogTimeText(): String = logTimeFormatter.stringFromDate(NSDate())
