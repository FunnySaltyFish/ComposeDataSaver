package com.funny.data_saver.kmp

import android.util.Log as AndroidLog

actual object LoggerImpl: Logger {
    private const val TAG = "DefaultLog"
    // i, d, w, e, v
    override fun d(msg: String) { AndroidLog.d(TAG, msg) }
    override fun d(tag: String, msg: String) { AndroidLog.d(tag, msg) }
    override fun d(tag: String, msg: String, throwable: Throwable) { AndroidLog.d(tag, msg, throwable) }

    override fun i(msg: String) { AndroidLog.i(TAG, msg) }
    override fun i(tag: String, msg: String) { AndroidLog.i(tag, msg) }
    override fun i(tag: String, msg: String, throwable: Throwable) { AndroidLog.i(tag, msg, throwable) }

    override fun w(msg: String) { AndroidLog.w(TAG, msg) }
    override fun w(tag: String, msg: String) { AndroidLog.w(tag, msg) }
    override fun w(tag: String, msg: String, throwable: Throwable) { AndroidLog.w(tag, msg, throwable) }

    override fun e(msg: String) { AndroidLog.e(TAG, msg) }
    override fun e(tag: String, msg: String) { AndroidLog.e(tag, msg) }
    override fun e(tag: String, msg: String, throwable: Throwable) { AndroidLog.e(tag, msg, throwable) }

    override fun v(msg: String) { AndroidLog.v(TAG, msg) }
    override fun v(tag: String, msg: String) { AndroidLog.v(tag, msg) }
    override fun v(tag: String, msg: String, throwable: Throwable) { AndroidLog.v(tag, msg, throwable) }

    // wtf
    override fun wtf(msg: String) { AndroidLog.wtf(TAG, msg) }
    override fun wtf(tag: String, msg: String) { AndroidLog.wtf(tag, msg) }
    override fun wtf(tag: String, msg: String, throwable: Throwable) { AndroidLog.wtf(tag, msg, throwable) }
}