package com.funny.data_saver.core

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * key interface
 */
interface DataSaverInterface{
    fun <T> saveData(key:String, data : T)
    fun <T> readData(key: String, default : T) : T
//    fun <T> readDataBean(key: String, clazz: Class<T>, default : T) : T
//    fun <T> saveDataBean(key: String, clazz: Class<T>, default : T)
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
            else -> throw IllegalArgumentException("This type can be get from Preferences")
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
 * get the instance you've provided. You can call `readData` and `SaveData` then.
 */
var LocalDataSaver : ProvidableCompositionLocal<DataSaverInterface> = staticCompositionLocalOf {
    error("No instance of DataSaveInterface is provided")
}