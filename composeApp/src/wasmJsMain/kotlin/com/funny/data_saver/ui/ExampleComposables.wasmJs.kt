package com.funny.data_saver.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalInspectionMode
import com.funny.data_saver.core.DataSaverInMemory
import com.funny.data_saver.core.DataSaverInterface
import com.funny.data_saver.core.DataSaverLocalStorage
import com.funny.data_saver.kmp.LoggerImpl.d

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
    else sensorExternalLocalStorage
}

private val sensorExternalLocalStorage by lazy {
    DataSaverLocalStorage(senseExternalDataChange = true)
}

@OptIn(ExperimentalWasmJsInterop::class)
internal actual fun currentLogTimeText(): String = js(
    """(() => {
        const d = new Date();
        const pad = (n, w = 2) => String(n).padStart(w, '0');
        return `${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}.${pad(d.getMilliseconds(), 3)}`;
    })()"""
) as String
