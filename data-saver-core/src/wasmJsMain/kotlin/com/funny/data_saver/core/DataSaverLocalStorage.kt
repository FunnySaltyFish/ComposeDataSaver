package com.funny.data_saver.core

import kotlinx.browser.localStorage
import org.w3c.dom.get
import org.w3c.dom.set

/**
 * WASM platform implementation using localStorage to save data.
 * 使用 localStorage 的 WASM 平台实现。
 */
open class DataSaverLocalStorage(
    private val keyPrefix: String = "DataSaver_",
    senseExternalDataChange: Boolean = false
) : DataSaverInterface(senseExternalDataChange) {

    // localStorage doesn't support listener by default, so we manually notify the listener
    private fun notifyExternalDataChanged(key: String, value: Any?) {
        if (senseExternalDataChange) externalDataChangedFlow?.tryEmit(key to value)
    }

    private fun getPrefixedKey(key: String) = keyPrefix + key

    override fun <T> saveData(key: String, data: T) {
        if (data == null) {
            remove(key)
            notifyExternalDataChanged(key, null)
            return
        }

        val prefixedKey = getPrefixedKey(key)
        val stringValue = when (data) {
            is Long -> data.toString()
            is Int -> data.toString()
            is String -> data
            is Boolean -> data.toString()
            is Float -> data.toString()
            is Double -> data.toString()
            else -> unsupportedType("save", data)
        }
        
        localStorage[prefixedKey] = stringValue
        notifyExternalDataChanged(key, data)
    }

    override fun <T> readData(key: String, default: T): T {
        val prefixedKey = getPrefixedKey(key)
        val value = localStorage[prefixedKey] ?: return default
        
        val res: Any = when (default) {
            is Long -> value.toLongOrNull() ?: default
            is Int -> value.toIntOrNull() ?: default
            is String -> value
            is Boolean -> value.toBooleanStrictOrNull() ?: default
            is Float -> value.toFloatOrNull() ?: default
            is Double -> value.toDoubleOrNull() ?: default
            else -> unsupportedType("read", default)
        }
        return res as T
    }

    override fun remove(key: String) {
        val prefixedKey = getPrefixedKey(key)
        localStorage.removeItem(prefixedKey)
        notifyExternalDataChanged(key, null)
    }

    override fun contains(key: String): Boolean {
        val prefixedKey = getPrefixedKey(key)
        return localStorage[prefixedKey] != null
    }
}

val DefaultDataSaverLocalStorage by lazy(LazyThreadSafetyMode.PUBLICATION) {
    DataSaverLocalStorage()
} 