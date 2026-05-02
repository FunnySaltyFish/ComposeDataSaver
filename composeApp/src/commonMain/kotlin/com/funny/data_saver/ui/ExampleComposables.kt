package com.funny.data_saver.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.data_saver.Constant
import com.funny.data_saver.Constant.KEY_BEAN_EXAMPLE
import com.funny.data_saver.Constant.KEY_BOOLEAN_EXAMPLE
import com.funny.data_saver.Constant.KEY_STRING_EXAMPLE
import com.funny.data_saver.core.ClassTypeConverter
import com.funny.data_saver.core.DataSaverConfig
import com.funny.data_saver.core.DataSaverInMemory
import com.funny.data_saver.core.DataSaverInterface
import com.funny.data_saver.core.DataSaverLogEntry
import com.funny.data_saver.core.DataSaverLogLevel
import com.funny.data_saver.core.DataSaverLogs
import com.funny.data_saver.core.DataSaverMutableState
import com.funny.data_saver.core.LocalDataSaver
import com.funny.data_saver.core.SavePolicy
import com.funny.data_saver.core.getLocalDataSaverInterface
import com.funny.data_saver.core.rememberDataSaverState
import com.funny.data_saver.kmp.IO
import com.funny.data_saver.kmp.Log
import com.funny.data_saver.kmp.viewModel
import composedatasaver.composeapp.generated.resources.Res
import composedatasaver.composeapp.generated.resources.action_add
import composedatasaver.composeapp.generated.resources.action_add_id
import composedatasaver.composeapp.generated.resources.action_change_local_data
import composedatasaver.composeapp.generated.resources.action_clear
import composedatasaver.composeapp.generated.resources.action_close
import composedatasaver.composeapp.generated.resources.action_not_null
import composedatasaver.composeapp.generated.resources.action_open
import composedatasaver.composeapp.generated.resources.action_open_dialog
import composedatasaver.composeapp.generated.resources.action_randomly_change
import composedatasaver.composeapp.generated.resources.action_remove
import composedatasaver.composeapp.generated.resources.action_set_not_null
import composedatasaver.composeapp.generated.resources.action_set_null
import composedatasaver.composeapp.generated.resources.action_submit_with_value
import composedatasaver.composeapp.generated.resources.action_test_debug
import composedatasaver.composeapp.generated.resources.action_test_info
import composedatasaver.composeapp.generated.resources.add_bean_id
import composedatasaver.composeapp.generated.resources.current_boolean_value
import composedatasaver.composeapp.generated.resources.custom_scope_view_model_description
import composedatasaver.composeapp.generated.resources.custom_scope_view_model_title
import composedatasaver.composeapp.generated.resources.custom_type_converter_description
import composedatasaver.composeapp.generated.resources.custom_type_converter_title
import composedatasaver.composeapp.generated.resources.dialog_value_label
import composedatasaver.composeapp.generated.resources.example_bean_default_label
import composedatasaver.composeapp.generated.resources.example_string_initial_value
import composedatasaver.composeapp.generated.resources.hero_description
import composedatasaver.composeapp.generated.resources.hero_log_drawer_description
import composedatasaver.composeapp.generated.resources.hero_log_drawer_title
import composedatasaver.composeapp.generated.resources.hero_preview_mode_notice
import composedatasaver.composeapp.generated.resources.hero_title
import composedatasaver.composeapp.generated.resources.list_example_description
import composedatasaver.composeapp.generated.resources.list_example_item_name
import composedatasaver.composeapp.generated.resources.list_example_title
import composedatasaver.composeapp.generated.resources.log_console_empty_text
import composedatasaver.composeapp.generated.resources.log_console_empty_text_default
import composedatasaver.composeapp.generated.resources.log_drawer_description
import composedatasaver.composeapp.generated.resources.log_drawer_title
import composedatasaver.composeapp.generated.resources.log_level_debug
import composedatasaver.composeapp.generated.resources.log_level_error
import composedatasaver.composeapp.generated.resources.log_level_info
import composedatasaver.composeapp.generated.resources.log_level_label
import composedatasaver.composeapp.generated.resources.log_level_off
import composedatasaver.composeapp.generated.resources.log_level_verbose
import composedatasaver.composeapp.generated.resources.log_level_warn
import composedatasaver.composeapp.generated.resources.nullable_bean_description
import composedatasaver.composeapp.generated.resources.nullable_bean_not_null_label
import composedatasaver.composeapp.generated.resources.nullable_bean_title
import composedatasaver.composeapp.generated.resources.password_label
import composedatasaver.composeapp.generated.resources.save_when_disposed_description
import composedatasaver.composeapp.generated.resources.save_when_disposed_dialog_title
import composedatasaver.composeapp.generated.resources.save_when_disposed_initial_value
import composedatasaver.composeapp.generated.resources.saving_boolean_description
import composedatasaver.composeapp.generated.resources.saving_boolean_title
import composedatasaver.composeapp.generated.resources.saving_custom_bean_description
import composedatasaver.composeapp.generated.resources.saving_custom_bean_title
import composedatasaver.composeapp.generated.resources.saving_sealed_class_description
import composedatasaver.composeapp.generated.resources.saving_sealed_class_title
import composedatasaver.composeapp.generated.resources.saving_string_description
import composedatasaver.composeapp.generated.resources.saving_string_title
import composedatasaver.composeapp.generated.resources.section_basic_states_description
import composedatasaver.composeapp.generated.resources.section_basic_states_title
import composedatasaver.composeapp.generated.resources.section_behavior_description
import composedatasaver.composeapp.generated.resources.section_behavior_title
import composedatasaver.composeapp.generated.resources.section_collections_description
import composedatasaver.composeapp.generated.resources.section_collections_title
import composedatasaver.composeapp.generated.resources.section_scope_description
import composedatasaver.composeapp.generated.resources.section_scope_title
import composedatasaver.composeapp.generated.resources.sense_external_data_change_bean_label
import composedatasaver.composeapp.generated.resources.sense_external_data_change_description
import composedatasaver.composeapp.generated.resources.sense_external_data_change_list_added_label
import composedatasaver.composeapp.generated.resources.sense_external_data_change_list_initial_label
import composedatasaver.composeapp.generated.resources.sense_external_data_change_new_value
import composedatasaver.composeapp.generated.resources.sense_external_data_change_title
import composedatasaver.composeapp.generated.resources.sense_external_data_change_value_1
import composedatasaver.composeapp.generated.resources.sense_external_data_change_value_2
import composedatasaver.composeapp.generated.resources.sense_external_data_change_var1
import composedatasaver.composeapp.generated.resources.sense_external_data_change_var2
import composedatasaver.composeapp.generated.resources.string_value_label
import composedatasaver.composeapp.generated.resources.test_debug_log_message
import composedatasaver.composeapp.generated.resources.test_info_log_message
import composedatasaver.composeapp.generated.resources.theme_default
import composedatasaver.composeapp.generated.resources.theme_dynamic
import composedatasaver.composeapp.generated.resources.theme_label
import composedatasaver.composeapp.generated.resources.time_consuming_log_finish
import composedatasaver.composeapp.generated.resources.time_consuming_log_start
import composedatasaver.composeapp.generated.resources.time_consuming_save_description
import composedatasaver.composeapp.generated.resources.time_consuming_save_title
import composedatasaver.composeapp.generated.resources.time_consuming_wait_message
import composedatasaver.composeapp.generated.resources.username_label
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import moe.tlaster.precompose.navigation.BackHandler
import org.jetbrains.compose.resources.stringResource
import kotlin.random.Random
import kotlin.reflect.typeOf

