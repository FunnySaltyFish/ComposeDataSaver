package com.funny.data_saver.core

import com.funny.data_saver.kmp.Logger
import com.funny.data_saver.kmp.LoggerImpl

/**
 * Some config that you can set:
 * 1. logLevel: control which logs should be printed
 * 2. logger: custom logger
 */
object DataSaverConfig {
    var logLevel: DataSaverLogLevel = DataSaverLogLevel.INFO
    var logger: Logger = LoggerImpl

    @Deprecated(
        message = "Use logLevel instead.",
        replaceWith = ReplaceWith("logLevel")
    )
    var DEBUG: Boolean
        get() = logLevel.allows(DataSaverLogLevel.DEBUG)
        set(value) {
            logLevel = if (value) DataSaverLogLevel.DEBUG else DataSaverLogLevel.NONE
        }

    internal fun shouldLog(level: DataSaverLogLevel): Boolean = logLevel.allows(level)
}

