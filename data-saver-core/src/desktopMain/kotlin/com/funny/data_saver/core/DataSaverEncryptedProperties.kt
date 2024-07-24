package com.funny.data_saver.core

import java.io.FileNotFoundException
import java.io.FileReader
import java.io.FileWriter
import java.security.MessageDigest
import java.util.Base64
import java.util.Properties
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Use [Properties] to save data in a properties file with encryption. The algorithm is AES/CBC/NoPadding.
 *
 * @property filePath String The file path to save the data file. 数据文件的保存路径。
 * @param encryptionKey String The key to encrypt the data, can be any string. 用于加密数据的密钥，可以是任意字符串（实际使用的密钥为该字符串的 SHA-256 哈希值，且仅用其前 16 位产生 iv）
 */

open class DataSaverEncryptedProperties(private val filePath: String, private val encryptionKey: String) : DataSaverInterface() {
    private val properties = Properties()
    private val hashedKey = hashKey(encryptionKey)
    private val encryptCipher by lazy { createCipher(Cipher.ENCRYPT_MODE) }
    private val decryptCipher by lazy { createCipher(Cipher.DECRYPT_MODE) }

    init {
        try {
            createFile(filePath)
            FileReader(filePath).use { reader ->
                properties.load(reader)
            }
        } catch (e: FileNotFoundException) {
            // Handle file not found exception
        } catch (e: Exception) {
            // Handle other exceptions
            e.printStackTrace()
        }
    }

    private fun saveProperties() {
        try {
            FileWriter(filePath).use { writer ->
                properties.store(writer, null)
            }
        } catch (e: Exception) {
            // Handle file write exception
            e.printStackTrace()
        }
    }

    private fun createCipher(mode: Int): Cipher {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val keySpec = SecretKeySpec(hashedKey, "AES")
        val ivParameterSpec = IvParameterSpec(hashedKey.copyOfRange(0, 16))
        cipher.init(mode, keySpec, ivParameterSpec)
        return cipher
    }

    private fun hashKey(key: String): ByteArray {
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(key.toByteArray())
    }

    private fun encrypt(value: String): String {
        val encryptedBytes = encryptCipher.doFinal(value.toByteArray())
        return Base64.getEncoder().encodeToString(encryptedBytes)
    }

    private fun decrypt(value: String): String {
        val decryptedBytes = decryptCipher.doFinal(Base64.getDecoder().decode(value))
        return String(decryptedBytes)
    }

    override fun <T> saveData(key: String, data: T) {
        val encryptedValue = encrypt(data.toString())
        properties[key] = encryptedValue
        saveProperties()
    }

    override fun <T> readData(key: String, default: T): T {
        val encryptedValue = properties.getProperty(key) ?: return default
        val decryptedValue = decrypt(encryptedValue)
        return when (default) {
            is Int -> decryptedValue.toIntOrNull() ?: default
            is Long -> decryptedValue.toLongOrNull() ?: default
            is Boolean -> decryptedValue.toBooleanStrictOrNull() ?: default
            is Double -> decryptedValue.toDoubleOrNull() ?: default
            is Float -> decryptedValue.toFloatOrNull() ?: default
            is String -> decryptedValue
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