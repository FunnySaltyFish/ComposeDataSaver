@file:Suppress("UNCHECKED_CAST", "UNUSED")

package com.funny.data_saver_data_store

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.funny.data_saver.core.DataSaverInterface
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

/**
 * The implementation using [PreferenceDataStore] to save data. And because DataStore supports coroutine,
 * so does this.
 */
class DataSaverDataStorePreferences : DataSaverInterface {
    companion object {
        private lateinit var dataStore : DataStore<Preferences>
        fun setDataStorePreferences(dataStore: DataStore<Preferences>){
            this.dataStore = dataStore
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
                throw IllegalArgumentException("Unable to read $default, this type(${if (default==null)null else default!!::class.java}) cannot be get from DataStore, call [registerTypeConverters] to support it.")
            }
        }
    }

    private suspend fun <T> put(dataStore: DataStore<Preferences>, key: String, value: T) {
        dataStore.edit { setting ->
            when (value) {
                is Int -> setting[intPreferencesKey(key)] = value
                is Long -> setting[longPreferencesKey(key)] = value
                is Double -> setting[doublePreferencesKey(key)] = value
                is Float -> setting[floatPreferencesKey(key)] = value
                is Boolean -> setting[booleanPreferencesKey(key)] = value
                is String -> setting[stringPreferencesKey(key)] = value
                else -> throw IllegalArgumentException("Unable to save $value, this type(${if (value==null)null else value!!::class.java}) cannot be saved using DataStore, call [registerTypeConverters] to support it.")
            }
        }
    }
}