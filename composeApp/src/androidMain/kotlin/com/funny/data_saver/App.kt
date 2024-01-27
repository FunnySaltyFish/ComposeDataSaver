package com.funny.composedatasaver

import android.app.Application
import com.tencent.mmkv.MMKV
import kotlin.properties.Delegates

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        ctx = this
        // If you want to use [MMKV](https://github.com/Tencent/MMKV) to save data
        MMKV.initialize(ctx)
    }

    companion object {
        var ctx by Delegates.notNull<App>()
    }
}

val appCtx = App.ctx