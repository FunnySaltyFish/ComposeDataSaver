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

    override fun d(msg: String) {
        log(LogLevel.DEBUG, msg = msg)
    }

    override fun d(tag: String, msg: String) {
        log(LogLevel.DEBUG, tag, msg)
    }

    override fun d(tag: String, msg: String, throwable: Throwable) {
        log(LogLevel.DEBUG, tag, msg, throwable)
    }

    override fun i(msg: String) {
        log(LogLevel.INFO, msg = msg)
    }

    override fun i(tag: String, msg: String) {
        log(LogLevel.INFO, tag, msg)
    }

    override fun i(tag: String, msg: String, throwable: Throwable) {
        log(LogLevel.INFO, tag, msg, throwable)
    }

    override fun e(msg: String) {
        log(LogLevel.ERROR, msg = msg)
    }

    override fun e(tag: String, msg: String) {
        log(LogLevel.ERROR, tag, msg)
    }

    override fun e(tag: String, msg: String, throwable: Throwable) {
        log(LogLevel.ERROR, tag, msg, throwable)
    }

    override fun w(msg: String) {
        log(LogLevel.WARNING, msg = msg)
    }

    override fun w(tag: String, msg: String) {
        log(LogLevel.WARNING, tag, msg)
    }

    override fun w(tag: String, msg: String, throwable: Throwable) {
        log(LogLevel.WARNING, tag, msg, throwable)
    }


    override fun wtf(msg: String) {
        log(LogLevel.WTF, msg = msg)
    }

    override fun wtf(tag: String, msg: String) {
        log(LogLevel.WTF, tag, msg)
    }

    override fun wtf(tag: String, msg: String, throwable: Throwable) {
        log(LogLevel.WTF, tag, msg, throwable)
    }

    override fun v(msg: String) {
        log(LogLevel.VERBOSE, msg = msg)
    }

    override fun v(tag: String, msg: String) {
        log(LogLevel.VERBOSE, tag, msg)
    }

    override fun v(tag: String, msg: String, throwable: Throwable) {
        log(LogLevel.VERBOSE, tag, msg, throwable)
    }

}