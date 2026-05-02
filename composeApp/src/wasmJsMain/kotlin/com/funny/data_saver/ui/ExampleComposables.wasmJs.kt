package com.funny.data_saver.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalInspectionMode
import com.funny.data_saver.core.DataSaverInMemory
import com.funny.data_saver.core.DataSaverInterface
import com.funny.data_saver.core.DataSaverLocalStorage
import composedatasaver.composeapp.generated.resources.Res
import composedatasaver.composeapp.generated.resources.saving_parcelable_title
import composedatasaver.composeapp.generated.resources.saving_parcelable_unsupported_description
import composedatasaver.composeapp.generated.resources.saving_parcelable_unsupported_message
import org.jetbrains.compose.resources.stringResource

@Composable
actual fun ParcelableExample() {
    ExampleCard(
        title = stringResource(Res.string.saving_parcelable_title),
        description = stringResource(Res.string.saving_parcelable_unsupported_description)
    ) {
        Text(stringResource(Res.string.saving_parcelable_unsupported_message))
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
internal actual fun currentLogTimeText(): String =
    "${currentHour().padTimePart()}:${currentMinute().padTimePart()}:${currentSecond().padTimePart()}.${currentMillisecond().padTimePart(3)}"

@OptIn(ExperimentalWasmJsInterop::class)
private fun currentHour(): Int = js("new Date().getHours()")

@OptIn(ExperimentalWasmJsInterop::class)
private fun currentMinute(): Int = js("new Date().getMinutes()")

@OptIn(ExperimentalWasmJsInterop::class)
private fun currentSecond(): Int = js("new Date().getSeconds()")

@OptIn(ExperimentalWasmJsInterop::class)
private fun currentMillisecond(): Int = js("new Date().getMilliseconds()")

private fun Int.padTimePart(width: Int = 2): String = toString().padStart(width, '0')
