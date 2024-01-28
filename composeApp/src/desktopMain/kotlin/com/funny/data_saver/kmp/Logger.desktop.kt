package com.funny.data_saver.kmp

enum class LogLevel {
    VERBOSE,
    DEBUG,
    INFO,
    WARNING,
    ERROR,
    WTF
}

actual object Logger {
    private fun log(level: LogLevel, tag: String = "DefaultLog", msg: String = "", throwable: Throwable? = null) {
        println("[${level.name}] $tag: $msg")
        throwable?.printStackTrace()
    }

    actual fun d(msg: String) {
        log(LogLevel.DEBUG, msg = msg)
    }

    actual fun d(tag: String, msg: String) {
        log(LogLevel.DEBUG, tag, msg)
    }

    actual fun d(tag: String, msg: String, throwable: Throwable) {
        log(LogLevel.DEBUG, tag, msg, throwable)
    }

    actual fun i(msg: String) {
        log(LogLevel.INFO, msg = msg)
    }

    actual fun i(tag: String, msg: String) {
        log(LogLevel.INFO, tag, msg)
    }

    actual fun i(tag: String, msg: String, throwable: Throwable) {
        log(LogLevel.INFO, tag, msg, throwable)
    }

    actual fun e(msg: String) {
        log(LogLevel.ERROR, msg = msg)
    }

    actual fun e(tag: String, msg: String) {
        log(LogLevel.ERROR, tag, msg)
    }

    actual fun e(tag: String, msg: String, throwable: Throwable) {
        log(LogLevel.ERROR, tag, msg, throwable)
    }

    actual fun w(msg: String) {
        log(LogLevel.WARNING, msg = msg)
    }

    actual fun w(tag: String, msg: String) {
        log(LogLevel.WARNING, tag, msg)
    }

    actual fun w(tag: String, msg: String, throwable: Throwable) {
        log(LogLevel.WARNING, tag, msg, throwable)
    }


    actual fun wtf(msg: String) {
        log(LogLevel.WTF, msg = msg)
    }

    actual fun wtf(tag: String, msg: String) {
        log(LogLevel.WTF, tag, msg)
    }

    actual fun wtf(tag: String, msg: String, throwable: Throwable) {
        log(LogLevel.WTF, tag, msg, throwable)
    }

    actual fun v(msg: String) {
        log(LogLevel.VERBOSE, msg = msg)
    }

    actual fun v(tag: String, msg: String) {
        log(LogLevel.VERBOSE, tag, msg)
    }

    actual fun v(tag: String, msg: String, throwable: Throwable) {
        log(LogLevel.VERBOSE, tag, msg, throwable)
    }

}