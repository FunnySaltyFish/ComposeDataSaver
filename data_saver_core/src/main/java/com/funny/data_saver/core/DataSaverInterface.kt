package com.funny.data_saver.core

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalInspectionMode
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * The interface is used to save/read data. We provide the basic implementation using Preference, DataStore and MMKV.
 *
 * If you want to write your own, you need to implement `saveData` and `readData`. Besides, a suspend function `saveDataAsync` is optional(which is equal to `saveData` by default)
 */
abstract class DataSaverInterface(val senseExternalDataChange: Boolean = false) {
    abstract fun <T> saveData(key: String, data: T)
    abstract fun <T> readData(key: String, default: T): T
    open suspend fun <T> saveDataAsync(key: String, data: T) = saveData(key, data)
    abstract fun remove(key: String)
    abstract fun contains(key: String): Boolean

    var externalDataChangedFlow: MutableSharedFlow<Pair<String, Any?>>? =
        if (senseExternalDataChange) MutableSharedFlow(replay = 1) else null
}

/**
 * Default implementation using [SharedPreferences] to save data
 */
open class DataSaverPreferences(
    private val preference: SharedPreferences,
    senseExternalDataChange: Boolean = false
) : DataSaverInterface(senseExternalDataChange) {
    constructor(
        context: Context,
        senseExternalDataChange: Boolean
    ) : this(
        context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE),
        senseExternalDataChange
    )

    private val logger by lazy { DataSaverLogger("DataSaverPreferences") }
    private val onSharedPreferenceChangeListener by lazy {
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            logger.d("data changed: $key -> ${sharedPreferences.all[key]}, subscriptionCount: ${externalDataChangedFlow?.subscriptionCount?.value}")
            externalDataChangedFlow?.tryEmit(key to sharedPreferences.all[key])
        }
    }

    init {
        if (senseExternalDataChange) {
            this.preference.registerOnSharedPreferenceChangeListener(
                onSharedPreferenceChangeListener
            )
        }
    }

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
 * Using [HashMap] to save data in memory, can be used for testing
 * @property map MutableMap<String, Any?>
 */
class DataSaverInMemory(senseExternalDataChange: Boolean = false) : DataSaverInterface(senseExternalDataChange) {
    inner class ObservableMap() {
        private val map by lazy {
            mutableMapOf<String, Any?>()
        }

        operator fun set(key: String, value: Any?) {
            map[key] = value
            externalDataChangedFlow?.tryEmit(key to value)
        }

        operator fun get(key: String): Any? {
            return map[key]
        }

        fun remove(key: String) {
            map.remove(key)
            externalDataChangedFlow?.tryEmit(key to null)
        }

        fun containsKey(key: String) = map.containsKey(key)
    }

    private val map = ObservableMap()

    override fun <T> saveData(key: String, data: T) {
        waringUsage()
        if (data == null) {
            remove(key)
            return
        }
        map[key] = data
    }

    override fun <T> readData(key: String, default: T): T {
        waringUsage()
        val res = map[key] ?: default
        return res as T
    }

    override fun remove(key: String) {
        waringUsage()
        map.remove(key)
    }

    override fun contains(key: String) = map.containsKey(key)

    private fun waringUsage() {
        DataSaverLogger.w("DataSaverInMemory is used, it's not recommended to use it in production because it saves data in memory, which will be lost when the app is killed. If you are in Preview mode, please ignore this warning.")
    }
}

/**
 * You can call `LocalDataSaver.current` inside a [androidx.compose.runtime.Composable] to
 * get the instance you've provided. You can call `readData` and `saveData` then.
 */
var LocalDataSaver: ProvidableCompositionLocal<DataSaverInterface> = staticCompositionLocalOf {
    DefaultDataSaverInMemory
}

internal val DefaultDataSaverInMemory by lazy {
    DataSaverInMemory()
}

/**
 * Get the [DataSaverInterface] instance, if [LocalInspectionMode.current] is true, return [DataSaverInMemory] instead
 * which supports preview in Android Studio
 * @return DataSaverInterface
 */
@Composable
@ReadOnlyComposable
fun getLocalDataSaverInterface() =
    if (LocalInspectionMode.current) DefaultDataSaverInMemory else LocalDataSaver.current