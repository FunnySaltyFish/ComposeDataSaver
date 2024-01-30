@file:Suppress("UNCHECKED_CAST", "UNUSED")

package com.funny.data_saver_data_store

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.funny.data_saver.core.DataSaverInterface
import com.funny.data_saver.core.DataSaverLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * The implementation using [PreferenceDataStore] to save data. And because DataStore supports coroutine,
 * so does this.
 */
open class DataSaverDataStorePreferences(
    private val dataStore: DataStore<Preferences>,
    senseExternalDataChange: Boolean = false
) : DataSaverInterface(senseExternalDataChange) {
    private val scope by lazy { CoroutineScope(Dispatchers.IO) }
    private val logger by lazy { DataSaverLogger("DataStorePreferences") }
    init {
        if (senseExternalDataChange) {
            scope.launch {
                dataStore.data.distinctUntilChanged().collect {
                    it.asMap().forEach { (key, value) ->
                        logger.d("$key -> $value is emitted")
                        externalDataChangedFlow?.tryEmit(key.name to value)
                    }
                }
            }
        }
    }

    override fun <T> readData(key: String, default: T): T {
        return runBlocking { get(dataStore, key, default) }
    }

    override fun <T> saveData(key: String, data: T) {
        runBlocking { put(dataStore, key, data) }
    }

    override suspend fun <T> saveDataAsync(key: String, data: T)
        = put(dataStore, key, data)

    override fun remove(key: String) {
        runBlocking {
            dataStore.edit {
                it.remove(intPreferencesKey(key))
            }
        }
    }

    // Reference：https://blog.csdn.net/qq_36707431/article/details/119447093
    private suspend fun <T> get(dataStore: DataStore<Preferences>, key: String, default: T): T {
        return when (default) {
            is Int -> {
                dataStore.data.map { setting ->
                    setting[intPreferencesKey(key)] ?: default
                }.first() as T
            }
            is Long -> {
                dataStore.data.map { setting ->
                    setting[longPreferencesKey(key)] ?: default
                }.first() as T
            }
            is Double -> {
                dataStore.data.map { setting ->
                    setting[doublePreferencesKey(key)] ?: default
                }.first() as T
            }
            is Float -> {
                dataStore.data.map { setting ->
                    setting[floatPreferencesKey(key)] ?: default
                }.first() as T
            }
            is Boolean -> {
                dataStore.data.map { setting ->
                    setting[booleanPreferencesKey(key)]?: default
                }.first() as T
            }
            is String -> {
                dataStore.data.map { setting ->
                    setting[stringPreferencesKey(key)] ?: default
                }.first() as T
            }
            else -> {
                throw IllegalArgumentException("Unable to read $default, this type(${default!!::class.java}) cannot be read from DataStore, call [registerTypeConverters] to support it.")
            }
        }
    }

    private suspend fun <T> put(dataStore: DataStore<Preferences>, key: String, value: T) {
        if (value == null){
            remove(key)
            return
        }
        dataStore.edit { setting ->
            when (value) {
                is Int -> setting[intPreferencesKey(key)] = value
                is Long -> setting[longPreferencesKey(key)] = value
                is Double -> setting[doublePreferencesKey(key)] = value
                is Float -> setting[floatPreferencesKey(key)] = value
                is Boolean -> setting[booleanPreferencesKey(key)] = value
                is String -> setting[stringPreferencesKey(key)] = value
                else -> throw IllegalArgumentException("Unable to save $value, this type(${value!!::class.java}) cannot be saved using DataStore, call [registerTypeConverters] to support it.")
            }
        }
    }

    override fun contains(key: String): Boolean {
        return runBlocking {
            dataStore.data.map { setting ->
                setting.contains(intPreferencesKey(key))
            }.first()
        }
    }
}