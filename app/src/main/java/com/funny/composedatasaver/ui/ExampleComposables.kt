package com.funny.composedatasaver.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.composedatasaver.Constant
import com.funny.composedatasaver.Constant.KEY_BEAN_EXAMPLE
import com.funny.composedatasaver.Constant.KEY_BOOLEAN_EXAMPLE
import com.funny.composedatasaver.Constant.KEY_STRING_EXAMPLE
import com.funny.composedatasaver.ExampleParcelable
import com.funny.data_saver.core.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

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
// @Preview
fun ExampleComposable() {
    // get dataSaver                          | 获取 DataSaverInterface
    // you can use this to save data manually | 您可以使用此变量做手动保存
    val dataSaverInterface = LocalDataSaver.current

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

    var booleanExample by rememberDataSaverState(KEY_BOOLEAN_EXAMPLE, false)

    var beanExample by rememberDataSaverState(KEY_BEAN_EXAMPLE, default = EmptyBean)

    var themeType: ThemeType by rememberDataSaverState(
        key = "key_theme_type",
        default = ThemeType.DynamicNative
    )

    var listExample by rememberDataSaverListState(
        key = "key_list_example", default = listOf(
            EmptyBean.copy(label = "Name 1"),
            EmptyBean.copy(label = "Name 2"),
            EmptyBean.copy(label = "Name 3")
        )
    )

    // Among our basic implementations, only MMKV supports `Parcelable` by default
    var parcelableExample by rememberDataSaverState(
        key = "parcelable_example",
        default = ExampleParcelable("FunnySaltyFish", 20)
    )

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Heading(text = "Auto Save Examples:")
        Text(text = "This is an example of saving String") // 保存字符串的示例
        OutlinedTextField(value = stringExample, onValueChange = {
            stringExample = it
        })

        Text(text = "This is an example of saving Boolean") // 保存布尔值的示例
        Switch(checked = booleanExample, onCheckedChange = {
            booleanExample = it
        })

        Text(text = "This is an example of saving Parcelable") // 保存布尔值的示例
        Text(parcelableExample.toString())
        Button(onClick = {
            parcelableExample = parcelableExample.copy(age = parcelableExample.age + 1)
        }) {
            Text(text = "Add age by 1")
        }

        Text(text = "This is an example of saving custom Data Bean") // 保存自定义类型的示例
        Text(text = beanExample.toString())
        Button(onClick = {
            beanExample = beanExample.copy(id = beanExample.id + 1)
        }) {
            Text(text = "Add bean's id") // id自加
        }

        Text(text = "This is an example of saving custom Sealed Class") // 保存自定义类型的示例
        Column(
            Modifier
                .background(MaterialTheme.colors.surface, RoundedCornerShape(16.dp))
                .padding(8.dp)) {
            Text(
                modifier = Modifier.semantics { heading() },
                text = "主题/Theme",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            RadioTile(text = "默认", selected = themeType == ThemeType.StaticDefault) {
                themeType = ThemeType.StaticDefault
            }
            RadioTile(text = "动态取色", selected = themeType == ThemeType.DynamicNative) {
                themeType = ThemeType.DynamicNative
            }
        }


        val nullableCustomBeanState: DataSaverMutableState<ExampleBean?> = rememberDataSaverState(key = "nullable_bean", initialValue = null)
        Text(text = "This is an example of saving custom Data Bean(nullable)") // 保存自定义类型的示例
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


        Spacer(modifier = Modifier.height(16.dp))
        Heading(text = "Save-When-Disposed Examples:")
        SaveWhenDisposedExample()

        Spacer(modifier = Modifier.height(16.dp))
        Heading(text = "List Example")
        LazyColumn(Modifier.heightIn(0.dp, 400.dp)) {
            items(listExample) { item ->
                Text(modifier = Modifier.padding(8.dp), text = item.toString(), fontSize = 16.sp)
            }
            item {
                Row {
                    Button(onClick = {
                        listExample =
                            listExample + EmptyBean.copy(label = "Name ${listExample.size + 1}")
                    }) {
                        Text(text = "Add To List")
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Button(onClick = {
                        if (listExample.isNotEmpty()) listExample = listExample.dropLast(1)
                    }) {
                        Text(text = "Remove From List")
                    }
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
    Button(onClick = { showDialog = true }) {
        Text(text = "Click Me To Open Dialog")
    }
}

@Preview
@Composable
fun PreviewExample() {
    var checked by rememberDataSaverState(key = "preview_string", initialValue = false)
    Switch(checked = checked, onCheckedChange = {
        checked = it
    })
}


@Composable
private fun Heading(text: String) {
    Text(text, fontWeight = FontWeight.W600, fontSize = 18.sp)
}

@Composable
private fun RadioTile(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)) {
        Text(text = text, fontSize = 24.sp, fontWeight = FontWeight.W700)
        RadioButton(selected = selected, onClick = onClick)
    }
}