@Serializable
data class ExampleBean(var id: Int, val label: String)

private const val DefaultExampleBeanId = 233

private data class ExampleLogItem(
    val entry: DataSaverLogEntry,
    val timeText: String
)

private fun String.replaceIndexedArgs(vararg values: Any?): String {
    var result = this
    values.forEachIndexed { index, value ->
        result = result.replace("%${index + 1}\$s", value.toString())
    }
    return result
}

@OptIn(ExperimentalSerializationApi::class)
@Composable
fun ExampleComposable() {
    val isInspectMode = LocalInspectionMode.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val drawerScope = rememberCoroutineScope()
    val defaultBean = ExampleBean(
        id = DefaultExampleBeanId,
        label = stringResource(Res.string.example_bean_default_label)
    )
    var stringExample by rememberDataSaverState(
        KEY_STRING_EXAMPLE,
        stringResource(Res.string.example_string_initial_value),
        savePolicy = SavePolicy.IMMEDIATELY,
        async = true
    )
    var booleanExample by rememberDataSaverState(
        key = KEY_BOOLEAN_EXAMPLE,
        initialValue = false
    )
    var beanExample by rememberDataSaverState(
        key = KEY_BEAN_EXAMPLE,
        initialValue = defaultBean
    )
    var logLevel by remember { mutableStateOf(DataSaverConfig.logLevel) }
    val logs = remember { mutableStateListOf<ExampleLogItem>() }

    LaunchedEffect(Unit) {
        DataSaverLogs.entries.collect { entry ->
            logs.add(0, ExampleLogItem(entry = entry, timeText = currentLogTimeText()))
            if (logs.size > 120) {
                logs.subList(120, logs.size).clear()
            }
        }
    }

    LaunchedEffect(logLevel) {
        DataSaverConfig.logLevel = logLevel
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            LogDrawerContent(
                logs = logs,
                onClose = { drawerScope.launch { drawerState.close() } },
                onClearLogs = { logs.clear() },
                logLevel = logLevel,
                onLogLevelChange = { logLevel = it }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HeroCard(
                isInspectMode = isInspectMode,
                logCount = logs.size,
                onOpenLogs = { drawerScope.launch { drawerState.open() } }
            )

            SectionCard(
                title = stringResource(Res.string.section_basic_states_title),
                description = stringResource(Res.string.section_basic_states_description)
            ) {
                ExampleCard(
                    title = stringResource(Res.string.saving_string_title),
                    description = stringResource(Res.string.saving_string_description)
                ) {
                    OutlinedTextField(
                        value = stringExample,
                        onValueChange = { stringExample = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(Res.string.string_value_label)) }
                    )
                }

                ExampleCard(
                    title = stringResource(Res.string.saving_boolean_title),
                    description = stringResource(Res.string.saving_boolean_description)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(
                                Res.string.current_boolean_value,
                                booleanExample.toString()
                            ),
                            fontSize = 15.sp
                        )
                        Switch(
                            checked = booleanExample,
                            onCheckedChange = { booleanExample = it }
                        )
                    }
                }

                ExampleCard(
                    title = stringResource(Res.string.saving_custom_bean_title),
                    description = stringResource(Res.string.saving_custom_bean_description)
                ) {
                    Text(
                        text = beanExample.toString(),
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(onClick = { beanExample = beanExample.copy(id = beanExample.id + 1) }) {
                        Text(text = stringResource(Res.string.add_bean_id))
                    }
                }

                ParcelableExample()
            }

            SectionCard(
                title = stringResource(Res.string.section_collections_title),
                description = stringResource(Res.string.section_collections_description)
            ) {
                ListExample()
                NullableExample()
                CustomTypeConverterExample()
                CustomSealedClassExample()
            }

            SectionCard(
                title = stringResource(Res.string.section_behavior_title),
                description = stringResource(Res.string.section_behavior_description)
            ) {
                SenseExternalDataChangeExample()
                SaveWhenDisposedExample()
            }

            SectionCard(
                title = stringResource(Res.string.section_scope_title),
                description = stringResource(Res.string.section_scope_description)
            ) {
                CustomCoroutineScopeAndViewModelSample()
                TimeConsumingJobExample()
            }
        }
    }
}

