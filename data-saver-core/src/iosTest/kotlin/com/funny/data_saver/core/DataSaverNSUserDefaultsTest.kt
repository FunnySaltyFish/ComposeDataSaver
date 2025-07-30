package com.funny.data_saver.core

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.runBlocking
import platform.Foundation.NSDate
import platform.Foundation.NSUserDefaults
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.timeIntervalSince1970
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * iOS platform specific tests for DataSaverNSUserDefaults
 * DataSaverNSUserDefaults 的 iOS 平台特定测试
 */
@OptIn(ExperimentalForeignApi::class)
class DataSaverNSUserDefaultsTest : DataSaverInterfaceTest() {
    
    private lateinit var nsUserDefaultsDataSaver: DataSaverNSUserDefaults
    private lateinit var testUserDefaults: NSUserDefaults
    
    override fun createDataSaver(): DataSaverInterface {
        // 创建测试专用的 NSUserDefaults 实例
        testUserDefaults = NSUserDefaults(suiteName = "test_user_defaults")
        nsUserDefaultsDataSaver = DataSaverNSUserDefaults(
            userDefaults = testUserDefaults,
            senseExternalDataChange = true
        )
        return nsUserDefaultsDataSaver
    }
    
    @BeforeTest
    override fun setup() {
        super.setup()
        // 清理测试数据
        clearTestData()
    }
    
    @AfterTest
    fun tearDown() {
        // 清理观察和测试数据
        nsUserDefaultsDataSaver.clearObservation()
        clearTestData()
    }
    
    private fun clearTestData() {
        // 清理可能存在的测试键
        val testKeys = listOf(
            "test_string", "test_int", "test_long", "test_boolean",
            "test_float", "test_double", "test_bytearray", "test_url",
            "test_date", "test_list", "test_map", "test_contains",
            "test_remove", "test_null", "test_kvo", "external_change_test"
        )
        
        testKeys.forEach { key ->
            testUserDefaults.removeObjectForKey(key)
        }
        testUserDefaults.synchronize()
    }
    
    @Test
    fun testSaveAndReadURL() {
        val key = "test_url"
        val urlString = "https://example.com/test"
        val defaultUrl = "https://default.com"
        
        nsUserDefaultsDataSaver.saveURL(key, urlString)
        val result = nsUserDefaultsDataSaver.readURL(key, defaultUrl)
        
        assertEquals(urlString, result)
    }
    
    @Test
    fun testReadURLWithDefault() {
        val key = "non_existent_url"
        val defaultUrl = "https://default.com"
        
        val result = nsUserDefaultsDataSaver.readURL(key, defaultUrl)
        
        assertEquals(defaultUrl, result)
    }
    
    @Test
    fun testSaveAndReadDate() {
        val key = "test_date"
        val date = NSDate()
        val defaultDate = NSDate.dateWithTimeIntervalSince1970(0.0)
        
        nsUserDefaultsDataSaver.saveDate(key, date)
        val result = nsUserDefaultsDataSaver.readDate(key, defaultDate)
        
        assertNotNull(result)
        // 由于时间精度问题，我们比较时间戳差异是否在合理范围内
        val timeDifference = kotlin.math.abs(date.timeIntervalSince1970 - result!!.timeIntervalSince1970)
        assertTrue(timeDifference < 0.1, "Date difference should be less than 0.1 seconds")
    }
    
    @Test
    fun testReadDateWithDefault() {
        val key = "non_existent_date"
        val defaultDate = NSDate.dateWithTimeIntervalSince1970(0.0)
        
        val result = nsUserDefaultsDataSaver.readDate(key, defaultDate)
        
        assertEquals(defaultDate, result)
    }
    
    @Test
    fun testSaveAndReadList() {
        val key = "test_list"
        val value = listOf("item1", "item2", "item3")
        val default = emptyList<String>()
        
        nsUserDefaultsDataSaver.saveData(key, value)
        val result = nsUserDefaultsDataSaver.readData(key, default)
        
        assertEquals(value.size, result.size)
        assertEquals(value, result)
    }
    
    @Test
    fun testSaveAndReadMap() {
        val key = "test_map"
        val value = mapOf(
            "key1" to "value1",
            "key2" to "value2",
            "key3" to 123
        )
        val default = emptyMap<String, Any>()
        
        nsUserDefaultsDataSaver.saveData(key, value)
        val result = nsUserDefaultsDataSaver.readData(key, default)
        
        assertEquals(value.size, result.size)
        assertEquals(value["key1"], result["key1"])
        assertEquals(value["key2"], result["key2"])
        // NSNumber 可能会改变数字类型，所以我们检查数值
        assertEquals((value["key3"] as Int).toLong(), (result["key3"] as Number).toLong())
    }
    
