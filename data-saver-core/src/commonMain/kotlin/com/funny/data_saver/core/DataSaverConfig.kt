package com.funny.data_saver.core

import com.funny.data_saver.kmp.Logger
import com.funny.data_saver.kmp.LoggerImpl

/**
 * Some config that you can set:
 * 1. DEBUG: whether to output some debug info
 * 2. logger: custom logger
 */
object DataSaverConfig {
    var DEBUG = true
    var logger: Logger = LoggerImpl
}

