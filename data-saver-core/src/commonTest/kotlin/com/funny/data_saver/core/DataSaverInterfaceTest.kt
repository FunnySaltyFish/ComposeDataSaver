package com.funny.data_saver.core

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Common test cases for DataSaverInterface implementations
 * DataSaverInterface 实现的公共测试用例
 */
abstract class DataSaverInterfaceTest {
    
    abstract fun createDataSaver(): DataSaverInterface
    
    protected lateinit var dataSaver: DataSaverInterface
    
    @BeforeTest
    open fun setup() {
        dataSaver = createDataSaver()
    }
    
    @Test
    fun testSaveAndReadString() {
        val key = "test_string"
        val value = "Hello, World!"
        val default = "default"
        
        dataSaver.saveData(key, value)
        val result = dataSaver.readData(key, default)
        
        assertEquals(value, result)
    }
    
    @Test
    fun testSaveAndReadInt() {
        val key = "test_int"
        val value = 42
        val default = 0
        
        dataSaver.saveData(key, value)
        val result = dataSaver.readData(key, default)
        
        assertEquals(value, result)
    }
    
    @Test
    fun testSaveAndReadLong() {
        val key = "test_long"
        val value = 123456789L
        val default = 0L
        
        dataSaver.saveData(key, value)
        val result = dataSaver.readData(key, default)
        
        assertEquals(value, result)
    }
    
    @Test
    fun testSaveAndReadBoolean() {
        val key = "test_boolean"
        val value = true
        val default = false
        
        dataSaver.saveData(key, value)
        val result = dataSaver.readData(key, default)
        
        assertEquals(value, result)
    }
    
    @Test
    fun testSaveAndReadFloat() {
        val key = "test_float"
        val value = 3.14f
        val default = 0.0f
        
        dataSaver.saveData(key, value)
        val result = dataSaver.readData(key, default)
        
        assertEquals(value, result, 0.001f)
    }
    
    @Test
    fun testSaveAndReadDouble() {
        val key = "test_double"
        val value = 3.141592653589793
        val default = 0.0
        
        dataSaver.saveData(key, value)
        val result = dataSaver.readData(key, default)
        
        assertEquals(value, result, 0.000001)
    }
    
    @Test
    fun testReadWithDefault() {
        val key = "non_existent_key"
        val default = "default_value"
        
        val result = dataSaver.readData(key, default)
        
        assertEquals(default, result)
    }
    
    @Test
    fun testContains() {
        val key = "test_contains"
        val value = "test_value"
        
        assertFalse(dataSaver.contains(key))
        
        dataSaver.saveData(key, value)
        assertTrue(dataSaver.contains(key))
    }
    
    @Test
    fun testRemove() {
        val key = "test_remove"
        val value = "test_value"
        val default = "default"
        
        dataSaver.saveData(key, value)
        assertTrue(dataSaver.contains(key))
        
        dataSaver.remove(key)
        assertFalse(dataSaver.contains(key))
        
        val result = dataSaver.readData(key, default)
        assertEquals(default, result)
    }
    
    @Test
    fun testSaveNullRemovesKey() {
        val key = "test_null"
        val value = "test_value"
        val default = "default"
        
        dataSaver.saveData(key, value)
        assertTrue(dataSaver.contains(key))
        
        dataSaver.saveData(key, null)
        assertFalse(dataSaver.contains(key))
        
        val result = dataSaver.readData(key, default)
        assertEquals(default, result)
    }
} 