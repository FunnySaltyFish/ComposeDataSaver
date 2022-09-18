package com.funny.data_saver.core

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * The interface is used to save/read data. We provide the basic implementation using Preference, DataStore and MMKV.
 *
 * If you want to write your own, you need to implement `saveData` and `readData`. Besides, a suspend function `saveDataAsync` is optional(which is equal to `saveData` by default)
 */
interface DataSaverInterface{
    fun <T> saveData(key:String, data : T)
    fun <T> readData(key: String, default : T) : T
    suspend fun <T> saveDataAsync(key:String, data : T) = saveData(key, data)
}

/**
 * Default implementation using [SharedPreferences] to save data
 */
class DataSaverPreferences : DataSaverInterface {
    companion object {
        lateinit var preference: SharedPreferences

        /**
         * this method should be called to do initialization
         * @param context Context
         */
        fun setContext(context: Context) {
            preference = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
        }
    }


    private fun <T> getPreference(name: String, default: T): T = with(preference) {
        val res: Any = when (default) {
            is Long -> getLong(name, default)
            is String -> this.getString(name, default)!!
            is Int -> getInt(name, default)
            is Boolean -> getBoolean(name, default)
            is Float -> getFloat(name, default)
            else -> throw IllegalArgumentException("Unable to read $default, this type(${if(default==null)null else default!!::class.java}) cannot be get from Preferences, call [registerTypeConverters] to support it.")
//            else -> deSerialization(getString(name,serialize(default)).toString())
        }
        return res as T
    }

    private fun <T> putPreference(name: String, value: T) = with(preference.edit()) {
        when (value) {
            is Long -> putLong(name, value)
            is Int -> putInt(name, value)
            is String -> putString(name, value)
            is Boolean -> putBoolean(name, value)
            is Float -> putFloat(name, value)
            else -> throw IllegalArgumentException("This type can be saved into Preferences")
        }.apply()
    }

    override fun <T> saveData(key: String, data: T) {
        putPreference(key, data)
    }

    override fun <T> readData(key: String, default: T): T = getPreference(key, default)
}

/**
 * You can call `LocalDataSaver.current` inside a [androidx.compose.runtime.Composable] to
 * get the instance you've provided. You can call `readData` and `saveData` then.
 */
var LocalDataSaver : ProvidableCompositionLocal<DataSaverInterface> = staticCompositionLocalOf {
    error("No instance of DataSaveInterface is provided, please call `CompositionLocalProvider(LocalDataSaver provides dataSaverPreferences){}` first. See the README of the repo ComposeDataSaver to learn more")
}