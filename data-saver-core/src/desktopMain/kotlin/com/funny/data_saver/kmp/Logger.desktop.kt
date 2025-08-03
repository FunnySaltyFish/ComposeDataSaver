package com.funny.data_saver.kmp

// 加颜色
enum class LogLevel(val color: String) {
    VERBOSE("\u001B[34m"),
    DEBUG("\u001B[36m"),
    INFO("\u001B[32m"),
    WARNING("\u001B[33m"),
    ERROR("\u001B[31m"),
    WTF("\u001B[31m");
}

actual object LoggerImpl: Logger {
    private fun log(level: LogLevel, tag: String = "DefaultLog", msg: String = "", throwable: Throwable? = null) {
        // println("[${level.name}] $tag: $msg")
        println("${level.color} [${level.name}] $tag: $msg \u001B[0m")
        throwable?.printStackTrace()
    }

    actual override fun d(msg: String) {
        log(LogLevel.DEBUG, msg = msg)
    }

    actual override fun d(tag: String, msg: String) {
        log(LogLevel.DEBUG, tag, msg)
    }

    actual override fun d(tag: String, msg: String, throwable: Throwable) {
        log(LogLevel.DEBUG, tag, msg, throwable)
    }

    actual override fun i(msg: String) {
        log(LogLevel.INFO, msg = msg)
    }

    actual override fun i(tag: String, msg: String) {
        log(LogLevel.INFO, tag, msg)
    }

    actual override fun i(tag: String, msg: String, throwable: Throwable) {
        log(LogLevel.INFO, tag, msg, throwable)
    }

    actual override fun e(msg: String) {
        log(LogLevel.ERROR, msg = msg)
    }

    actual override fun e(tag: String, msg: String) {
        log(LogLevel.ERROR, tag, msg)
    }

    actual override fun e(tag: String, msg: String, throwable: Throwable) {
        log(LogLevel.ERROR, tag, msg, throwable)
    }

    actual override fun w(msg: String) {
        log(LogLevel.WARNING, msg = msg)
    }

    actual override fun w(tag: String, msg: String) {
        log(LogLevel.WARNING, tag, msg)
    }

    actual override fun w(tag: String, msg: String, throwable: Throwable) {
        log(LogLevel.WARNING, tag, msg, throwable)
    }


    actual override fun wtf(msg: String) {
        log(LogLevel.WTF, msg = msg)
    }

    actual override fun wtf(tag: String, msg: String) {
        log(LogLevel.WTF, tag, msg)
    }

    actual override fun wtf(tag: String, msg: String, throwable: Throwable) {
        log(LogLevel.WTF, tag, msg, throwable)
    }

    actual override fun v(msg: String) {
        log(LogLevel.VERBOSE, msg = msg)
    }

    actual override fun v(tag: String, msg: String) {
        log(LogLevel.VERBOSE, tag, msg)
    }

    actual override fun v(tag: String, msg: String, throwable: Throwable) {
        log(LogLevel.VERBOSE, tag, msg, throwable)
    }

}