@Composable
expect fun ParcelableExample()

@Composable
private fun ListExample() {
    val listItemNameTemplate = stringResource(Res.string.list_example_item_name)
    val addText = stringResource(Res.string.action_add)
    val removeText = stringResource(Res.string.action_remove)
    var listExample: List<ExampleBean> by rememberDataSaverState(
        key = "key_list_example",
        initialValue = listOf(
            ExampleBean(
                id = DefaultExampleBeanId,
                label = listItemNameTemplate.replaceIndexedArgs(1)
            ),
            ExampleBean(
                id = DefaultExampleBeanId,
                label = listItemNameTemplate.replaceIndexedArgs(2)
            ),
            ExampleBean(
                id = DefaultExampleBeanId,
                label = listItemNameTemplate.replaceIndexedArgs(3)
            )
        )
    )
    ExampleCard(
        title = stringResource(Res.string.list_example_title),
        description = stringResource(Res.string.list_example_description)
    ) {
        LazyColumn(Modifier.heightIn(min = 0.dp, max = 220.dp)) {
            items(listExample) { item ->
                Text(
                    modifier = Modifier.padding(vertical = 4.dp),
                    text = item.toString(),
                    fontSize = 15.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        ActionFlowRow {
            Button(onClick = {
                listExample += ExampleBean(
                    id = DefaultExampleBeanId,
                    label = listItemNameTemplate.replaceIndexedArgs(listExample.size + 1)
                )
            }) {
                Text(text = addText)
            }
            Button(onClick = {
                if (listExample.isNotEmpty()) {
                    listExample = listExample.dropLast(1)
                }
            }) {
                Text(text = removeText)
            }
        }
    }
}

@Composable
private fun SaveWhenDisposedExample() {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = stringResource(Res.string.save_when_disposed_dialog_title)) },
            text = {
                var stringExample2 by rememberDataSaverState(
                    key = Constant.KEY_STRING_EXAMPLE_2,
                    initialValue = stringResource(Res.string.save_when_disposed_initial_value),
                    savePolicy = SavePolicy.DISPOSED,
                    async = false
                )
                OutlinedTextField(
                    value = stringExample2,
                    onValueChange = { stringExample2 = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(Res.string.dialog_value_label)) }
                )
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(text = stringResource(Res.string.action_close))
                }
            }
        )
    }

    ExampleCard(
        title = stringResource(Res.string.save_when_disposed_dialog_title),
        description = stringResource(Res.string.save_when_disposed_description)
    ) {
        Button(onClick = { showDialog = true }) {
            Text(text = stringResource(Res.string.action_open_dialog))
        }
    }
}

