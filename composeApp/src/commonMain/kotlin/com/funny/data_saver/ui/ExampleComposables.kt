package com.funny.data_saver.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DrawerValue
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import moe.tlaster.precompose.navigation.BackHandler
import kotlin.random.Random
import kotlin.reflect.typeOf

@Serializable
data class ExampleBean(var id: Int, val label: String)

val EmptyBean = ExampleBean(233, "FunnySaltyFish")

private data class ExampleLogItem(
    val entry: DataSaverLogEntry,
    val timeText: String
)

@OptIn(ExperimentalSerializationApi::class)
@Composable
fun ExampleComposable() {
    val isInspectMode = LocalInspectionMode.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val drawerScope = rememberCoroutineScope()
    var stringExample by rememberDataSaverState(
        KEY_STRING_EXAMPLE,
        "FunnySaltyFish, tap to input",
        savePolicy = SavePolicy.IMMEDIATELY,
        async = true
    )
    var booleanExample by rememberDataSaverState(
        key = KEY_BOOLEAN_EXAMPLE,
        initialValue = false
    )
    var beanExample by rememberDataSaverState(
        key = KEY_BEAN_EXAMPLE,
        initialValue = EmptyBean
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
                title = "Basic States",
                description = "展示常见的 String、Boolean、Bean 与 Parcelable 保存方式。"
            ) {
                ExampleCard(
                    title = "Saving String",
                    description = "输入内容后会立即保存。"
                ) {
                    OutlinedTextField(
                        value = stringExample,
                        onValueChange = { stringExample = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("String Value") }
                    )
                }

                ExampleCard(
                    title = "Saving Boolean",
                    description = "切换状态后自动持久化。"
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (booleanExample) "当前值：true" else "当前值：false",
                            fontSize = 15.sp
                        )
                        Switch(
                            checked = booleanExample,
                            onCheckedChange = { booleanExample = it }
                        )
                    }
                }

                ExampleCard(
                    title = "Saving Custom Bean",
                    description = "通过类型转换器保存自定义对象。"
                ) {
                    Text(
                        text = beanExample.toString(),
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(onClick = { beanExample = beanExample.copy(id = beanExample.id + 1) }) {
                        Text(text = "Add bean id")
                    }
                }

                ParcelableExample()
            }

            SectionCard(
                title = "Collections And Types",
                description = "集中查看集合、可空值、自定义转换器和密封类的保存效果。"
            ) {
                ListExample()
                NullableExample()
                CustomTypeConverterExample()
                CustomSealedClassExample()
            }

            SectionCard(
                title = "Behavior Samples",
                description = "包含外部数据变化感知和页面销毁时保存等行为。"
            ) {
                SenseExternalDataChangeExample()
                SaveWhenDisposedExample()
            }

            SectionCard(
                title = "Scope And Async",
                description = "包含 ViewModel、自定义协程域和耗时任务示例。"
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
    var listExample: List<ExampleBean> by rememberDataSaverState(
        key = "key_list_example",
        initialValue = listOf(
            EmptyBean.copy(label = "Name 1"),
            EmptyBean.copy(label = "Name 2"),
            EmptyBean.copy(label = "Name 3")
        )
    )
    ExampleCard(
        title = "List Example",
        description = "列表内容的增删会被完整持久化。"
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
                listExample += EmptyBean.copy(label = "Name ${listExample.size + 1}")
            }) {
                Text(text = "Add")
            }
            Button(onClick = {
                if (listExample.isNotEmpty()) {
                    listExample = listExample.dropLast(1)
                }
            }) {
                Text(text = "Remove")
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
            title = { Text(text = "Save When Disposed") },
            text = {
                var stringExample2 by rememberDataSaverState(
                    key = Constant.KEY_STRING_EXAMPLE_2,
                    initialValue = "this one will be saved only when disposed",
                    savePolicy = SavePolicy.DISPOSED,
                    async = false
                )
                OutlinedTextField(
                    value = stringExample2,
                    onValueChange = { stringExample2 = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Dialog Value") }
                )
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(text = "Close")
                }
            }
        )
    }

    ExampleCard(
        title = "Save When Disposed",
        description = "对话框关闭时才触发保存，适合批量编辑后一次落盘。"
    ) {
        Button(onClick = { showDialog = true }) {
            Text(text = "Open Dialog")
        }
    }
}

