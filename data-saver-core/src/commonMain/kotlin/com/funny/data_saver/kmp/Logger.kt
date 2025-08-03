package com.funny.data_saver.kmp

import com.funny.data_saver.core.DataSaverConfig

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

val Log get() = DataSaverConfig.logger