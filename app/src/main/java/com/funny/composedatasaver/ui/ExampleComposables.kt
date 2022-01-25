package com.funny.composedatasaver.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.funny.composedatasaver.Constant.KEY_BEAN_EXAMPLE
import com.funny.composedatasaver.Constant.KEY_BOOLEAN_EXAMPLE
import com.funny.data_saver.core.LocalDataSaver
import com.funny.composedatasaver.Constant.KEY_STRING_EXAMPLE
import com.funny.data_saver.core.DataSaverMutableState
import com.funny.data_saver.core.rememberDataSaverState
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
data class ExampleBean(var id:Int, val label:String)
val EmptyBean = ExampleBean(233,"FunnySaltyFish")

@ExperimentalSerializationApi
@Composable
fun ExampleComposable() {
    // get dataSaver
    // you can use this to save data manually
    val dataSaverInterface = LocalDataSaver.current

    // you can set [autoSave] = false to prevent saving too frequently
    // in that case, you need to saveData by yourself
    // eg: onClick = { dataSaverInterface.saveData(key, value) }
    var stringExample by rememberDataSaverState(KEY_STRING_EXAMPLE, "", autoSave = true)

    var booleanExample by rememberDataSaverState(KEY_BOOLEAN_EXAMPLE, false)

    var beanExample by rememberDataSaverBeanState(KEY_BEAN_EXAMPLE, default = EmptyBean)

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(24.dp)) {
        Text(text = "This is an example of saving String")
        OutlinedTextField(value = stringExample, onValueChange = {
            stringExample = it
        })

        Text(text = "This is an example of saving Boolean")
        Switch(checked = booleanExample, onCheckedChange = {
            booleanExample = it
        })

        Text(text = "This is an example of saving custom Data Bean")
        Text(text = beanExample.toString())
        Button(onClick = {
            beanExample = beanExample.copy(id = beanExample.id+1)
//            dataSaverInterface.saveData(KEY_BEAN_EXAMPLE,Json.encodeToString(beanExample))
        }) {
            Text(text = "Add bean's id")
        }
    }
}

/**
 * This function is used to read custom bean
 * Here we use [Json] to load [String] and convert it into bean
 * You can use whatever you like to do this(eg.Gson/Jackson/Fastjson)
 * @param key String
 * @param default T
 * @return DataSaverMutableState<T>
 */
@ExperimentalSerializationApi
@Composable
inline fun <reified T> rememberDataSaverBeanState(key:String, default:T) : DataSaverMutableState<T> {
    val dataSaverInterface = LocalDataSaver.current
    val jsonData = dataSaverInterface.readData(key,"")
    return if (jsonData == "") remember {
        DataSaverMutableState(dataSaverInterface, key, default)
    } else remember {
        DataSaverMutableState(dataSaverInterface, key, Json.decodeFromString(jsonData))
    }
}

/**
 * This function is used to save custom bean
 * If you don't provide converters which save such classes,
 * you can use mutableState like this and call `dataSaverInterface.saveData()` manually
 * @param key String the key of value
 * @param default T default value
 * @return MutableState<T>
 */
//@ExperimentalSerializationApi
//@Composable
//inline fun <reified T> rememberDataSaverBeanState(key:String, default:T) : MutableState<T> {
//    val dataSaverInterface = LocalDataSaver.current
//    val jsonData = dataSaverInterface.readData(key,"")
//    return if (jsonData == "") remember {
//        mutableStateOf(default)
//    } else remember {
//        mutableStateOf(Json.decodeFromString(jsonData))
//    }
//}