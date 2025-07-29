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
 * @constructor
 */
class DataSaverProperties(
    private val filePath: String,
    private val fileEncoding: String = "UTF-8"
) : DataSaverInterface() {
    private val properties = Properties()

    init {
        try {
            InputStreamReader(FileInputStream(filePath), fileEncoding).use { reader ->
                properties.load(reader)
            }
        } catch (e: FileNotFoundException) {
            // 处理文件不存在等异常
            createFile(filePath)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading properties: ${e.message}", e)
        }
    }

    private fun saveProperties() {
        try {
            OutputStreamWriter(FileOutputStream(filePath), fileEncoding).use { writer ->
                properties.store(writer, null)
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