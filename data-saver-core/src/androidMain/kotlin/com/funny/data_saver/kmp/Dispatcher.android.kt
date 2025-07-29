package com.funny.data_saver.kmp

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO as AndroidIO

actual val Dispatchers.IO: CoroutineDispatcher
    get() = AndroidIO