package com.funny.data_saver.kmp

import android.util.Log as AndroidLog

actual object LoggerImpl: Logger {
    private const val TAG = "DefaultLog"
    // i, d, w, e, v
    actual override fun d(msg: String) { AndroidLog.d(TAG, msg) }
    actual override fun d(tag: String, msg: String) { AndroidLog.d(tag, msg) }
    actual override fun d(tag: String, msg: String, throwable: Throwable) { AndroidLog.d(tag, msg, throwable) }

    actual override fun i(msg: String) { AndroidLog.i(TAG, msg) }
    actual override fun i(tag: String, msg: String) { AndroidLog.i(tag, msg) }
    actual override fun i(tag: String, msg: String, throwable: Throwable) { AndroidLog.i(tag, msg, throwable) }

    actual override fun w(msg: String) { AndroidLog.w(TAG, msg) }
    actual override fun w(tag: String, msg: String) { AndroidLog.w(tag, msg) }
    actual override fun w(tag: String, msg: String, throwable: Throwable) { AndroidLog.w(tag, msg, throwable) }

    actual override fun e(msg: String) { AndroidLog.e(TAG, msg) }
    actual override fun e(tag: String, msg: String) { AndroidLog.e(tag, msg) }
    actual override fun e(tag: String, msg: String, throwable: Throwable) { AndroidLog.e(tag, msg, throwable) }

    actual override fun v(msg: String) { AndroidLog.v(TAG, msg) }
    actual override fun v(tag: String, msg: String) { AndroidLog.v(tag, msg) }
    actual override fun v(tag: String, msg: String, throwable: Throwable) { AndroidLog.v(tag, msg, throwable) }

    // wtf
    actual override fun wtf(msg: String) { AndroidLog.wtf(TAG, msg) }
    actual override fun wtf(tag: String, msg: String) { AndroidLog.wtf(tag, msg) }
    actual override fun wtf(tag: String, msg: String, throwable: Throwable) { AndroidLog.wtf(tag, msg, throwable) }
}