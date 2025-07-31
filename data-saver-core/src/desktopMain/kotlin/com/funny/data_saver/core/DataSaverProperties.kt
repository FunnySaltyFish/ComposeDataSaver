package com.funny.data_saver.core

import com.funny.data_saver.kmp.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.Properties

/**
 * Use [Properties] to save data in a file, all data are stored in **PLAIN TEXT**. If you want to encrypt the data, you can use [DataSaverEncryptedProperties] instead.
 *
 * ---
 * 基于 [Properties] 的数据存储器，所有数据都是以 **明文** 存储的。如果你想要加密数据，可以使用 [DataSaverEncryptedProperties]。
 * @property filePath String The file path to save the data file. 数据文件的保存路径。
 * @property fileEncoding String The encoding of the data file, default to UTF-8. 数据文件的编码，默认为 UTF-8。
 * @param enableFileMonitoring Boolean Whether to enable automatic file monitoring to detect external changes.
 *        When enabled, the properties cache will be reloaded if the file was modified by other instances.
 *        Default is false to avoid performance overhead.
 *        是否启用自动文件监控以检测外部更改。启用后，如果文件被其他实例修改，属性缓存将重新加载。默认为false以避免性能开销。
 * @constructor
 */
class DataSaverProperties(
    private val filePath: String,
    private val fileEncoding: String = "UTF-8",
    private val enableFileMonitoring: Boolean = false
) : DataSaverInterface() {
    private val properties = Properties()
    private var lastModified: Long = 0

    init {
        loadProperties()
    }
    
    private fun loadProperties() {
        try {
            val file = File(filePath)
            if (file.exists()) {
                lastModified = file.lastModified()
                InputStreamReader(FileInputStream(filePath), fileEncoding).use { reader ->
                    properties.clear()
                    properties.load(reader)
                }
            } else {
                createFile(filePath)
                lastModified = 0
            }
        } catch (e: FileNotFoundException) {
            // 处理文件不存在等异常
            createFile(filePath)
            lastModified = 0
        } catch (e: Exception) {
            Log.e(TAG, "Error loading properties: ${e.message}", e)
        }
    }
    
    private fun checkAndReloadIfModified() {
        if (!enableFileMonitoring) return
        try {
            val file = File(filePath)
            if (file.exists() && file.lastModified() > lastModified) {
                loadProperties()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking file modification: ${e.message}", e)
        }
    }

    private fun saveProperties() {
        try {
            OutputStreamWriter(FileOutputStream(filePath), fileEncoding).use { writer ->
                properties.store(writer, null)
            }
            // 更新最后修改时间
            val file = File(filePath)
            if (file.exists()) {
                lastModified = file.lastModified()
            }
        } catch (e: FileNotFoundException) {
            Log.e(TAG, "File not found: $filePath")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving properties: ${e.message}", e)
        }
    }

    override fun <T> saveData(key: String, data: T) {
        if (data == null) {
            remove(key)
            return
        }
        properties[key] = data.toString()
        saveProperties()
    }

    override fun <T> readData(key: String, default: T): T {
        checkAndReloadIfModified()
        val value = properties.getProperty(key) ?: return default
        return when (default) {
            is Int -> value.toIntOrNull() ?: default
            is Long -> value.toLongOrNull() ?: default
            is Boolean -> value.toBooleanStrictOrNull() ?: default
            is Double -> value.toDoubleOrNull() ?: default
            is Float -> value.toFloatOrNull() ?: default
            is String -> value
            else -> unsupportedType("read", default)
        } as T
    }

    override fun remove(key: String) {
        properties.remove(key)
        saveProperties()
    }

    override fun contains(key: String): Boolean {
        checkAndReloadIfModified()
        return properties.containsKey(key)
    }

    companion object {
        private const val TAG = "DataSaverProperties"
    }
}

internal fun createFile(filePath: String) {
    val file = File(filePath)
    if (!file.exists()) {
        val parentFile = file.parentFile
        parentFile?.mkdirs()
        file.createNewFile()
    }
}