@Composable
private fun NullableExample() {
    val notNullLabel = stringResource(Res.string.nullable_bean_not_null_label)
    val nullableCustomBeanState: DataSaverMutableState<ExampleBean?> =
        rememberDataSaverState(key = "nullable_bean", initialValue = null)

    ExampleCard(
        title = stringResource(Res.string.nullable_bean_title),
        description = stringResource(Res.string.nullable_bean_description)
    ) {
        Text(text = nullableCustomBeanState.value.toString(), fontSize = 15.sp)
        Spacer(modifier = Modifier.height(6.dp))
        ActionFlowRow {
            Button(onClick = {
                nullableCustomBeanState.value = ExampleBean(
                    id = 100,
                    label = notNullLabel
                )
            }) {
                Text(text = stringResource(Res.string.action_set_not_null))
            }
            Button(onClick = {
                nullableCustomBeanState.value = null
            }) {
                Text(text = stringResource(Res.string.action_set_null))
            }
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Composable
private fun SenseExternalDataChangeExample() {
    val dataSaver = getSensorExternalDataSaver()
    val externalValueTemplate = stringResource(Res.string.sense_external_data_change_new_value)
    val beanLabel = stringResource(Res.string.sense_external_data_change_bean_label)
    val initialListLabel = stringResource(Res.string.sense_external_data_change_list_initial_label)
    val addedListLabel = stringResource(Res.string.sense_external_data_change_list_added_label)

    ExampleCard(
        title = stringResource(Res.string.sense_external_data_change_title),
        description = stringResource(Res.string.sense_external_data_change_description)
    ) {
        CompositionLocalProvider(LocalDataSaver provides dataSaver) {
            val key = "sense_external_data_change_example"
            val stringExample by rememberDataSaverState(
                key = key,
                initialValue = stringResource(Res.string.sense_external_data_change_value_1),
                senseExternalDataChange = true
            )
            val stringExample2 by rememberDataSaverState(
                key = key,
                initialValue = stringResource(Res.string.sense_external_data_change_value_2),
                senseExternalDataChange = true
            )
            val dataSaverInterface = getLocalDataSaverInterface()

            Text(
                text = stringResource(Res.string.sense_external_data_change_var1, stringExample),
                fontSize = 15.sp
            )
            Text(
                text = stringResource(Res.string.sense_external_data_change_var2, stringExample2),
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Button(onClick = {
                dataSaverInterface.saveData(
                    key,
                    externalValueTemplate.replaceIndexedArgs(Random.nextInt())
                )
            }) {
                Text(text = stringResource(Res.string.action_change_local_data))
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp))

            val keyBean = "sense_external_data_change_example_bean"
            val bean: ExampleBean? by rememberDataSaverState(
                key = keyBean,
                initialValue = null,
                senseExternalDataChange = true
            )
            val saveBean = { value: ExampleBean? ->
                dataSaverInterface.saveData(keyBean, Json.encodeToString(value))
            }

            Text(text = bean.toString(), fontSize = 15.sp)
            Spacer(modifier = Modifier.height(6.dp))
            ActionFlowRow {
                Button(onClick = {
                    saveBean(
                        ExampleBean(
                            0,
                            beanLabel
                        )
                    )
                }) {
                    Text(text = stringResource(Res.string.action_not_null))
                }
                Button(onClick = {
                    bean?.copy(id = (bean?.id ?: 0) + 1)?.let(saveBean)
                }) {
                    Text(text = stringResource(Res.string.action_add_id))
                }
                Button(onClick = {
                    saveBean(null)
                }) {
                    Text(text = stringResource(Res.string.action_set_null))
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp))

            val keyList = "sense_external_data_change_example_list"
            val list by rememberDataSaverState(
                key = keyList,
                initialValue = listOf(
                    ExampleBean(
                        0,
                        initialListLabel
                    )
                ),
                senseExternalDataChange = true
            )

            LazyColumn(Modifier.heightIn(min = 0.dp, max = 180.dp)) {
                items(list) { item ->
                    Text(
                        modifier = Modifier.padding(vertical = 4.dp),
                        text = item.toString(),
                        fontSize = 15.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            ActionFlowRow {
                Button(onClick = {
                    val updated = list + ExampleBean(
                        list.size,
                        addedListLabel
                    )
                    dataSaverInterface.saveData(keyList, Json.encodeToString(updated))
                }) {
                    Text(text = stringResource(Res.string.action_add))
                }
                Button(onClick = {
                    if (list.isNotEmpty()) {
                        val updated = list.dropLast(1)
                        dataSaverInterface.saveData(keyList, Json.encodeToString(updated))
                    }
                }) {
                    Text(text = stringResource(Res.string.action_remove))
                }
            }
        }
    }
}

@Composable
private fun CustomTypeConverterExample() {
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

    ExampleCard(
        title = stringResource(Res.string.custom_type_converter_title),
        description = stringResource(Res.string.custom_type_converter_description)
    ) {
        Text(
            text = array.joinToString(", ", prefix = "[", postfix = "]"),
            fontSize = 15.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Button(onClick = {
            array = IntArray(5) { Random.nextInt(0, 99) }
        }) {
            Text(stringResource(Res.string.action_randomly_change))
        }
    }
}

@Composable
private fun CustomSealedClassExample() {
    var themeType: ThemeType by rememberDataSaverState(
        key = "key_theme_type",
        initialValue = ThemeType.DynamicNative
    )

    ExampleCard(
        title = stringResource(Res.string.saving_sealed_class_title),
        description = stringResource(Res.string.saving_sealed_class_description)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                Text(
                    text = stringResource(Res.string.theme_label),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                RadioTile(
                    text = stringResource(Res.string.theme_default),
                    selected = themeType == ThemeType.StaticDefault
                ) {
                    themeType = ThemeType.StaticDefault
                }
                RadioTile(
                    text = stringResource(Res.string.theme_dynamic),
                    selected = themeType == ThemeType.DynamicNative
                ) {
                    themeType = ThemeType.DynamicNative
                }
            }
        }
    }
}

@Composable
private fun CustomCoroutineScopeAndViewModelSample() {
    val vm: MainViewModel = viewModel(MainViewModel::class) { MainViewModel() }

    ExampleCard(
        title = stringResource(Res.string.custom_scope_view_model_title),
        description = stringResource(Res.string.custom_scope_view_model_description)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = vm.username,
                onValueChange = { vm.username = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = stringResource(Res.string.username_label)) }
            )
            OutlinedTextField(
                value = vm.password,
                onValueChange = { vm.password = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = stringResource(Res.string.password_label)) }
            )
        }
    }
}

@Composable
private fun TimeConsumingJobExample() {
    val startLogMessage = stringResource(Res.string.time_consuming_log_start)
    val finishLogTemplate = stringResource(Res.string.time_consuming_log_finish)

    class TimeConsumingDataSaver(
        private val startLogMessage: String,
        private val finishLogTemplate: String
    ) : DataSaverInMemory() {
        override suspend fun <T> saveDataAsync(key: String, data: T) {
            Log.i("ExampleComposable", startLogMessage)
            delay(5000)
            super.saveDataAsync(key, data)
            Log.i(
                "ExampleComposable",
                finishLogTemplate.replaceIndexedArgs(key, data.toString())
            )
        }
    }

    ExampleCard(
        title = stringResource(Res.string.time_consuming_save_title),
        description = stringResource(Res.string.time_consuming_save_description)
    ) {
        CompositionLocalProvider(
            LocalDataSaver provides TimeConsumingDataSaver(startLogMessage, finishLogTemplate)
        ) {
            val scope = remember { CoroutineScope(Dispatchers.IO) }
            val state = rememberDataSaverState(
                key = "time_consuming_job",
                initialValue = 0,
                coroutineScope = scope
            )

            Button(onClick = {
                scope.launch {
                    state.value = state.value + 1
                }
            }) {
                Text(
                    text = stringResource(
                        Res.string.action_submit_with_value,
                        state.value.toString()
                    )
                )
            }

            var showDialog by remember { mutableStateOf(false) }
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    confirmButton = {
                        Button(onClick = { showDialog = false }) {
                            Text(text = stringResource(Res.string.action_close))
                        }
                    },
                    text = {
                        Text(text = stringResource(Res.string.time_consuming_wait_message))
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
}

@Composable
fun PreviewExample() {
    var checked by rememberDataSaverState(key = "preview_string", initialValue = false)
    Switch(
        checked = checked,
        onCheckedChange = { checked = it }
    )
}

@Composable
internal fun ExampleCard(
    title: String,
    description: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (description != null) {
                    Text(
                        text = description,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                content()
            }
        )
    }
}

@Composable
private fun HeroCard(
    isInspectMode: Boolean,
    logCount: Int,
    onOpenLogs: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(Res.string.hero_title),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(Res.string.hero_description),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.hero_log_drawer_title),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = stringResource(Res.string.hero_log_drawer_description),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = logCount.toString(),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    TextButton(onClick = onOpenLogs) {
                        Text(stringResource(Res.string.action_open))
                    }
                }
            }
            if (isInspectMode) {
                Text(
                    text = stringResource(Res.string.hero_preview_mode_notice),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    description: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.025f),
        shape = RoundedCornerShape(26.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
            )
            HorizontalDivider()
            content()
        }
    }
}

