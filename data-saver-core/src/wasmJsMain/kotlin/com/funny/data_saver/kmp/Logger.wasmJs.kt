package com.funny.data_saver.kmp

actual object LoggerImpl: Logger {
    private const val TAG = "DefaultLog"
    
    // WASM console.log 样式颜色
    private object ConsoleColors {
        const val DEBUG = "color: #2196F3"
        const val INFO = "color: #4CAF50"
        const val WARNING = "color: #FF9800"
        const val ERROR = "color: #F44336"
        const val VERBOSE = "color: #9C27B0"
        const val WTF = "color: #F44336; font-weight: bold"
    }

    actual override fun d(msg: String) { consoleLog("DEBUG", ConsoleColors.DEBUG, TAG, msg) }
    actual override fun d(tag: String, msg: String) { consoleLog("DEBUG", ConsoleColors.DEBUG, tag, msg) }
    actual override fun d(tag: String, msg: String, throwable: Throwable) { consoleLog("DEBUG", ConsoleColors.DEBUG, tag, msg, throwable) }

    actual override fun i(msg: String) { consoleLog("INFO", ConsoleColors.INFO, TAG, msg) }
    actual override fun i(tag: String, msg: String) { consoleLog("INFO", ConsoleColors.INFO, tag, msg) }
    actual override fun i(tag: String, msg: String, throwable: Throwable) { consoleLog("INFO", ConsoleColors.INFO, tag, msg, throwable) }

    actual override fun w(msg: String) { consoleLog("WARNING", ConsoleColors.WARNING, TAG, msg) }
    actual override fun w(tag: String, msg: String) { consoleLog("WARNING", ConsoleColors.WARNING, tag, msg) }
    actual override fun w(tag: String, msg: String, throwable: Throwable) { consoleLog("WARNING", ConsoleColors.WARNING, tag, msg, throwable) }

    actual override fun e(msg: String) { consoleLog("ERROR", ConsoleColors.ERROR, TAG, msg) }
    actual override fun e(tag: String, msg: String) { consoleLog("ERROR", ConsoleColors.ERROR, tag, msg) }
    actual override fun e(tag: String, msg: String, throwable: Throwable) { consoleLog("ERROR", ConsoleColors.ERROR, tag, msg, throwable) }

    actual override fun v(msg: String) { consoleLog("VERBOSE", ConsoleColors.VERBOSE, TAG, msg) }
    actual override fun v(tag: String, msg: String) { consoleLog("VERBOSE", ConsoleColors.VERBOSE, tag, msg) }
    actual override fun v(tag: String, msg: String, throwable: Throwable) { consoleLog("VERBOSE", ConsoleColors.VERBOSE, tag, msg, throwable) }

    actual override fun wtf(msg: String) { consoleLog("WTF", ConsoleColors.WTF, TAG, msg) }
    actual override fun wtf(tag: String, msg: String) { consoleLog("WTF", ConsoleColors.WTF, tag, msg) }
    actual override fun wtf(tag: String, msg: String, throwable: Throwable) { consoleLog("WTF", ConsoleColors.WTF, tag, msg, throwable) }
}

private fun consoleLog(level: String, color: String, tag: String, msg: String, throwable: Throwable? = null) {
    if (throwable == null) {
        logNormal(level, color, tag, msg)
    } else {
        logThrowable(level, tag, msg, throwable.stackTraceToString())
    }
}

private fun logNormal(level: String, color: String, tag: String, msg: String) {
    js("console.log(`%c[\${level}] \${tag}: \${msg}`, color)")
}

private fun logThrowable(level: String, tag: String, msg: String, throwableStr: String) {
    js("console.error('[\${level}] \${tag}: \${msg}', throwableStr)")
}