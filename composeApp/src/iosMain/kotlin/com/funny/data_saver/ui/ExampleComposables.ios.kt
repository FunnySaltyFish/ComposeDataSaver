package com.funny.data_saver.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalInspectionMode
import com.funny.data_saver.core.DataSaverInMemory
import com.funny.data_saver.core.DataSaverInterface
import com.funny.data_saver.core.DataSaverNSUserDefaults
import composedatasaver.composeapp.generated.resources.Res
import composedatasaver.composeapp.generated.resources.saving_parcelable_title
import composedatasaver.composeapp.generated.resources.saving_parcelable_unsupported_description
import composedatasaver.composeapp.generated.resources.saving_parcelable_unsupported_message
import org.jetbrains.compose.resources.stringResource
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter

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
