package com.funny.data_saver.kmp

import com.funny.data_saver.core.DataSaverConfig
import com.funny.data_saver.core.DataSaverLogLevel
import com.funny.data_saver.core.DataSaverLogs

interface Logger {
    fun d(msg: String) : Unit
    fun d(tag: String, msg: String) : Unit
    fun d(tag: String, msg: String, throwable: Throwable) : Unit

    fun i(msg: String) : Unit
    fun i(tag: String, msg: String) : Unit
    fun i(tag: String, msg: String, throwable: Throwable) : Unit

    fun e(msg: String) : Unit
    fun e(tag: String, msg: String) : Unit
    fun e(tag: String, msg: String, throwable: Throwable) : Unit

    fun w(msg: String) : Unit
    fun w(tag: String, msg: String) : Unit
    fun w(tag: String, msg: String, throwable: Throwable) : Unit

    fun v(msg: String) : Unit
    fun v(tag: String, msg: String) : Unit
    fun v(tag: String, msg: String, throwable: Throwable) : Unit

    fun wtf(msg: String) : Unit
    fun wtf(tag: String, msg: String) : Unit
    fun wtf(tag: String, msg: String, throwable: Throwable) : Unit
}

expect object LoggerImpl: Logger {
    override fun d(msg: String)
    override fun d(tag: String, msg: String)
    override fun d(tag: String, msg: String, throwable: Throwable)

    override fun i(msg: String)
    override fun i(tag: String, msg: String)
    override fun i(tag: String, msg: String, throwable: Throwable)

    override fun e(msg: String)
    override fun e(tag: String, msg: String)
    override fun e(tag: String, msg: String, throwable: Throwable)

    override fun w(msg: String)
    override fun w(tag: String, msg: String)
    override fun w(tag: String, msg: String, throwable: Throwable)

    override fun v(msg: String)
    override fun v(tag: String, msg: String)
    override fun v(tag: String, msg: String, throwable: Throwable)

    override fun wtf(msg: String)
    override fun wtf(tag: String, msg: String)
    override fun wtf(tag: String, msg: String, throwable: Throwable)
}

private object DataSaverLogProxy : Logger {
    private const val DEFAULT_TAG = "ComposeDataSaver"

    private inline fun dispatch(
        level: DataSaverLogLevel,
        tag: String,
        msg: String,
        throwable: Throwable? = null,
        block: (Logger) -> Unit
    ) {
        if (!DataSaverConfig.shouldLog(level)) return
        val detail = throwable?.let { error ->
            val type = error::class.simpleName ?: "Throwable"
            "$type: ${error.message.orEmpty()}"
        }
        val renderedMessage = if (detail.isNullOrBlank()) msg else "$msg\n$detail"
        DataSaverLogs.emit(level, tag, renderedMessage)
        val delegate = DataSaverConfig.logger.takeUnless { it === this } ?: LoggerImpl
        block(delegate)
    }

    override fun d(msg: String) = d(DEFAULT_TAG, msg)
    override fun d(tag: String, msg: String) = dispatch(DataSaverLogLevel.DEBUG, tag, msg) { it.d(tag, msg) }
    override fun d(tag: String, msg: String, throwable: Throwable) =
        dispatch(DataSaverLogLevel.DEBUG, tag, msg, throwable) { it.d(tag, msg, throwable) }

    override fun i(msg: String) = i(DEFAULT_TAG, msg)
    override fun i(tag: String, msg: String) = dispatch(DataSaverLogLevel.INFO, tag, msg) { it.i(tag, msg) }
    override fun i(tag: String, msg: String, throwable: Throwable) =
        dispatch(DataSaverLogLevel.INFO, tag, msg, throwable) { it.i(tag, msg, throwable) }

    override fun e(msg: String) = e(DEFAULT_TAG, msg)
    override fun e(tag: String, msg: String) = dispatch(DataSaverLogLevel.ERROR, tag, msg) { it.e(tag, msg) }
    override fun e(tag: String, msg: String, throwable: Throwable) =
        dispatch(DataSaverLogLevel.ERROR, tag, msg, throwable) { it.e(tag, msg, throwable) }

    override fun w(msg: String) = w(DEFAULT_TAG, msg)
    override fun w(tag: String, msg: String) = dispatch(DataSaverLogLevel.WARNING, tag, msg) { it.w(tag, msg) }
    override fun w(tag: String, msg: String, throwable: Throwable) =
        dispatch(DataSaverLogLevel.WARNING, tag, msg, throwable) { it.w(tag, msg, throwable) }

    override fun v(msg: String) = v(DEFAULT_TAG, msg)
    override fun v(tag: String, msg: String) = dispatch(DataSaverLogLevel.VERBOSE, tag, msg) { it.v(tag, msg) }
    override fun v(tag: String, msg: String, throwable: Throwable) =
        dispatch(DataSaverLogLevel.VERBOSE, tag, msg, throwable) { it.v(tag, msg, throwable) }

    override fun wtf(msg: String) = wtf(DEFAULT_TAG, msg)
    override fun wtf(tag: String, msg: String) = dispatch(DataSaverLogLevel.ERROR, tag, msg) { it.wtf(tag, msg) }
    override fun wtf(tag: String, msg: String, throwable: Throwable) =
        dispatch(DataSaverLogLevel.ERROR, tag, msg, throwable) { it.wtf(tag, msg, throwable) }
}

val Log: Logger = DataSaverLogProxy
