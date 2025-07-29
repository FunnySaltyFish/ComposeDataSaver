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

    override fun d(msg: String) { nsLog("DEBUG", TAG, msg) }
    override fun d(tag: String, msg: String) { nsLog("DEBUG", tag, msg) }
    override fun d(tag: String, msg: String, throwable: Throwable) { nsLog("DEBUG", tag, msg, throwable) }

    override fun i(msg: String) { nsLog("INFO", TAG, msg) }
    override fun i(tag: String, msg: String) { nsLog("INFO", tag, msg) }
    override fun i(tag: String, msg: String, throwable: Throwable) { nsLog("INFO", tag, msg, throwable) }

    override fun w(msg: String) { nsLog("WARNING", TAG, msg) }
    override fun w(tag: String, msg: String) { nsLog("WARNING", tag, msg) }
    override fun w(tag: String, msg: String, throwable: Throwable) { nsLog("WARNING", tag, msg, throwable) }

    override fun e(msg: String) { nsLog("ERROR", TAG, msg) }
    override fun e(tag: String, msg: String) { nsLog("ERROR", tag, msg) }
    override fun e(tag: String, msg: String, throwable: Throwable) { nsLog("ERROR", tag, msg, throwable) }

    override fun v(msg: String) { nsLog("VERBOSE", TAG, msg) }
    override fun v(tag: String, msg: String) { nsLog("VERBOSE", tag, msg) }
    override fun v(tag: String, msg: String, throwable: Throwable) { nsLog("VERBOSE", tag, msg, throwable) }

    override fun wtf(msg: String) { nsLog("WTF", TAG, msg) }
    override fun wtf(tag: String, msg: String) { nsLog("WTF", tag, msg) }
    override fun wtf(tag: String, msg: String, throwable: Throwable) { nsLog("WTF", tag, msg, throwable) }
} 