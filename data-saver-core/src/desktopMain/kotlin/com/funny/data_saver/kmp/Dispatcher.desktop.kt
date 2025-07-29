package com.funny.data_saver.kmp

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO as DesktopIO

actual val Dispatchers.IO: CoroutineDispatcher
    get() = DesktopIO