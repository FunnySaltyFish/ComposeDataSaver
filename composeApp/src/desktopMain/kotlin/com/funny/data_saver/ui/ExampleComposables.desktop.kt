package com.funny.data_saver.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import com.funny.data_saver.core.DataSaverInMemory
import com.funny.data_saver.core.DataSaverInterface
import java.time.LocalTime
import java.time.format.DateTimeFormatter

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
    return DataSaverInMemory(true)
}

private val logTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")

internal actual fun currentLogTimeText(): String = LocalTime.now().format(logTimeFormatter)
