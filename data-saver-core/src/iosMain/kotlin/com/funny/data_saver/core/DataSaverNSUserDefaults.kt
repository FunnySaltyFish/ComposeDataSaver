package com.funny.data_saver.core

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.toCValues
import platform.Foundation.NSArray
import platform.Foundation.NSData
import platform.Foundation.NSDate
import platform.Foundation.NSDictionary
import platform.Foundation.NSKeyValueChangeNewKey
import platform.Foundation.NSKeyValueObservingOptionNew
import platform.Foundation.NSMutableDictionary
import platform.Foundation.NSNumber
import platform.Foundation.NSURL
import platform.Foundation.NSUserDefaults
import platform.Foundation.addObserver
import platform.Foundation.allKeys
import platform.Foundation.arrayWithArray
import platform.Foundation.create
import platform.Foundation.removeObserver
import platform.Foundation.setValue
import platform.darwin.NSObject
import kotlinx.cinterop.autoreleasepool as autorelease

/**
 * KVO Observer for UserDefaults changes
 * UserDefaults 变更的 KVO 观察者
 */
@OptIn(ExperimentalForeignApi::class)
class UserDefaultsKVOObserver(
    private val onChanged: (String, Any?) -> Unit
) : NSObject(), NSObjectObserverProtocol {

    override fun observeValueForKeyPath(
        keyPath: String?,
        ofObject: Any?,
        change: Map<Any?, *>?,
        context: kotlinx.cinterop.COpaquePointer?
    ) {
        keyPath?.let { key ->
            val newValue = change?.get(NSKeyValueChangeNewKey)
            DataSaverLogger.d("Key '$key' changed to: $newValue")
            onChanged(key, newValue)
        }
    }
}

/**
 * iOS platform implementation using NSUserDefaults to save data.
 * Enhanced version with support for more data types and efficient external change observation.
 * 使用 NSUserDefaults 的增强版 iOS 平台实现，支持更多数据类型和高效的外部变更监听。
 */
@OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)
open class DataSaverNSUserDefaults(
    private val userDefaults: NSUserDefaults = NSUserDefaults.standardUserDefaults,
    senseExternalDataChange: Boolean = false
) : DataSaverInterface(senseExternalDataChange) {

    private var kvoObserver: UserDefaultsKVOObserver? = null
    private val observedKeys = mutableSetOf<String>()

    init {
        if (senseExternalDataChange) {
            setupKVOObserver()
        }
    }

    /**
     * Setup KVO observer for precise key monitoring
     * 设置 KVO 观察者以精确监听键变更
     */
    private fun setupKVOObserver() {
        kvoObserver = UserDefaultsKVOObserver { key, newValue ->
            externalDataChangedFlow?.tryEmit(key to newValue)
        }
    }

    /**
     * Add KVO observation for a specific key
     * 为特定键添加 KVO 观察
     */
    private fun addKVOForKey(key: String) {
        if (senseExternalDataChange && !observedKeys.contains(key)) {
            kvoObserver?.let { observer ->
                autorelease {
                    userDefaults.addObserver(
                        observer = observer,
                        forKeyPath = key,
                        options = NSKeyValueObservingOptionNew,
                        context = null
                    )
                }
            }
            observedKeys.add(key)
        }
    }

    /**
     * Remove KVO observation for a specific key
     * 移除特定键的 KVO 观察
     */
    private fun removeKVOForKey(key: String) {
        if (observedKeys.contains(key)) {
            kvoObserver?.let { observer ->
                autorelease {
                    userDefaults.removeObserver(observer, forKeyPath = key)
                }
            }
            observedKeys.remove(key)
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun <T> saveData(key: String, data: T) {
        if (data == null) {
            remove(key)
            return
        }

        autorelease {
            when (data) {
                is Long -> userDefaults.setInteger(data, key)
                is Int -> userDefaults.setInteger(data.toLong(), key)
                is String -> userDefaults.setObject(data, key)
                is Boolean -> userDefaults.setBool(data, key)
                is Float -> userDefaults.setFloat(data, key)
                is Double -> userDefaults.setDouble(data, key)
                is NSDate -> saveDate(key, data)
                is ByteArray -> {
                    memScoped {
                        val nsData = NSData.create(
                            bytes = data.toCValues().getPointer(this),
                            length = data.size.toULong()
                        )
                        userDefaults.setObject(nsData, key)
                    }
                }
                is List<*> -> {
                    // Convert to NSArray for supported types
                    val nsArray = data.map { item ->
                        when (item) {
                            is String, is Number, is Boolean -> item
                            else -> unsupportedType("save", data)
                        }
                    }.let { NSArray.arrayWithArray(it) }
                    userDefaults.setObject(nsArray, key)
                }
                is Map<*, *> -> {
                    // Convert to NSDictionary for supported types
                    val nsDict = NSMutableDictionary()
                    data.forEach { (k, v) ->
                        if (k is String && (v is String || v is Number || v is Boolean)) {
                            nsDict.setValue(v, k)
                        } else {
                            unsupportedType("save", data)
                        }
                    }
                    userDefaults.setObject(nsDict, key)
                }
                else -> unsupportedType("save", data)
            }
            userDefaults.synchronize()

            // Add KVO observation for this key if external change sensing is enabled
            addKVOForKey(key)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> readData(key: String, default: T): T = autorelease {
        // Add KVO observation for this key if external change sensing is enabled
        addKVOForKey(key)

        val rawValue = userDefaults.objectForKey(key) ?: return@autorelease default

        val res: Any = when (default) {
            is Long -> (rawValue as? NSNumber)?.longLongValue ?: default
            is Int -> (rawValue as? NSNumber)?.intValue ?: default
            is String -> rawValue as? String ?: default
            is Boolean -> (rawValue as? NSNumber)?.boolValue ?: default
            is Float -> (rawValue as? NSNumber)?.floatValue ?: default
            is Double -> (rawValue as? NSNumber)?.doubleValue ?: default
            is ByteArray -> {
                memScoped {
                    (rawValue as? NSData)?.let { nsData ->
                        nsData.bytes?.readBytes(nsData.length.toInt())
                    } ?: default
                }
            }
            is NSDate -> readDate(key, default) ?: default
            is List<*> -> {
                (rawValue as? NSArray)?.let { nsArray ->
                    (0 until nsArray.count.toInt()).mapNotNull { index ->
                        nsArray.objectAtIndex(index.toULong())
                    }
                } ?: default
            }
            is Map<*, *> -> {
                (rawValue as? NSDictionary)?.let { nsDict ->
                    buildMap {
                        nsDict.allKeys.forEach { key ->
                            val keyStr = key as? String
                            val value = nsDict.objectForKey(key)
                            if (keyStr != null && value != null) {
                                put(keyStr, value)
                            }
                        }
                    }
                } ?: default
            }
            else -> unsupportedType("read", default)
        }
        return@autorelease res as T
    }

    override fun remove(key: String) {
        autorelease {
            userDefaults.removeObjectForKey(key)
            userDefaults.synchronize()
            removeKVOForKey(key)
        }
    }

    override fun contains(key: String): Boolean = autorelease {
        userDefaults.objectForKey(key) != null
    }

    /**
     * Clear all observed keys and stop external change observation
     * 清除所有观察的键并停止外部变更观察
     */
    fun clearObservation() {
        observedKeys.toList().forEach { key ->
            removeKVOForKey(key)
        }
        kvoObserver = null
    }

    /**
     * Get all currently observed keys
     * 获取当前观察的所有键
     */
    fun getObservedKeys(): Set<String> = observedKeys.toSet()

    /**
     * Save a URL value
     * 保存 URL 值
     */
    fun saveURL(key: String, url: String) {
        autorelease {
            val nsURL = NSURL.URLWithString(url)
            if (nsURL != null) {
                userDefaults.setURL(nsURL, key)
                userDefaults.synchronize()
                addKVOForKey(key)
            }
        }
    }

    /**
     * Read a URL value
     * 读取 URL 值
     */
    fun readURL(key: String, default: String? = null): String? = autorelease {
        addKVOForKey(key)
        userDefaults.URLForKey(key)?.absoluteString ?: default
    }

    /**
     * Save a Date value
     * 保存 Date 值
     */
    fun saveDate(key: String, date: NSDate) {
        autorelease {
            userDefaults.setObject(date, key)
            userDefaults.synchronize()
            addKVOForKey(key)
        }
    }

    /**
     * Read a Date value
     * 读取 Date 值
     */
    fun readDate(key: String, default: NSDate? = null): NSDate? = autorelease {
        addKVOForKey(key)
        userDefaults.objectForKey(key) as? NSDate ?: default
    }
}

val DefaultDataSaverNSUserDefaults by lazy(LazyThreadSafetyMode.PUBLICATION) {
    DataSaverNSUserDefaults(senseExternalDataChange = true)
}