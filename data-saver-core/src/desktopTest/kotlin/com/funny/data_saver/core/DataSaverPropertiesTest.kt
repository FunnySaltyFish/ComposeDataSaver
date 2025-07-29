package com.funny.data_saver.core

import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val DIR_NAME = "composeDataSaverTest"

/**
 * Desktop platform specific tests for DataSaverProperties
 * DataSaverProperties 的桌面平台特定测试
 */
class DataSaverPropertiesTest : DataSaverInterfaceTest() {
    
    private lateinit var dataSaverProperties: DataSaverProperties
    private lateinit var testFile: File
    
    override fun createDataSaver(): DataSaverInterface {
        val testDir = Files.createTempDirectory(DIR_NAME).toFile()
        testFile = File(testDir, "test.properties")
        dataSaverProperties = DataSaverProperties(testFile.absolutePath)
        return dataSaverProperties
    }
    
    @BeforeTest
    override fun setup() {
        super.setup()
        clearTestData()
    }
    
    @AfterTest
    fun tearDown() {
        clearTestData()
    }
    
    private fun clearTestData() {
        if (::testFile.isInitialized && testFile.exists()) {
            testFile.delete()
        }
    }
    
    @Test
    fun testPropertiesFileCreation() {
        val key = "test_key"
        val value = "test_value"
        
        dataSaverProperties.saveData(key, value)
        
        assertTrue(testFile.exists(), "Properties file should be created")
        assertTrue(testFile.length() > 0, "Properties file should contain data")
    }
    
    @Test
    fun testPropertiesFilePersistence() {
        val key = "persistence_test"
        val value = "persistent_value"
        
        // 保存数据
        dataSaverProperties.saveData(key, value)
        
        // 创建新的 DataSaver 实例来模拟重启
        val newDataSaver = DataSaverProperties(testFile.absolutePath)
        
        // 验证数据持久化
        val result = newDataSaver.readData(key, "default")
        assertEquals(value, result)
    }
    
    @Test
    fun testFilePathCreation() {
        val nonExistentDir = Files.createTempDirectory(DIR_NAME).toFile()
        nonExistentDir.deleteRecursively() // 删除以确保不存在
        
        val nestedFile = File(nonExistentDir, "nested/deep/test.properties")
        val dataSaver = DataSaverProperties(nestedFile.absolutePath)
        
        val key = "nested_test"
        val value = "nested_value"
        
        dataSaver.saveData(key, value)
        
        assertTrue(nestedFile.exists(), "Nested file should be created")
        assertTrue(nestedFile.parentFile.exists(), "Parent directories should be created")
        
        val result = dataSaver.readData(key, "default")
        assertEquals(value, result)
        
        // 清理
        nonExistentDir.deleteRecursively()
    }
    
    @Test
    fun testPropertiesWithSpecialCharacters() {
        val testCases = mapOf(
            "key_with_spaces" to "value with spaces",
            "key=with=equals" to "value=with=equals",
            "key:with:colons" to "value:with:colons",
            "key#with#hash" to "value#with#hash",
            "unicode_key_测试" to "unicode_value_测试值"
        )
        
        testCases.forEach { (key, value) ->
            dataSaverProperties.saveData(key, value)
            val result = dataSaverProperties.readData(key, "default")
            assertEquals(value, result, "Failed for key: $key")
        }
    }
    
    @Test
    fun testLargeDataHandling() {
        val key = "large_data_test"
        val largeValue = "x".repeat(10000) // 10KB 的数据
        
        dataSaverProperties.saveData(key, largeValue)
        val result = dataSaverProperties.readData(key, "default")
        
        assertEquals(largeValue, result)
        assertEquals(largeValue.length, result.length)
    }
    
    @Test
    fun testConcurrentFileAccess() {
        val threads = mutableListOf<Thread>()
        val results = mutableMapOf<String, String>()
        
        repeat(5) { index ->
            val thread = Thread {
                val key = "concurrent_$index"
                val value = "value_$index"
                
                dataSaverProperties.saveData(key, value)
                val result = dataSaverProperties.readData(key, "default")
                
                synchronized(results) {
                    results[key] = result
                }
            }
            threads.add(thread)
            thread.start()
        }
        
        threads.forEach { it.join() }
        
        // 验证所有结果
        repeat(5) { index ->
            val key = "concurrent_$index"
            val expected = "value_$index"
            assertEquals(expected, results[key])
        }
    }
}

