package com.funny.data_saver.core

import android.content.Context
import android.content.SharedPreferences

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

    constructor(
        context: Context,
        name: String,
        mode: Int = Context.MODE_PRIVATE,
        senseExternalDataChange: Boolean = false
    ) : this(
        context.getSharedPreferences(name, mode),
        senseExternalDataChange
    )

    private val logger by lazy { DataSaverLogger("DataSaverPreferences") }
    private val onSharedPreferenceChangeListener by lazy {
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            key ?: return@OnSharedPreferenceChangeListener
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
            is Int -> putInt(key, data)
            is Long -> putLong(key, data)
            is String -> putString(key, data)
            is Boolean -> putBoolean(key, data)
            is Float -> putFloat(key, data)
            else -> unsupportedType("save", data)
        }.apply()
    }

    override fun <T> readData(key: String, default: T): T = with(preference) {
        val res: Any = when (default) {
            is Int -> getInt(key, default)
            is Long -> getLong(key, default)
            is String -> getString(key, default)!!
            is Boolean -> getBoolean(key, default)
            is Float -> getFloat(key, default)
            else -> unsupportedType("read", default)
        }
        return res as T
    }

    override fun remove(key: String) {
        preference.edit().remove(key).apply()
    }

    override fun contains(key: String) = preference.contains(key)

}
