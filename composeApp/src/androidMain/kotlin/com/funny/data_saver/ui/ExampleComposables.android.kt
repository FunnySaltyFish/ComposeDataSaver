package com.funny.data_saver.ui

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalInspectionMode
import com.funny.data_saver.ExampleParcelable
import com.funny.data_saver.core.DataSaverInMemory
import com.funny.data_saver.core.DataSaverInterface
import com.funny.data_saver.core.rememberDataSaverState
import com.funny.data_saver_mmkv.DataSaverMMKV
import com.tencent.mmkv.MMKV

@Composable
actual fun ParcelableExample() {
    // Among our basic implementations, only MMKV supports `Parcelable` by default
    var parcelableExample by rememberDataSaverState(
        key = "parcelable_example",
        initialValue = ExampleParcelable("FunnySaltyFish", 20)
    )
    Heading(text = "Saving Parcelable") // 保存布尔值的示例
    Text(parcelableExample.toString())
    Button(onClick = {
        parcelableExample = parcelableExample.copy(age = parcelableExample.age + 1)
    }) {
        Text(text = "Add age by 1")
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