@Composable
private fun NullableExample() {
    val nullableCustomBeanState: DataSaverMutableState<ExampleBean?> =
        rememberDataSaverState(key = "nullable_bean", initialValue = null)

    ExampleCard(
        title = "Nullable Bean",
        description = "可空对象同样可以保存和恢复。"
    ) {
        Text(text = nullableCustomBeanState.value.toString(), fontSize = 15.sp)
        Spacer(modifier = Modifier.height(6.dp))
        ActionFlowRow {
            Button(onClick = {
                nullableCustomBeanState.value = ExampleBean(id = 100, label = "I'm not null")
            }) {
                Text(text = "Set Not Null")
            }
            Button(onClick = {
                nullableCustomBeanState.value = null
            }) {
                Text(text = "Set Null")
            }
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Composable
private fun SenseExternalDataChangeExample() {
    val dataSaver = getSensorExternalDataSaver()

    ExampleCard(
        title = "Sense External Data Change",
        description = "直接改底层存储，界面会感知到变化并同步更新。"
    ) {
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

            Text(text = "var1: $stringExample", fontSize = 15.sp)
            Text(text = "var2: $stringExample2", fontSize = 15.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Button(onClick = {
                dataSaverInterface.saveData(key, "Hello World ${Random.nextInt()}")
            }) {
                Text(text = "Change Local Data")
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
                    saveBean(ExampleBean(0, "not null"))
                }) {
                    Text(text = "Not Null")
                }
                Button(onClick = {
                    bean?.copy(id = (bean?.id ?: 0) + 1)?.let(saveBean)
                }) {
                    Text(text = "Add Id")
                }
                Button(onClick = {
                    saveBean(null)
                }) {
                    Text(text = "Set Null")
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp))

            val keyList = "sense_external_data_change_example_list"
            val list by rememberDataSaverState(
                key = keyList,
                initialValue = listOf(ExampleBean(0, "initial")),
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
                    val updated = list + ExampleBean(list.size, "add")
                    dataSaverInterface.saveData(keyList, Json.encodeToString(updated))
                }) {
                    Text(text = "Add")
                }
                Button(onClick = {
                    if (list.isNotEmpty()) {
                        val updated = list.dropLast(1)
                        dataSaverInterface.saveData(keyList, Json.encodeToString(updated))
                    }
                }) {
                    Text(text = "Remove")
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
        title = "Custom Type Converter",
        description = "为非默认支持的类型自定义序列化逻辑。"
    ) {
        Text(
            text = array.joinToString(", ", prefix = "[", postfix = "]"),
            fontSize = 15.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Button(onClick = {
            array = IntArray(5) { Random.nextInt(0, 99) }
        }) {
            Text("Randomly Change")
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
        title = "Saving Sealed Class",
        description = "密封类也可以通过转换器稳定地保存。"
    ) {
        Surface(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                Text(
                    text = "Theme",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                RadioTile(
                    text = "Default",
                    selected = themeType == ThemeType.StaticDefault
                ) {
                    themeType = ThemeType.StaticDefault
                }
                RadioTile(
                    text = "Dynamic",
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
        title = "Custom CoroutineScope And ViewModel",
        description = "状态创建在 ViewModel 内，协程也绑定到 ViewModel 生命周期。"
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = vm.username,
                onValueChange = { vm.username = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Username") }
            )
            OutlinedTextField(
                value = vm.password,
                onValueChange = { vm.password = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Password") }
            )
        }
    }
}

@Composable
private fun TimeConsumingJobExample() {
    class TimeConsumingDataSaver : DataSaverInMemory() {
        override suspend fun <T> saveDataAsync(key: String, data: T) {
            Log.i("ExampleComposable", "start to save data, it takes 5s...")
            delay(5000)
            super.saveDataAsync(key, data)
            Log.i("ExampleComposable", "finish saving data. key=$key, data=$data")
        }
    }

    ExampleCard(
        title = "Time Consuming Save",
        description = "模拟慢速存储，保存未完成前会拦截返回动作。"
    ) {
        CompositionLocalProvider(LocalDataSaver provides TimeConsumingDataSaver()) {
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
                Text(text = "Submit (curr value: ${state.value})")
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
                text = "ComposeDataSaver Examples",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "集中展示 ComposeDataSaver 的常见保存场景，可直接交互查看效果。",
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
                            text = "日志抽屉",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "点这里打开，或从页面左侧向右滑动查看日志。",
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
                        Text("打开")
                    }
                }
            }
            if (isInspectMode) {
                Text(
                    text = "当前为预览模式，使用内存存储。",
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
                        text = "运行日志",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "查看读写日志和示例中的主动日志。",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                TextButton(onClick = onClose) {
                    Text("关闭")
                }
            }

            LogLevelSelector(
                selectedLevel = logLevel,
                onLevelSelected = onLogLevelChange
            )

            ActionFlowRow {
                SidebarActionButton(
                    text = "清空",
                    onClick = onClearLogs
                )
                SidebarActionButton(
                    text = "测试 Debug",
                    onClick = { Log.d("ExampleComposable", "测试 Debug 日志") }
                )
                SidebarActionButton(
                    text = "测试 Info",
                    onClick = { Log.i("ExampleComposable", "测试 Info 日志") }
                )
            }

            LogConsole(
                logs = logs,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                emptyText = "暂时没有日志，操作主页面示例后可在这里查看。"
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
            text = "日志级别：${selectedLevel.displayName()}",
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
    emptyText: String = "暂无日志。操作左侧示例后可在这里查看。"
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
        shape = RoundedCornerShape(16.dp)
    ) {
        if (logs.isEmpty()) {
            Text(
                text = emptyText,
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

private fun DataSaverLogLevel.displayName(): String = when (this) {
    DataSaverLogLevel.NONE -> "Off"
    DataSaverLogLevel.ERROR -> "Error"
    DataSaverLogLevel.WARNING -> "Warn"
    DataSaverLogLevel.INFO -> "Info"
    DataSaverLogLevel.DEBUG -> "Debug"
    DataSaverLogLevel.VERBOSE -> "Verbose"
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