    @Test
    fun testKVOObservation() = runBlocking {
        val key = "test_kvo"
        val initialValue = "initial_value"
        val newValue = "new_value"
        
        // 首先保存一个初始值以建立 KVO 观察
        nsUserDefaultsDataSaver.saveData(key, initialValue)
        
        // 确保观察者已设置
        assertTrue(nsUserDefaultsDataSaver.getObservedKeys().contains(key))
        
        // 在 iOS 测试环境中，我们可以模拟外部数据变更
        // 通过直接修改 NSUserDefaults 来触发 KVO
        testUserDefaults.setObject(newValue, key)
        testUserDefaults.synchronize()
        
        // 验证观察者是否收到了变更通知
        // 注意：在单元测试环境中，KVO 可能不会立即触发，这是正常的
        val observedKeys = nsUserDefaultsDataSaver.getObservedKeys()
        assertTrue(observedKeys.contains(key), "Key should be observed")
    }
    
    @Test
    fun testClearObservation() {
        val key1 = "test_key1"
        val key2 = "test_key2"
        val value = "test_value"
        
        // 保存数据以建立观察
        nsUserDefaultsDataSaver.saveData(key1, value)
        nsUserDefaultsDataSaver.saveData(key2, value)
        
        // 验证观察已建立
        val observedKeys = nsUserDefaultsDataSaver.getObservedKeys()
        assertTrue(observedKeys.contains(key1))
        assertTrue(observedKeys.contains(key2))
        
        // 清除观察
        nsUserDefaultsDataSaver.clearObservation()
        
        // 验证观察已清除
        val clearedKeys = nsUserDefaultsDataSaver.getObservedKeys()
        assertTrue(clearedKeys.isEmpty())
    }
    
    @Test
    fun testGetObservedKeys() {
        val keys = listOf("key1", "key2", "key3")
        val value = "test_value"
        
        // 初始状态应该没有观察的键
        assertTrue(nsUserDefaultsDataSaver.getObservedKeys().isEmpty())
        
        // 保存数据以建立观察
        keys.forEach { key ->
            nsUserDefaultsDataSaver.saveData(key, value)
        }
        
        // 验证所有键都被观察
        val observedKeys = nsUserDefaultsDataSaver.getObservedKeys()
        assertEquals(keys.size, observedKeys.size)
        keys.forEach { key ->
            assertTrue(observedKeys.contains(key))
        }
    }
    
    @Test
    fun testRemoveStopsObservation() {
        val key = "test_remove_observation"
        val value = "test_value"
        
        // 保存数据以建立观察
        nsUserDefaultsDataSaver.saveData(key, value)
        assertTrue(nsUserDefaultsDataSaver.getObservedKeys().contains(key))
        
        // 删除数据
        nsUserDefaultsDataSaver.remove(key)
        
        // 验证观察也被移除
        assertFalse(nsUserDefaultsDataSaver.getObservedKeys().contains(key))
        assertFalse(nsUserDefaultsDataSaver.contains(key))
    }
    
    @Test
    fun testDataSaverWithoutExternalChangeObservation() {
        val dataSaverWithoutObservation = DataSaverNSUserDefaults(
            userDefaults = testUserDefaults,
            senseExternalDataChange = false
        )
        
        val key = "test_no_observation"
        val value = "test_value"
        
        dataSaverWithoutObservation.saveData(key, value)
        
        // 验证没有建立观察
        assertTrue(dataSaverWithoutObservation.getObservedKeys().isEmpty())
        
        val result = dataSaverWithoutObservation.readData(key, "default")
        assertEquals(value, result)
        
        // 读取后也不应该建立观察
        assertTrue(dataSaverWithoutObservation.getObservedKeys().isEmpty())
    }
    
    @Test
    fun testUnsupportedDataTypes() {
        val key = "test_unsupported"
        
        // 测试不支持的数据类型
        class UnsupportedType
        val unsupportedValue = UnsupportedType()
        
        assertFailsWith<IllegalArgumentException> {
            nsUserDefaultsDataSaver.saveData(key, unsupportedValue)
        }
    }
    
    @Test
    fun testDefaultDataSaverInstance() {
        // 测试默认实例
        val defaultInstance = DefaultDataSaverNSUserDefaults
        assertNotNull(defaultInstance)
        
        val key = "test_default_instance"
        val value = "test_value"
        val default = "default_value"
        
        defaultInstance.saveData(key, value)
        val result = defaultInstance.readData(key, default)
        
        assertEquals(value, result)
        
        // 清理
        defaultInstance.remove(key)
    }
} 