package com.funny.data_saver.core

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Android platform specific tests for DataSaverPreferences
 * DataSaverPreferences 的 Android 平台特定测试
 */
@RunWith(AndroidJUnit4::class)
class DataSaverPreferencesTest : DataSaverInterfaceTest() {
    
    private lateinit var context: Context
    private lateinit var dataSaverPreferences: DataSaverPreferences
    
    override fun createDataSaver(): DataSaverInterface {
        context = ApplicationProvider.getApplicationContext()
        dataSaverPreferences = DataSaverPreferences(
            context = context,
            name = "test_preferences",
            senseExternalDataChange = true
        )
        return dataSaverPreferences
    }
    
    @Before
    override fun setup() {
        super.setup()
        // 清理测试数据
        clearTestData()
    }
    
    @After
    fun tearDown() {
        clearTestData()
    }
    
    private fun clearTestData() {
        val editor = context.getSharedPreferences("test_preferences", Context.MODE_PRIVATE).edit()
        editor.clear()
        editor.apply()
    }
    
    @Test
    fun testPreferencesCreation() {
        assertNotNull(dataSaverPreferences)
        assertTrue(dataSaverPreferences.senseExternalDataChange)
    }
    
    @Test
    fun testPreferencesWithDifferentName() {
        val customDataSaver = DataSaverPreferences(
            context = context,
            name = "custom_test_preferences",
            senseExternalDataChange = false
        )
        
        val key = "test_key"
        val value = "test_value"
        
        customDataSaver.saveData(key, value)
        val result = customDataSaver.readData(key, "default")
        
        assertEquals(value, result)
        
        // 清理
        context.getSharedPreferences("custom_test_preferences", Context.MODE_PRIVATE)
            .edit().clear().apply()
    }
    
    @Test
    fun testSharedPreferencesIntegration() {
        val key = "shared_prefs_test"
        val value = "shared_value"
        
        // 直接通过 SharedPreferences 保存数据
        val sharedPrefs = context.getSharedPreferences("test_preferences", Context.MODE_PRIVATE)
        sharedPrefs.edit().putString(key, value).apply()
        
        // 通过 DataSaver 读取
        val result = dataSaverPreferences.readData(key, "default")
        assertEquals(value, result)
    }
    
    @Test
    fun testMultipleDataTypes() {
        // 测试多种数据类型的保存和读取
        dataSaverPreferences.saveData("string_key", "string_value")
        dataSaverPreferences.saveData("int_key", 100)
        dataSaverPreferences.saveData("long_key", 200L)
        dataSaverPreferences.saveData("float_key", 3.14f)
        dataSaverPreferences.saveData("boolean_key", true)
        
        assertEquals("string_value", dataSaverPreferences.readData("string_key", ""))
        assertEquals(100, dataSaverPreferences.readData<Int>("int_key", 0))
        assertEquals(200L, dataSaverPreferences.readData("long_key", 0L))
        assertEquals(3.14f, dataSaverPreferences.readData("float_key", 0f), 0.001f)
        assertEquals(true, dataSaverPreferences.readData("boolean_key", false))
    }
    
    @Test
    fun testConcurrentAccess() {
        val key = "concurrent_test"
        val threads = mutableListOf<Thread>()
        
        repeat(10) { index ->
            val thread = Thread {
                val value = "value_$index"
                dataSaverPreferences.saveData("${key}_$index", value)
                val result = dataSaverPreferences.readData("${key}_$index", "default")
                assertEquals(value, result)
            }
            threads.add(thread)
            thread.start()
        }
        
        threads.forEach { it.join() }
        
        // 验证所有数据都正确保存
        repeat(10) { index ->
            val expected = "value_$index"
            val actual = dataSaverPreferences.readData("${key}_$index", "default")
            assertEquals(expected, actual)
        }
    }

    override fun testSaveAndReadDouble() {
        // SharedPreference dose not support Double
    }
} 