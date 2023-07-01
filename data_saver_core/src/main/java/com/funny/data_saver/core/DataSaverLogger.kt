package com.funny.data_saver.core

import android.util.Log

class DataSaverLogger(private val tag: String) {
    fun d(msg: String) {
        if (DataSaverConfig.DEBUG) Log.d(tag, msg)
    }
    fun w(msg: String) {
        if (DataSaverConfig.DEBUG) Log.w(tag, msg)
    }

    companion object {
        private const val TAG = "ComposeDataSaver"
        private val logger by lazy(LazyThreadSafetyMode.PUBLICATION) {
            DataSaverLogger(TAG)
        }

        fun log(msg: String) = ::d
        fun d(msg: String) { logger.d(msg) }
        fun w(msg: String) { logger.w(msg) }
    }
}