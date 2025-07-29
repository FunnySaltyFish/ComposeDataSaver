package com.funny.data_saver.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.RadioButton
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.data_saver.Constant
import com.funny.data_saver.Constant.KEY_BEAN_EXAMPLE
import com.funny.data_saver.Constant.KEY_BOOLEAN_EXAMPLE
import com.funny.data_saver.Constant.KEY_STRING_EXAMPLE
import com.funny.data_saver.core.ClassTypeConverter
import com.funny.data_saver.core.DataSaverInMemory
import com.funny.data_saver.core.DataSaverInterface
import com.funny.data_saver.core.DataSaverMutableState
import com.funny.data_saver.core.LocalDataSaver
import com.funny.data_saver.core.SavePolicy
import com.funny.data_saver.core.getLocalDataSaverInterface
import com.funny.data_saver.core.rememberDataSaverState
import com.funny.data_saver.kmp.IO
import com.funny.data_saver.kmp.Log
import com.funny.data_saver.kmp.viewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import moe.tlaster.precompose.navigation.BackHandler
import kotlin.random.Random
import kotlin.reflect.typeOf

/**
 * Example usage of this library
 *
 * NOTE: You may see two languages in comments: English and Chinese,
 * they HAVE THE SAME MEANING. you can choose one to read.
 *
 * -----------------------
 * 此仓库的使用示例
 * 注意：下面的注释包含两种语言：中、英文，意思一样的。
 * 选一个读就好
 */

@Serializable
data class ExampleBean(var id: Int, val label: String)

val EmptyBean = ExampleBean(233, "FunnySaltyFish")


@ExperimentalSerializationApi
@Composable
fun ExampleComposable() {
    // get dataSaver                          | 获取 DataSaverInterface
    // you can use this to save data manually | 您可以使用此变量做手动保存
    val dataSaverInterface = getLocalDataSaverInterface()

    // support @Preview by additionally register the type converter
    val isInspectMode = LocalInspectionMode.current

    // you can set [savePolicy] to other types (see [SavePolicy] ) to prevent saving too frequently
    // if you set it to SavePolicy.NEVER , you need to saveData by yourself
    // eg: onClick = { dataSaverState.save() }
    // .............................................................
    // 你可以设置 [savePolicy]为其他类型(参见 [SavePolicy] )，以防止某些情况下过于频繁地保存
    // 如果你设置为 SavePolicy.NEVER，则写入本地的操作需要自己做
    // 例如: onClick = { dataSaverState.save() }
    var stringExample by rememberDataSaverState(
        KEY_STRING_EXAMPLE,
        "FunnySaltyFish, tap to input",
        savePolicy = SavePolicy.IMMEDIATELY,
        async = true
    )

    var booleanExample by rememberDataSaverState(key = KEY_BOOLEAN_EXAMPLE, initialValue = false)

    var beanExample by rememberDataSaverState(key = KEY_BEAN_EXAMPLE, initialValue = EmptyBean)


    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Heading(text = "Simple Examples:")
        Text(text = "This is an example of saving String") // 保存字符串的示例
        OutlinedTextField(value = stringExample, onValueChange = {
            stringExample = it
        })

        Text(text = "Saving Boolean") // 保存布尔值的示例
        Switch(checked = booleanExample, onCheckedChange = {
            booleanExample = it
        })



        Heading(text = "Saving Custom Data Bean") // 保存自定义类型的示例
        Text(text = beanExample.toString())
        Button(onClick = {
            beanExample = beanExample.copy(id = beanExample.id + 1)
        }) {
            Text(text = "Add bean's id") // id自加
        }

        ParcelableExample()

        ListExample()

        CustomSealedClassExample()

        NullableExample()

        SenseExternalDataChangeExample()

        CustomTypeConverterExample()

        SaveWhenDisposedExample()

        CustomCoroutineScopeAndViewModelSample()

        TimeConsumingJobExample()


    }
}

@Composable
expect fun ParcelableExample()

@Composable
private fun ListExample() {
    var listExample: List<ExampleBean> by rememberDataSaverState(
        key = "key_list_example", initialValue = listOf(
            EmptyBean.copy(label = "Name 1"),
            EmptyBean.copy(label = "Name 2"),
            EmptyBean.copy(label = "Name 3")
        )
    )
    Heading(text = "List Example")
    LazyColumn(Modifier.heightIn(0.dp, 400.dp)) {
        items(listExample) { item ->
            Text(modifier = Modifier.padding(8.dp), text = item.toString(), fontSize = 16.sp)
        }
        item {
            Row {
                Button(onClick = {
                    listExample += EmptyBean.copy(label = "Name ${listExample.size + 1}")
                }) {
                    Text(text = "Add To List")
                }
                Spacer(modifier = Modifier.width(4.dp))
                Button(onClick = {
                    if (listExample.isNotEmpty()) {
                        listExample = listExample.dropLast(1)
                    }
                }) {
                    Text(text = "Remove From List")
                }
            }
        }
    }
}

