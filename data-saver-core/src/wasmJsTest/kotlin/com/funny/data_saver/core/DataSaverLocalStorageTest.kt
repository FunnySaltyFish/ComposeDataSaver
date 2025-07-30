package com.funny.data_saver.core

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * WASM platform specific tests for DataSaverLocalStorage
 * DataSaverLocalStorage 的 WASM 平台特定测试
 *
 * ./gradlew :data-saver-core:wasmJsTest
 */
class DataSaverLocalStorageTest : DataSaverInterfaceTest() {
    
    private lateinit var dataSaverLocalStorage: DataSaverLocalStorage
    
    override fun createDataSaver(): DataSaverInterface {
        dataSaverLocalStorage = DataSaverLocalStorage(
            keyPrefix = "test_",
            senseExternalDataChange = true
        )
        return dataSaverLocalStorage
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
        // 清理所有测试相关的键
        val testKeys = listOf(
            "test_string", "test_int", "test_long", "test_boolean",
            "test_float", "test_double", "test_bytearray", "test_contains",
            "test_remove", "test_null", "test_prefix", "test_custom"
        )
        
        testKeys.forEach { key ->
            try {
                dataSaverLocalStorage.remove(key)
            } catch (e: Exception) {
                // 忽略清理时的异常
            }
        }
    }
    
    @Test
    fun testLocalStorageCreation() {
        assertNotNull(dataSaverLocalStorage)
        assertTrue(dataSaverLocalStorage.senseExternalDataChange)
    }
    
    @Test
    fun testKeyPrefixFunctionality() {
        val customPrefix = "custom_prefix_"
        val customDataSaver = DataSaverLocalStorage(
            keyPrefix = customPrefix,
            senseExternalDataChange = false
        )
        
        val key = "test_prefix"
        val value = "prefix_value"
        
        customDataSaver.saveData(key, value)
        val result = customDataSaver.readData(key, "default")
        
        assertEquals(value, result)
        
        // 验证前缀不同的 DataSaver 不能读取到数据
        val differentPrefixDataSaver = DataSaverLocalStorage(
            keyPrefix = "different_prefix_",
            senseExternalDataChange = false
        )
        
        val differentResult = differentPrefixDataSaver.readData(key, "default")
        assertEquals("default", differentResult)
        
        // 清理
        customDataSaver.remove(key)
    }
    
    @Test
    fun testEmptyKeyPrefix() {
        val noPrefixDataSaver = DataSaverLocalStorage(
            keyPrefix = "",
            senseExternalDataChange = false
        )
        
        val key = "no_prefix_test"
        val value = "no_prefix_value"
        
        noPrefixDataSaver.saveData(key, value)
        val result = noPrefixDataSaver.readData(key, "default")
        
        assertEquals(value, result)
        
        // 清理
        noPrefixDataSaver.remove(key)
    }
    
    @Test
    fun testLocalStorageDataTypes() {
        // 测试各种数据类型在 LocalStorage 中的存储
        val testData = mapOf<String, Any>(
            "string_test" to "test_string_value",
            "int_test" to 42,
            "long_test" to 123456789L,
            "float_test" to 3.14f,
            "double_test" to 2.718281828,
            "boolean_true_test" to true,
            "boolean_false_test" to false
        )
        
        testData.forEach { (key, value) ->
            dataSaverLocalStorage.saveData(key, value)
        }
        
        // 验证数据类型
        assertEquals("test_string_value", dataSaverLocalStorage.readData("string_test", ""))
        assertEquals(42, dataSaverLocalStorage.readData("int_test", 0))
        assertEquals(123456789L, dataSaverLocalStorage.readData("long_test", 0L))
        assertEquals(3.14f, dataSaverLocalStorage.readData("float_test", 0f), 0.001f)
        assertEquals(2.718281828, dataSaverLocalStorage.readData("double_test", 0.0), 0.000001)
        assertEquals(true, dataSaverLocalStorage.readData("boolean_true_test", false))
        assertEquals(false, dataSaverLocalStorage.readData("boolean_false_test", true))
        
        // 清理
        testData.keys.forEach { key ->
            dataSaverLocalStorage.remove(key)
        }
    }
    