/**
 * Desktop platform specific tests for DataSaverEncryptedProperties
 * DataSaverEncryptedProperties 的桌面平台特定测试
 */
class DataSaverEncryptedPropertiesTest : DataSaverInterfaceTest() {
    
    private lateinit var dataSaverEncryptedProperties: DataSaverEncryptedProperties
    private lateinit var testFile: File
    private val testPassword = "test_password_123"
    
    override fun createDataSaver(): DataSaverInterface {
        val testDir = Files.createTempDirectory(DIR_NAME).toFile()
        testFile = File(testDir, "test_encrypted.properties")
        dataSaverEncryptedProperties = DataSaverEncryptedProperties(
            testFile.absolutePath, testPassword
        )
        return dataSaverEncryptedProperties
    }
    
    @BeforeTest
    override fun setup() {
        super.setup()
        clearTestData()
    }
    
    @AfterTest
    fun tearDown() {
        clearTestData()
    }
    
    private fun clearTestData() {
        if (::testFile.isInitialized && testFile.exists()) {
            testFile.delete()
        }
    }
    
    @Test
    fun testEncryptedPropertiesFileCreation() {
        val key = "encrypted_test_key"
        val value = "encrypted_test_value"
        
        dataSaverEncryptedProperties.saveData(key, value)
        
        assertTrue(testFile.exists(), "Encrypted properties file should be created")
        
        // 验证文件内容是加密的（不应该包含明文）
        val fileContent = testFile.readText()
        assertFalse(fileContent.contains(value), "File should not contain plain text value")
    }
    
    @Test
    fun testEncryptedDataPersistence() {
        val key = "persistence_encrypted_test"
        val value = "persistent_encrypted_value"
        
        // 保存数据
        dataSaverEncryptedProperties.saveData(key, value)
        
        // 创建新的加密 DataSaver 实例
        val newDataSaver = DataSaverEncryptedProperties(
            testFile.absolutePath, testPassword
        )
        
        // 验证加密数据可以正确解密和读取
        val result = newDataSaver.readData(key, "default")
        assertEquals(value, result)
    }
    
    @Test
    fun testWrongPasswordHandling() {
        val key = "password_test"
        val value = "password_test_value"
        
        // 使用正确密码保存数据（启用数据完整性检查）
        val secureDataSaver = DataSaverEncryptedProperties(
            testFile.absolutePath, testPassword, enableDataIntegrityCheck = true
        )
        secureDataSaver.saveData(key, value)
        
        // 使用错误密码尝试读取（启用数据完整性检查）
        val wrongPasswordDataSaver = DataSaverEncryptedProperties(
            testFile.absolutePath, "wrong_password", enableDataIntegrityCheck = true
        )
        
        // 应该无法正确读取数据，返回默认值
        val result = wrongPasswordDataSaver.readData(key, "default_value")
        assertEquals("default_value", result)
        
        // 清理
        secureDataSaver.clearCipherCache()
        wrongPasswordDataSaver.clearCipherCache()
    }
    
    @Test
    fun testPasswordChangeHandling() {
        val key = "password_change_test"
        val value = "password_change_value"
        
        // 使用第一个密码保存数据（启用数据完整性检查）
        val firstDataSaver = DataSaverEncryptedProperties(
            testFile.absolutePath, testPassword, enableDataIntegrityCheck = true
        )
        firstDataSaver.saveData(key, value)
        
        // 验证第一个密码可以正确读取
        val firstResult = firstDataSaver.readData(key, "default")
        assertEquals(value, firstResult)
        
        // 更改密码
        val newPassword = "new_password_456"
        val newPasswordDataSaver = DataSaverEncryptedProperties(
            testFile.absolutePath, newPassword, enableDataIntegrityCheck = true
        )
        
        // 使用新密码读取应该失败（返回默认值）
        val result = newPasswordDataSaver.readData(key, "default")
        assertEquals("default", result)
        
        // 使用新密码重新保存数据（覆盖原有数据）
        newPasswordDataSaver.saveData(key, value)
        
        // 使用新密码读取应该成功
        val newResult = newPasswordDataSaver.readData(key, "default")
        assertEquals(value, newResult)
        
        // 验证旧密码现在无法读取（因为数据已被新密码重新加密）
        val oldPasswordResult = firstDataSaver.readData(key, "old_default")
        assertEquals("old_default", oldPasswordResult)
        
        // 清理
        firstDataSaver.clearCipherCache()
        newPasswordDataSaver.clearCipherCache()
    }
    
