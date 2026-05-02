package com.funny.data_saver.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import com.funny.data_saver.core.DataSaverInMemory
import com.funny.data_saver.core.DataSaverInterface
import composedatasaver.composeapp.generated.resources.Res
import composedatasaver.composeapp.generated.resources.saving_parcelable_title
import composedatasaver.composeapp.generated.resources.saving_parcelable_unsupported_description
import composedatasaver.composeapp.generated.resources.saving_parcelable_unsupported_message
import org.jetbrains.compose.resources.stringResource
import java.time.LocalTime
import java.time.format.DateTimeFormatter

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
    return DataSaverInMemory(true)
}

private val logTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")

internal actual fun currentLogTimeText(): String = LocalTime.now().format(logTimeFormatter)