@Composable
private fun SaveWhenDisposedExample() {
    var showDialog by remember {
        mutableStateOf(false)
    }
    if (showDialog) AlertDialog(
        onDismissRequest = { showDialog = false },
        title = { Text(text = "Sample") },
        text = {
            var stringExample2 by rememberDataSaverState(
                key = Constant.KEY_STRING_EXAMPLE_2,
                initialValue = "this one will be saved only when disposed",
                savePolicy = SavePolicy.DISPOSED,
                async = false
            )
            OutlinedTextField(value = stringExample2, onValueChange = {
                stringExample2 = it
            })
        },
        confirmButton = { TextButton(onClick = { showDialog = false }) { Text(text = "Close") } },
    )
    Heading(text = "Save-When-Disposed Example")
    Button(onClick = { showDialog = true }) {
        Text(text = "Click Me To Open Dialog")
    }
}

@Composable
private fun ColumnScope.NullableExample() {
    val nullableCustomBeanState: DataSaverMutableState<ExampleBean?> =
        rememberDataSaverState(key = "nullable_bean", initialValue = null)
    Heading(text = "Saving Custom Data Bean(nullable)") // 保存自定义类型的示例
    Text(text = nullableCustomBeanState.value.toString())
    Row(Modifier.fillMaxWidth()) {
        Button(onClick = {
            nullableCustomBeanState.value = ExampleBean(id = 100, label = "I'm not null")
        }) {
            Text(text = "Set As Not Null")
        }
        Spacer(modifier = Modifier.width(4.dp))
        Button(onClick = {
            nullableCustomBeanState.value = null
            // nullableCustomBeanState.remove(replacement = EmptyBean)
        }) {
            Text(text = "Set As Null")
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Composable
private fun ColumnScope.SenseExternalDataChangeExample() {
    val dataSaver = getSensorExternalDataSaver()
    Heading(text = "Sense External Data Change Example")
    CompositionLocalProvider(LocalDataSaver provides dataSaver) {
        val key = "sense_external_data_change_example"
        val stringExample by rememberDataSaverState(
            key = key,
            initialValue = "Hello World(1)",
            senseExternalDataChange = true
        )
        val stringExample2 by rememberDataSaverState(
            key = key,
            initialValue = "Hello World(2)",
            senseExternalDataChange = true
        )
        val dataSaverInterface = getLocalDataSaverInterface()
        Text(text = "var1: $stringExample")
        Text(text = "var2: $stringExample2")
        Button(onClick = {
            // here we change the local data instead of the state
            dataSaverInterface.saveData(key, "Hello World ${Random.nextInt()}")
        }) {
            Text(text = "Click Me To Change Local Data")
        }

        Spacer(modifier = Modifier.height(4.dp))
        val keyBean = "sense_external_data_change_example_bean"
        val bean: ExampleBean? by rememberDataSaverState(
            key = keyBean,
            initialValue = null,
            senseExternalDataChange = true
        )
        val saveBean = { b: ExampleBean? ->
            dataSaverInterface.saveData(keyBean, Json.encodeToString(b))
        }
        Text(text = bean.toString())
        Row {
            // not null
            Button(onClick = {
                saveBean(ExampleBean(0, "not null"))
            }) {
                Text(text = "Not Null")
            }
            // id += 1
            Spacer(modifier = Modifier.width(4.dp))
            Button(onClick = {
                bean?.copy(id = (bean?.id ?: 0) + 1)?.let(saveBean)
            }) {
                Text(text = "Add id by 1")
            }
            Spacer(modifier = Modifier.width(4.dp))
            // set as null
            Button(onClick = {
                saveBean(null)
            }) {
                Text(text = "Set As Null")
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        val keyList = "sense_external_data_change_example_list"
        val list by rememberDataSaverState(
            key = keyList,
            initialValue = listOf(ExampleBean(0, "initial")),
            senseExternalDataChange = true
        )
        LazyColumn(Modifier.heightIn(0.dp, 200.dp)) {
            items(list) { item ->
                Text(modifier = Modifier.padding(8.dp), text = item.toString(), fontSize = 16.sp)
            }
            item {
                Row {
                    // add
                    Button(onClick = {
                        val l = list + ExampleBean(list.size, "add")
                        dataSaverInterface.saveData(
                            keyList,
                            Json.encodeToString(l)
                        )
                    }) {
                        Text(text = "Add")
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    // remove
                    Button(onClick = {
                        if (list.isNotEmpty()) {
                            val l = list.dropLast(1)
                            dataSaverInterface.saveData(
                                keyList,
                                Json.encodeToString(l)
                            )
                        }
                    }) {
                        Text(text = "Remove")
                    }
                }
            }
        }

    }
}

@Composable
private fun CustomTypeConverterExample() {
    Heading(text = "Custom Type Converter")
    var array by rememberDataSaverState(
        "custom_type_converter_example",
        intArrayOf(1, 2, 3, 4, 5),
        typeConverter = object : ClassTypeConverter(type = typeOf<IntArray>()) {
            override fun save(data: Any?): String {
                return (data as IntArray).joinToString(",")
            }

            override fun restore(str: String): Any {
                return str.split(",").map { it.toInt() }.toIntArray()
            }
        }
    )
    Text(text = array.joinToString(", ", prefix = "[", postfix = "]"))
    Button(onClick = {
        array = IntArray(5) {
            Random.nextInt()
        }
    }) {
        Text("Randomly Change")
    }
}

@Composable
private fun CustomSealedClassExample() {
    var themeType: ThemeType by rememberDataSaverState(
        key = "key_theme_type",
        initialValue = ThemeType.DynamicNative
    )
    Heading(text = "Saving custom Sealed Class") // 保存自定义类型的示例
    Column(
        Modifier
            .background(MaterialTheme.colors.surface, RoundedCornerShape(16.dp))
            .padding(8.dp)
    ) {
        Text(
            modifier = Modifier.semantics { heading() },
            text = "Theme",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(12.dp))
        RadioTile(text = "Default", selected = themeType == ThemeType.StaticDefault) {
            themeType = ThemeType.StaticDefault
        }
        RadioTile(text = "Dynamic", selected = themeType == ThemeType.DynamicNative) {
            themeType = ThemeType.DynamicNative
        }
    }
}

@Composable
private fun CustomCoroutineScopeAndViewModelSample() {
    Heading(text = "Use custom CoroutineScope and ViewModel")
    val vm: MainViewModel = viewModel(MainViewModel::class) { MainViewModel() }
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(value = vm.username, onValueChange = { vm.username = it }, label = {
            Text(text = "Username")
        })

        OutlinedTextField(value = vm.password, onValueChange = { vm.password = it }, label = {
            Text(text = "Password")
        })
    }
}

@Composable
private fun TimeConsumingJobExample() {
    class TimeConsumingDataSaver : DataSaverInMemory() {
        override suspend fun <T> saveDataAsync(key: String, data: T) {
            Log.i("start to save data, it takes 5s...")
            // mock time consuming, it might be HTTP request or complex data saving in real world
            delay(5000)
            super.saveDataAsync(key, data)
            Log.i("finish saving data. key=$key, data=$data")
        }
    }

    CompositionLocalProvider(LocalDataSaver provides TimeConsumingDataSaver()) {
        Heading(text = "Time consuming example, wait until finished\n You cannot go back until it finished")
        // here we pass a custom coroutineScope
        val scope = remember {
            CoroutineScope(Dispatchers.IO)
        }
        val state =
            rememberDataSaverState(
                key = "time_consuming_job",
                initialValue = 0,
                coroutineScope = scope
            )
        Button(onClick = {
            scope.launch {
                state.value = state.value + 1
            }
        }) {
            Text(text = "Submit(curr value: ${state.value})")
        }

        var showDialog by remember { mutableStateOf(false) }
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                confirmButton = {
                    Button(onClick = { showDialog = false }) {
                        Text(text = "Close")
                    }
                },
                text = {
                    Text(text = "Current job is not finished, please wait until it is finished!")
                }
            )
        }
        LaunchedEffect(key1 = state.job) {
            Log.d("ExampleComposable", "TimeConsumingJobExample job: ${state.job}")
        }

        BackHandler(state.job?.isCompleted == false) {
            showDialog = true
            Log.d("ExampleComposable", "TimeConsumingJobExample is not finished")
        }
    }
}

@Composable
fun PreviewExample() {
    var checked by rememberDataSaverState(key = "preview_string", initialValue = false)
    Switch(checked = checked, onCheckedChange = {
        checked = it
    })
}


@Composable
internal fun Heading(text: String) {
    Spacer(modifier = Modifier.height(8.dp))
    Text(text, fontWeight = FontWeight.W600, fontSize = 18.sp)
}

@Composable
private fun RadioTile(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(text = text, fontSize = 24.sp, fontWeight = FontWeight.W700)
        RadioButton(selected = selected, onClick = onClick)
    }
}

@Composable
@ReadOnlyComposable
internal expect fun getSensorExternalDataSaver(): DataSaverInterface