    @Test
    fun testLargeDataHandling() {
        val key = "large_data"
        val largeString = "Large data test: " + "x".repeat(1000) // 相对较小的数据，因为 LocalStorage 有大小限制
        
        dataSaverLocalStorage.saveData(key, largeString)
        val result = dataSaverLocalStorage.readData(key, "default")
        
        assertEquals(largeString, result)
        assertEquals(largeString.length, result.length)
    }
    
    @Test
    fun testSpecialCharactersInKeys() {
        val specialKeys = listOf(
            "key_with_spaces",
            "key-with-dashes",
            "key.with.dots",
            "key_with_underscore",
            "keyWithCamelCase"
        )
        
        specialKeys.forEach { key ->
            val value = "value_for_$key"
            dataSaverLocalStorage.saveData(key, value)
            val result = dataSaverLocalStorage.readData(key, "default")
            assertEquals(value, result, "Failed for key: $key")
        }
        
        // 清理
        specialKeys.forEach { key ->
            dataSaverLocalStorage.remove(key)
        }
    }
    
    @Test
    fun testSpecialCharactersInValues() {
        val key = "special_values_test"
        val specialValues = listOf(
            "Value with spaces",
            "Value\nwith\nnewlines",
            "Value\twith\ttabs",
            "Value\"with\"quotes",
            "Value'with'single'quotes",
            "Value\\with\\backslashes",
            "Value{with}braces",
            "Value[with]brackets",
            "Unicode: 测试数据 🚀 🎉",
            "JSON-like: {\"key\": \"value\", \"number\": 123}",
            "HTML-like: <div>content</div>"
        )
        
        specialValues.forEachIndexed { index, value ->
            val testKey = "${key}_$index"
            dataSaverLocalStorage.saveData(testKey, value)
            val result = dataSaverLocalStorage.readData(testKey, "default")
            assertEquals(value, result, "Failed for value: $value")
            
            // 清理
            dataSaverLocalStorage.remove(testKey)
        }
    }
    
    @Test
    fun testDataPersistenceSimulation() {
        val key = "persistence_test"
        val value = "persistent_value"
        
        // 保存数据
        dataSaverLocalStorage.saveData(key, value)
        
        // 创建新的 DataSaver 实例来模拟页面刷新
        val newDataSaver = DataSaverLocalStorage(
            keyPrefix = "test_",
            senseExternalDataChange = false
        )
        
        // 验证数据持久化
        val result = newDataSaver.readData(key, "default")
        assertEquals(value, result)
        
        // 清理
        newDataSaver.remove(key)
    }
    
    @Test
    fun testLocalStorageQuotaHandling() {
        // 测试 LocalStorage 配额处理
        // 注意：这个测试可能在不同的环境中表现不同
        val key = "quota_test"
        val attempts = 10
        var successfulSaves = 0
        
        repeat(attempts) { index ->
            try {
                val largeValue = "Large data chunk $index: " + "x".repeat(100)
                dataSaverLocalStorage.saveData("${key}_$index", largeValue)
                successfulSaves++
            } catch (e: Exception) {
                // LocalStorage 配额已满或其他错误
                println("Save failed at attempt $index: ${e.message}")
            }
        }
        
        // 至少应该能保存一些数据
        assertTrue(successfulSaves > 0, "Should be able to save at least some data")
        
        // 清理
        repeat(successfulSaves) { index ->
            try {
                dataSaverLocalStorage.remove("${key}_$index")
            } catch (e: Exception) {
                // 忽略清理时的异常
            }
        }
    }
    
    @Test
    fun testDefaultDataSaverInstance() {
        // 如果有默认实例，测试它
        // 注意：这里假设存在默认实例，如果不存在可以移除这个测试
        val key = "default_instance_test"
        val value = "default_instance_value"
        
        // 使用当前实例作为默认实例进行测试
        dataSaverLocalStorage.saveData(key, value)
        val result = dataSaverLocalStorage.readData(key, "default")
        
        assertEquals(value, result)
        
        // 清理
        dataSaverLocalStorage.remove(key)
    }
} 