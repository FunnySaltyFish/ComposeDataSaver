package com.funny.data_saver.core

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

enum class DataSaverLogLevel(val priority: Int) {
    NONE(0),
    ERROR(1),
    WARNING(2),
    INFO(3),
    DEBUG(4),
    VERBOSE(5);

    fun allows(level: DataSaverLogLevel): Boolean {
        return level != NONE && priority >= level.priority
    }
}

data class DataSaverLogEntry(
    val level: DataSaverLogLevel,
    val tag: String,
    val message: String
)

object DataSaverLogs {
    private const val BUFFER_SIZE = 64

    private val _entries = MutableSharedFlow<DataSaverLogEntry>(
        replay = BUFFER_SIZE,
        extraBufferCapacity = BUFFER_SIZE
    )

    val entries = _entries.asSharedFlow()

    internal fun emit(level: DataSaverLogLevel, tag: String, message: String) {
        _entries.tryEmit(DataSaverLogEntry(level, tag, message))
    }
}