    @Test
    fun testEncryptedSpecialCharacters() {
        val testCases = mapOf(
            "special_chars_key" to "Special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?",
            "unicode_key" to "Unicode: 测试数据 🚀 🎉 ñáéíóú",
            "json_like" to """{"key": "value", "number": 123, "array": [1,2,3]}""",
            "xml_like" to "<root><item>value</item></root>"
        )
        
        testCases.forEach { (key, value) ->
            dataSaverEncryptedProperties.saveData(key, value)
            val result = dataSaverEncryptedProperties.readData(key, "default")
            assertEquals(value, result, "Failed for encrypted key: $key")
        }
    }
    
    @Test
    fun testEncryptedLargeData() {
        val key = "large_encrypted_data"
        val largeValue = "Encrypted data: " + "x".repeat(5000)
        
        dataSaverEncryptedProperties.saveData(key, largeValue)
        val result = dataSaverEncryptedProperties.readData(key, "default")
        
        assertEquals(largeValue, result)
        assertEquals(largeValue.length, result.length)
    }
    
    @Test
    fun testBackwardCompatibility() {
        val key = "backward_compatibility_test"
        val value = "backward_compatible_value"
        
        // 默认情况下应该保持向后兼容
        val defaultDataSaver = DataSaverEncryptedProperties(
            testFile.absolutePath, testPassword
        )
        
        defaultDataSaver.saveData(key, value)
        val result = defaultDataSaver.readData(key, "default")
        
        assertEquals(value, result)
        
        // 创建另一个实例验证向后兼容性
        val anotherDataSaver = DataSaverEncryptedProperties(
            testFile.absolutePath, testPassword
        )
        val compatResult = anotherDataSaver.readData(key, "default")
        assertEquals(value, compatResult)
        
        defaultDataSaver.clearCipherCache()
        anotherDataSaver.clearCipherCache()
    }
    
    @Test
    fun testDataIntegrityCheckFeature() {
        val key = "integrity_check_test"
        val value = "integrity_test_value"
        
        // 测试启用数据完整性检查的情况
        val secureDataSaver = DataSaverEncryptedProperties(
            testFile.absolutePath, testPassword, enableDataIntegrityCheck = true
        )
        
        secureDataSaver.saveData(key, value)
        val result = secureDataSaver.readData(key, "default")
        
        assertEquals(value, result)
        secureDataSaver.clearCipherCache()
    }
    
    @Test
    fun testCipherCaching() {
        val key = "cipher_cache_test"
        val value = "cache_test_value"
        
        // 多次操作验证缓存功能
        repeat(100) { index ->
            val testKey = "${key}_$index"
            val testValue = "${value}_$index"
            
            dataSaverEncryptedProperties.saveData(testKey, testValue)
            val result = dataSaverEncryptedProperties.readData(testKey, "default")
            
            assertEquals(testValue, result)
        }
        
        // 清理缓存
        dataSaverEncryptedProperties.clearCipherCache()
        
        // 清理后仍然应该正常工作
        dataSaverEncryptedProperties.saveData(key, value)
        val result = dataSaverEncryptedProperties.readData(key, "default")
        assertEquals(value, result)
    }
    
    @Test
    fun testThreadSafety() {
        val key = "thread_safety_test"
        val threads = mutableListOf<Thread>()
        val results = mutableMapOf<String, String>()
        
        repeat(10) { index ->
            val thread = Thread {
                val testKey = "${key}_$index"
                val testValue = "thread_value_$index"
                
                dataSaverEncryptedProperties.saveData(testKey, testValue)
                val result = dataSaverEncryptedProperties.readData(testKey, "default")
                
                synchronized(results) {
                    results[testKey] = result
                }
            }
            threads.add(thread)
            thread.start()
        }
        
        threads.forEach { it.join() }
        
        // 验证所有结果
        repeat(10) { index ->
            val testKey = "${key}_$index"
            val expectedValue = "thread_value_$index"
            assertEquals(expectedValue, results[testKey])
        }
        
        dataSaverEncryptedProperties.clearCipherCache()
    }
} 