@Composable
private fun LogDrawerContent(
    logs: List<ExampleLogItem>,
    onClose: () -> Unit,
    onClearLogs: () -> Unit,
    logLevel: DataSaverLogLevel,
    onLogLevelChange: (DataSaverLogLevel) -> Unit
) {
    val debugLogMessage = stringResource(Res.string.test_debug_log_message)
    val infoLogMessage = stringResource(Res.string.test_info_log_message)
    ModalDrawerSheet(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.88f)
            .widthIn(max = 360.dp),
        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerTonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(Res.string.log_drawer_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(Res.string.log_drawer_description),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                TextButton(onClick = onClose) {
                    Text(stringResource(Res.string.action_close))
                }
            }

            LogLevelSelector(
                selectedLevel = logLevel,
                onLevelSelected = onLogLevelChange
            )

            ActionFlowRow {
                SidebarActionButton(
                    text = stringResource(Res.string.action_clear),
                    onClick = onClearLogs
                )
                SidebarActionButton(
                    text = stringResource(Res.string.action_test_debug),
                    onClick = {
                        Log.d("ExampleComposable", debugLogMessage)
                    }
                )
                SidebarActionButton(
                    text = stringResource(Res.string.action_test_info),
                    onClick = {
                        Log.i("ExampleComposable", infoLogMessage)
                    }
                )
            }

            LogConsole(
                logs = logs,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                emptyText = stringResource(Res.string.log_console_empty_text)
            )
        }
    }
}

