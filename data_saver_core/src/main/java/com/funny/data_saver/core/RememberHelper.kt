package com.funny.data_saver.core

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlin.reflect.KProperty

class DataSaverMutableState<T>(
    private val dataSaverInterface: DataSaverInterface,
    private val key: String,
    value: T,
    private val autoSave: Boolean = true
){
    private var state : MutableState<T> = mutableStateOf(value)

    operator fun setValue(thisObj: Any?, property: KProperty<*>, value: T) {
        if (autoSave && this.state.value != value) saveData(value)
        this.state.value = value
    }

    operator fun getValue(thisObj: Any?, property: KProperty<*>): T {
        return this.state.value
    }

    private fun saveData(value: T){
        value?:return
        val typeConverter = typeConverters[value!!::class.java]
        if(typeConverter!=null){
            Log.d(TAG, "saveConvertedData: $key -> $value")
            dataSaverInterface.saveData(key, typeConverter(value))
        }else {
            Log.d(TAG, "saveData:$key -> $value")
            dataSaverInterface.saveData(key, value)
        }
    }

    companion object {
        val typeConverters : MutableMap<Class<*>, (Any)->Any> = mutableMapOf()
        const val TAG = "RememberHelper"
    }
}

@Composable
fun <T> rememberDataSaverState(key:String, default:T, autoSave: Boolean = true) : DataSaverMutableState<T> {
    val saverInterface = LocalDataSaver.current
    return remember {
        DataSaverMutableState(saverInterface, key , saverInterface.readData(key, default), autoSave = autoSave)
    }
}

fun registerTypeConverters(clazz: Class<*>, func: (Any)->Any){
    DataSaverMutableState.typeConverters[clazz] = func
}