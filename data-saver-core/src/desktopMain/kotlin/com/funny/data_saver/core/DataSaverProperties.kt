package com.funny.data_saver.core

import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.FileWriter
import java.util.Properties

/**
 * Use [Properties] to save data in a file, all data are stored in **PLAIN TEXT**. If you want to encrypt the data, you can use [DataSaverEncryptedProperties] instead.
 *
 * ---
 * 基于 [Properties] 的数据存储器，所有数据都是以 **明文** 存储的。如果你想要加密数据，可以使用 [DataSaverEncryptedProperties]。
 * @property filePath String The file path to save the data file. 数据文件的保存路径。
 * @constructor
 */
class DataSaverProperties(private val filePath: String) : DataSaverInterface() {
    private val properties = Properties()

    init {
        try {
            FileReader(filePath).use { reader ->
                properties.load(reader)
            }
        } catch (e: FileNotFoundException) {
            // 处理文件不存在等异常
            createFile(filePath)
        }
    }

    private fun saveProperties() {
        FileWriter(filePath).use { writer ->
            properties.store(writer, null)
        }
    }

    override fun <T> saveData(key: String, data: T) {
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
}

internal fun createFile(filePath: String) {
    val file = File(filePath)
    if (!file.exists()) {
        val parentFile = file.parentFile
        parentFile?.mkdirs()
        file.createNewFile()
    }
}