@Composable
private fun SidebarActionButton(
    text: String,
    onClick: () -> Unit
) {
    Button(onClick = onClick) {
        Text(text)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActionFlowRow(
    content: @Composable () -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = { content() }
    )
}

@Composable
private fun LogLevelSelector(
    selectedLevel: DataSaverLogLevel,
    onLevelSelected: (DataSaverLogLevel) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(Res.string.log_level_label, selectedLevel.displayName()),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = selectedLevel.displayName(),
                        color = selectedLevel.color()
                    )
                    Text(
                        text = "v",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DataSaverLogLevel.entries.forEach { level ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = level.displayName(),
                                color = level.color()
                            )
                        },
                        onClick = {
                            onLevelSelected(level)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LogConsole(
    logs: List<ExampleLogItem>,
    modifier: Modifier = Modifier,
    emptyText: String? = null
) {
    val resolvedEmptyText = emptyText ?: stringResource(Res.string.log_console_empty_text_default)
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
        shape = RoundedCornerShape(16.dp)
    ) {
        if (logs.isEmpty()) {
            Text(
                text = resolvedEmptyText,
                modifier = Modifier.padding(16.dp),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(logs) { entry ->
                    Column {
                        Text(
                            text = "${entry.timeText}  [${entry.entry.level.displayName()}] ${entry.entry.tag}",
                            fontSize = 12.sp,
                            color = entry.entry.level.color(),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = entry.entry.message,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f)
                        )
                    }
                }
            }
        }
    }
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
            .padding(vertical = 4.dp)
    ) {
        Text(text = text, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        RadioButton(selected = selected, onClick = onClick)
    }
}

