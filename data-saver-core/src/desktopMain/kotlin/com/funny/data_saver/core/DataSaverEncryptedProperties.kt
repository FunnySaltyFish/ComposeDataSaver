package com.funny.data_saver.core

import com.funny.data_saver.kmp.Log
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.security.MessageDigest
import java.util.Base64
import java.util.Properties
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Use [Properties] to save data in a properties file with encryption. The algorithm is AES/CBC/PKCS5Padding.
 *
 * @property filePath String The file path to save the data file. 数据文件的保存路径。
 * @param encryptionKey String The key to encrypt the data, can be any string. 用于加密数据的密钥，可以是任意字符串（实际使用的密钥为该字符串的 SHA-256 哈希值，且仅用其前 16 位产生 iv）
 * @param fileEncoding String The encoding of the data file, default to UTF-8. 数据文件的编码，默认为 UTF-8。
 * @param enableDataIntegrityCheck Boolean Whether to enable data integrity check by adding a prefix to encrypted data. 
 *        When enabled, it can detect wrong passwords or corrupted data, but breaks backward compatibility with existing encrypted data.
 *        Default is false to maintain backward compatibility.
 *        是否启用数据完整性检查，通过在加密数据中添加前缀来实现。启用后可以检测错误密码或数据损坏，但会破坏与现有加密数据的向后兼容性。默认为false以保持向后兼容性。
 */

open class DataSaverEncryptedProperties(
    private val filePath: String,
    private val encryptionKey: String,
    private val fileEncoding: String = "UTF-8",
    private val enableDataIntegrityCheck: Boolean = false
) : DataSaverInterface() {
    private val properties = Properties()
    private val hashedKey = hashKey(encryptionKey)
    
    // 使用 ThreadLocal 缓存 cipher 实例以提高性能并确保线程安全
    private val encryptCipherCache = ThreadLocal<Cipher>()
    private val decryptCipherCache = ThreadLocal<Cipher>()

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

    private fun createCipher(mode: Int): Cipher {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val keySpec = SecretKeySpec(hashedKey, "AES")
        val ivParameterSpec = IvParameterSpec(hashedKey.copyOfRange(0, 16))
        cipher.init(mode, keySpec, ivParameterSpec)
        return cipher
    }
    
    /**
     * 获取加密cipher，使用ThreadLocal缓存以提高性能
     */
    private fun getEncryptCipher(): Cipher {
        return encryptCipherCache.get() ?: run {
            val cipher = createCipher(Cipher.ENCRYPT_MODE)
            encryptCipherCache.set(cipher)
            cipher
        }
    }
    
    /**
     * 获取解密cipher，使用ThreadLocal缓存以提高性能
     */
    private fun getDecryptCipher(): Cipher {
        return decryptCipherCache.get() ?: run {
            val cipher = createCipher(Cipher.DECRYPT_MODE)
            decryptCipherCache.set(cipher)
            cipher
        }
    }

    private fun hashKey(key: String): ByteArray {
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(key.toByteArray())
    }

    private fun encrypt(value: String): String {
        val dataToEncrypt = if (enableDataIntegrityCheck) {
            // 只有启用数据完整性检查时才添加校验标记
            "$CHECKSUM_PREFIX$value"
        } else {
            value
        }
        
        val cipher = getEncryptCipher()
        val encryptedBytes = cipher.doFinal(dataToEncrypt.toByteArray())
        return Base64.getEncoder().encodeToString(encryptedBytes)
    }

    private fun decrypt(value: String): String {
        val cipher = getDecryptCipher()
        val decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(value))
        val decryptedString = String(decryptedBytes)
        
        if (enableDataIntegrityCheck) {
            // 只有启用数据完整性检查时才验证校验标记
            if (!decryptedString.startsWith(CHECKSUM_PREFIX)) {
                throw IllegalArgumentException("Invalid encryption key or corrupted data, key: $encryptionKey")
            }
            return decryptedString.substring(CHECKSUM_PREFIX.length)
        } else {
            // 不启用数据完整性检查时，直接返回解密结果
            return decryptedString
        }
    }

    override fun <T> saveData(key: String, data: T) {
        if (data == null) {
            remove(key)
            return
        }
        val encryptedValue = encrypt(data.toString())
        properties[key] = encryptedValue
        saveProperties()
    }

    override fun <T> readData(key: String, default: T): T {
        val encryptedValue = properties.getProperty(key) ?: return default
        val decryptedValue = try {
            decrypt(encryptedValue)
        } catch (e: Exception) {
            Log.e(TAG, "Error decrypting value: ${e.message}", e)
            return default
        }
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
    
    /**
     * Clear cipher caches to free memory.
     * Call this method when the instance is no longer needed or in long-running applications.
     * 清理cipher缓存以释放内存。
     * 当实例不再需要或在长时间运行的应用程序中时调用此方法。
     */
    fun clearCipherCache() {
        encryptCipherCache.remove()
        decryptCipherCache.remove()
    }

    companion object {
        private const val TAG = "DataSaverEncryptedProperties"
        const val CHECKSUM_PREFIX = "__DSE_VALID_"
    }
}