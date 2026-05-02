package com.funny.data_saver.ui

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalInspectionMode
import com.funny.data_saver.ExampleParcelable
import com.funny.data_saver.core.DataSaverInMemory
import com.funny.data_saver.core.DataSaverInterface
import com.funny.data_saver.core.rememberDataSaverState
import com.funny.data_saver_mmkv.DataSaverMMKV
import com.tencent.mmkv.MMKV
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
actual fun ParcelableExample() {
    ExampleCard(
        title = "Saving Parcelable",
        description = "基础实现里只有 MMKV 默认支持 Parcelable。"
    ) {
        var parcelableExample by rememberDataSaverState(
            key = "parcelable_example",
            initialValue = ExampleParcelable("FunnySaltyFish", 20)
        )
        Text(parcelableExample.toString())
        Button(onClick = {
            parcelableExample = parcelableExample.copy(age = parcelableExample.age + 1)
        }) {
            Text(text = "Add age by 1")
        }
    }
}

@ReadOnlyComposable
@Composable
internal actual fun getSensorExternalDataSaver(): DataSaverInterface {
    return if (LocalInspectionMode.current)
        DataSaverInMemory(true)
    else sensorExternalMMKV
}

private val sensorExternalMMKV by lazy {
    DataSaverMMKV(MMKV.defaultMMKV(), true)
}

private val logTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")

internal actual fun currentLogTimeText(): String = LocalTime.now().format(logTimeFormatter)