@Composable
private fun DataSaverLogLevel.displayName(): String = when (this) {
    DataSaverLogLevel.NONE -> stringResource(Res.string.log_level_off)
    DataSaverLogLevel.ERROR -> stringResource(Res.string.log_level_error)
    DataSaverLogLevel.WARNING -> stringResource(Res.string.log_level_warn)
    DataSaverLogLevel.INFO -> stringResource(Res.string.log_level_info)
    DataSaverLogLevel.DEBUG -> stringResource(Res.string.log_level_debug)
    DataSaverLogLevel.VERBOSE -> stringResource(Res.string.log_level_verbose)
}

@Composable
private fun DataSaverLogLevel.color(): Color = when (this) {
    DataSaverLogLevel.NONE -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    DataSaverLogLevel.ERROR -> Color(0xFFB3261E)
    DataSaverLogLevel.WARNING -> Color(0xFF9A6700)
    DataSaverLogLevel.INFO -> Color(0xFF0B57D0)
    DataSaverLogLevel.DEBUG -> Color(0xFF2E7D32)
    DataSaverLogLevel.VERBOSE -> Color(0xFF6A1B9A)
}

@Composable
@ReadOnlyComposable
internal expect fun getSensorExternalDataSaver(): DataSaverInterface

internal expect fun currentLogTimeText(): String
