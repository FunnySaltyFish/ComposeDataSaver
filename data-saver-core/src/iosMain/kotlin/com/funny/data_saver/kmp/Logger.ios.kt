package com.funny.data_saver.kmp

import platform.Foundation.NSLog

actual object LoggerImpl: Logger {
    private const val TAG = "DefaultLog"
    
    private fun nsLog(level: String, tag: String, msg: String, throwable: Throwable? = null) {
        NSLog("[$level] $tag: $msg")
        throwable?.let { 
            NSLog("[$level] $tag: ${it.stackTraceToString()}")
        }
    }

    actual override fun d(msg: String) { nsLog("DEBUG", TAG, msg) }
    actual override fun d(tag: String, msg: String) { nsLog("DEBUG", tag, msg) }
    actual override fun d(tag: String, msg: String, throwable: Throwable) { nsLog("DEBUG", tag, msg, throwable) }

    actual override fun i(msg: String) { nsLog("INFO", TAG, msg) }
    actual override fun i(tag: String, msg: String) { nsLog("INFO", tag, msg) }
    actual override fun i(tag: String, msg: String, throwable: Throwable) { nsLog("INFO", tag, msg, throwable) }

    actual override fun w(msg: String) { nsLog("WARNING", TAG, msg) }
    actual override fun w(tag: String, msg: String) { nsLog("WARNING", tag, msg) }
    actual override fun w(tag: String, msg: String, throwable: Throwable) { nsLog("WARNING", tag, msg, throwable) }

    actual override fun e(msg: String) { nsLog("ERROR", TAG, msg) }
    actual override fun e(tag: String, msg: String) { nsLog("ERROR", tag, msg) }
    actual override fun e(tag: String, msg: String, throwable: Throwable) { nsLog("ERROR", tag, msg, throwable) }

    actual override fun v(msg: String) { nsLog("VERBOSE", TAG, msg) }
    actual override fun v(tag: String, msg: String) { nsLog("VERBOSE", tag, msg) }
    actual override fun v(tag: String, msg: String, throwable: Throwable) { nsLog("VERBOSE", tag, msg, throwable) }

    actual override fun wtf(msg: String) { nsLog("WTF", TAG, msg) }
    actual override fun wtf(tag: String, msg: String) { nsLog("WTF", tag, msg) }
    actual override fun wtf(tag: String, msg: String, throwable: Throwable) { nsLog("WTF", tag, msg, throwable) }
} 