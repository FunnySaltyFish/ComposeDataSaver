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
    fun remove(key: String)
    fun contains(key: String): Boolean
}

/**
 * Default implementation using [SharedPreferences] to save data
 */
class DataSaverPreferences(private val preference: SharedPreferences) : DataSaverInterface {
    constructor(context: Context): this(context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE))

    override fun <T> saveData(key: String, data: T) = with(preference.edit()) {
        when (data) {
            null -> {
                this@DataSaverPreferences.remove(key)
                return@with
            }
            is Long -> putLong(key, data)
            is Int -> putInt(key, data)
            is String -> putString(key, data)
            is Boolean -> putBoolean(key, data)
            is Float -> putFloat(key, data)
            else -> throw IllegalArgumentException("Unable to save $data, this type(${data!!::class.java}) cannot be saved using SharedPreferences, call [registerTypeConverters] to support it.")
        }.apply()
    }

    override fun <T> readData(key: String, default: T): T = with(preference) {
        val res: Any = when (default) {
            is Long -> getLong(key, default)
            is String -> this.getString(key, default)!!
            is Int -> getInt(key, default)
            is Boolean -> getBoolean(key, default)
            is Float -> getFloat(key, default)
            else -> throw IllegalArgumentException("Unable to read $default, this type(${default!!::class.java}) cannot be get from Preferences, call [registerTypeConverters] to support it.")
        }
        return res as T
    }

    override fun remove(key: String) {
        preference.edit().remove(key).apply()
    }

    override fun contains(key: String) = preference.contains(key)
}

/**
 * You can call `LocalDataSaver.current` inside a [androidx.compose.runtime.Composable] to
 * get the instance you've provided. You can call `readData` and `saveData` then.
 */
var LocalDataSaver : ProvidableCompositionLocal<DataSaverInterface> = staticCompositionLocalOf {
    error("No instance of DataSaveInterface is provided, please call `CompositionLocalProvider(LocalDataSaver provides dataSaverPreferences){}` first. See the README of the repo ComposeDataSaver to learn more")
}