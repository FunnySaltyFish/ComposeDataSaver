# ComposeDataSaver

| [![Maven Central](https://img.shields.io/maven-central/v/io.github.FunnySaltyFish/data-saver-core)](https://central.sonatype.com/artifact/io.github.FunnySaltyFish/data-saver-core) | [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0) |
|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------| ------------------------------------------------------------ |

| [English Version](README_en.md) |

优雅地在 Compose Multiplatform ( Android / JVM Desktop ) 中完成数据持久化

```kotlin
// booleanExample 初始化值为false
// 之后会自动读取本地数据
var booleanExample by rememberDataSaverState(KEY_BOOLEAN_EXAMPLE, false)
// 直接赋值即可完成持久化
booleanExample = true
```


- :tada: 简洁：近似原生Compose的写法
- :tada: 低耦合：抽象接口，不限制底层保存算法实现
- :tada: 轻巧：默认不引入除Compose外任何第三方库（主体jar包约**10kb**，可选的实现仅**1kb**）
- :tada: 强大：支持基本的数据类型和自定义类型、支持List类型

**注：此库是对Compose中使用其他框架（比如 Preference、MMKV、DataStore）的封装，不是一个单独的数据保存框架**。您可以参考[此链接](https://juejin.cn/post/7144750071156834312)以了解它的设计思想。



<img src="screenshot.png" alt="Example" style="zoom: 15%;" />

您可以点击 [这里下载demo体验](demo.apk)（Debug 包，相较于 release 包较卡顿）

---

## 引入

在`settings.gradle`引入jitpack仓库位置

```bash
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
```

在项目`build.gradle`引入

```bash
dependencies {
    implementation "io.github.FunnySaltyFish:data-saver-core:{version}"
}
```

> 注意：自 v1.2.0 起，仓库转为 Compose Multiplatform，发布至 Maven Central，Group Id 也有改变。从 v1.2.0 之前升级版本时请注意更改

## 示例代码
以下介绍的示例代码均可在 [这里](composeApp/src/commonMain/kotlin/com/funny/data_saver/ui/ExampleComposables.kt) 查看具体实现

## 基本使用

项目使用`DataSaverInterface`的实现类来保存数据，因此您需要先提供一个此类对象。

### Android
项目默认包含了使用`Preference`保存数据的实现类`DataSaverPreferences`，可如下初始化：

```kotlin
// init preferences
val dataSaverPreferences = DataSaverPreferences(applicationContext)
CompositionLocalProvider(LocalDataSaver provides dataSaverPreferences){
	ExampleComposable()
}
```

### JVM Desktop
默认包含了基于 `java.util.Properties` 的实现类 `DataSaverProperties`，您可以如下初始化：

```kotlin
// init properties
val dataSaver = DataSaverProperties("$userHome/$projectName/$filename")
CompositionLocalProvider(LocalDataSaver provides dataSaver){
    ExampleComposable()
}
```

此后在`ExampleComposable`及其子微件内部可使用`LocalDataSaver.current`获取当前实例

对于基本数据类型（如String/Int/Boolean）：

```kotlin
// booleanExample 初始化值为false
// 之后会自动读取本地数据
var booleanExample by rememberDataSaverState(KEY_BOOLEAN_EXAMPLE, false)
// 直接赋值即可完成持久化
booleanExample = true
```

v1.1.0 起新增了对列表的支持，使用方式为：

```kotlin
var listExample by rememberDataSaverListState(key = "key_list_example", default = listOf(...))
// 删除一个
onClick = { listExample = listExample.dropLast(1) }
```

通过赋值，数据即可自动转换、存于本地。就这么简单！


## 在Composable函数外使用

有些情况下，您可能需要将 `DataSaverState` 置于 `@Composable` 函数外面，比如放在 `ViewModel` 中。v1.1.0提供了 `mutableDataSavarStateOf` 函数用于此用途，该函数将会自动读取并转换已保存的值，并返回State。

此函数签名如下：

```Kotlin
/**
 * This function READ AND CONVERT the saved data and return a [DataSaverMutableState].
 * Check the example in `README.md` to see how to use it.
 *
 * 此函数 **读取并转换** 已保存的数据，返回 [DataSaverMutableState]
 *
 * @param key String 键
 * @param initialValue T 如果本地还没保存过值，此值将作为初始值；其他情况下会读取已保存值
 * @param savePolicy 管理是否。何时做持久化操作，见 [SavePolicy]
 * @param async 是否异步做持久化
 * @return DataSaverMutableState<T>
 *
 * @see DataSaverMutableState
 */
inline fun <reified T> mutableDataSaverStateOf(
    dataSaverInterface: DataSaverInterface,
    key: String,
    initialValue: T,
    savePolicy: SavePolicy = SavePolicy.IMMEDIATELY,
    async: Boolean = true
): DataSaverMutableState<T>
```


## 自定义存储框架

我们提供了基于 [MMKV](https://github.com/Tencent/MMKV) 或者 [DataStorePreference](https://developer.android.google.cn/jetpack/androidx/releases/datastore) 的简单实现

### MMKV

1. 在上述依赖基础上，额外添加

```bash
// if you want to use mmkv
implementation "io.github.FunnySaltyFish:data-saver-mmkv:{tag}"
implementation 'com.tencent:mmkv:1.2.14'
```

2. 如下初始化

```kotlin
MMKV.initialize(applicationContext)
...

val dataSaverMMKV = DefaultDataSaverMMKV
// DefaultDataSaverMMKV 是我们提供的默认实现，您可以在任何地方使用它，就像一个 MMKVUtils 那样
// 如果有定制 MMKV 的需要，可以选择 DataSaverMMKV(MMKV.defaultMMKV())

CompositionLocalProvider(LocalDataSaver provides dataSaverMMKV){
    // ...
}
```

---

### DataStorePreference

1. 在上述依赖基础上，额外添加

```bash
// if you want to use DataStore
implementation "io.github.FunnySaltyFish:data-saver-data-store-preferences:{tag}"
def data_store_version = "1.0.0"
implementation "androidx.datastore:datastore:$data_store_version"
implementation "androidx.datastore:datastore-preferences:$data_store_version"
```

2. 如下初始化

```kotlin
val Context.dataStore : DataStore<Preferences> by preferencesDataStore("dataStore")
val dataSaverDataStorePreferences = DataSaverDataStorePreferences(applicationContext.dataStore)

CompositionLocalProvider(LocalDataSaver provides dataSaverDataStorePreferences){
    // ...
}
```



四者默认支持的类型如下所示

|    类型     | DataSaverPreference | DataSaverMMKV | DataSaverDataStorePreferences | DataSaverProperties |
|:---------:|:-------------------:|:-------------:|:-----------------------------:|:-------------------:|
|    Int    |          Y          |       Y       |               Y               |          Y          |
|  Boolean  |          Y          |       Y       |               Y               |          Y          |
|  String   |          Y          |       Y       |               Y               |          Y          |
|   Long    |          Y          |       Y       |               Y               |          Y          |
|   Float   |          Y          |       Y       |               Y               |          Y          |
|  Double   |                     |       Y       |               Y               |          Y          |
| Parceable |                     |       Y       |                               |                     |
| ByteArray |                     |       Y       |                               |                     |


更多类型的支持请参见 [保存自定义类型](#保存自定义类型)

---

### 使用其他存储框架

只需要实现`DataSaverInterface`类，并重写`saveData`和`readData`方法分别用于保存数据和读取数据。对于一些支持协程的框架（如DataStore），您也可以重写`saveDataAsync`以实现异步的保存

```kotlin
abstract class DataSaverInterface(val senseExternalDataChange: Boolean = false) {
    abstract fun <T> saveData(key: String, data: T)
    abstract fun <T> readData(key: String, default: T): T
    open suspend fun <T> saveDataAsync(key: String, data: T) = saveData(key, data)
    abstract fun remove(key: String)
    abstract fun contains(key: String): Boolean

    var externalDataChangedFlow: MutableSharedFlow<Pair<String, Any?>>? =
        if (senseExternalDataChange) MutableSharedFlow(replay = 1) else null
}
```

然后将LocalDataSaver提供的对象更改为您自己的类实例

```kotlin
val dataSaverXXX = DataSaverXXX()
CompositionLocalProvider(LocalDataSaver provides dataSaverXXX){
    ExampleComposable()
}
```

后续相同使用即可。




## 保存自定义类型

自`1.1.0`起，对自定义类型提供了更完善的支持。

因为默认的`DataSaverPreferences`并不提供自定义类型的保存（当尝试这样做时会报错），所以您可以从以下两种方式中**任选其一**以保存自定义数据。


1. 通过`DataSaverConverter.registerTypeConverters`将实体类序列化为 String 再储存
2. 重写自己的`DataSaverInterface`实现类（见上）并实现相关的保存方法

对于第一种方式，您需要为对应实体类添加转换器，以实现保存时自动转换为String、并从String还原。方法如下：

```kotlin
@Serializable
data class ExampleBean(var id:Int, val label:String)
// ------------ //

// 在初始化时调用registerTypeConverters方法注册对应转换方法
// 该方法接收两个参数：分别用于 转成可序列化类型以保存 和 反序列化为您的Bean
// 此处使用 Json.encodeToString 和 Json.decodeFromString， 您也可以用 Gson、Fastjson 等
registerTypeConverters<ExampleBean>(
	save = { bean -> Json.encodeToString(bean) },
	restore = { str -> Json.decodeFromString(str) }
)
```

通过注册类型转换器，框架即可在remember和save时自动尝试转换。甚至，如果您为 `ExampleBean` 注册了转换器，那么 `List<ExampleBean>` **也将自动得到支持**（通过 `rememberDataSaverListState` ）



完整例子见 [示例项目](composeApp/src/commonMain/kotlin/com/funny/data_saver/AppConfig.kt)

## 感知外部数据变化
自 v1.1.6 起，框架加入了**有限的对外部数据变化感知的支持**，具体来说，就是当您在外部修改了某个key对应的值时，框架会自动感知到并更新对应的`MutableDataSaverState`，从而触发Composable的更新。

目前，仅有 `rememberDataSaverState` 和 `rememberDataSaverListState` 支持此功能，您需要设置 `senseExternalDataChange` 参数为 `true`。同时，对应的 `DataSaverInterface` 也需要设置 `senseExternalDataChange` 为 true

```kotlin
val dataSaverXXX = DataSaverXXX(senseExternalDataChange = true)
CompositionLocalProvider(LocalDataSaver provides dataSaverXXX){
    val stringExample by rememberDataSaverState(
        key = key,
        initialValue = "Hello World(1)",
        senseExternalDataChange = true
    )
        ...
    onClick = {
        // 外部修改了key对应的值，此时Composable会自动更新
        dataSaverXXX.saveData(key, "Hello World(2)")
    }
}
```
其中，MMKV 本身不支持感知数据变化，因此它的数据变化是 `DataSaverMMKV` 手动提交的。如果你在使用 MMKV 时需要感知数据变化，那么需要调用 `DataSaverMMKV::saveData` 来做数据保存才可以。

请注意，当新数据为 null 时，会有以下情况：
- 当使用 `rememberDataSaverState` 时
  - 如果 T 为可空类型，比如 ExampleBean? ，那么正确的设置为 null
  - 如果 T 为非空类型，比如 ExampleBean ，那么 State 的 value 会重新变为 initialValue
- 当使用 `rememberDataSaverListState` 时
  - State 的 value 会重新变为 initialValue

## 高级设置

### 控制保存策略

v1.1.0 将原先的 `autoSave` 升级为了 `savePolicy`，以控制是否做、什么时候做数据持久化。`mutableDataSaverStateOf`、`rememberDataSaverState` 均包含此参数，默认为`IMEDIATELY`

该类目前包含下面三种值：

```Kotlin
open class SavePolicy {
    /**
     * 默认模式，每次给state的value赋新值时就做持久化
     */
    object IMMEDIATELY : SavePolicy()

    /**
     * Composable `onDispose` 时做数据持久化，适合数据变动比较频繁、且此Composable会进入onDispose的情况。
     * **慎用此模式，因为有些情况下onDispose不会被回调**
     */
    object DISPOSED: SavePolicy()

    /**
     * 不会自动做持久化操作，请按需自行调用`state.saveData()`。
     * Example: `onClick = { state.saveData() }`
     */
    object NEVER : SavePolicy()
}
```



### 设置库参数

目前，库提供了一些可以设置的参数，它们位于`DataSaverConfig`下

```Kotlin
/**
 * 1. DEBUG: 是否输出库的调试信息
 * 2. LIST_SEPARATOR: 内置的 列表转字符串 使用的分隔符，默认为'#@#'。(**请不要使用 ','，因为单个bean 序列化后的json中会包含它** )
 */
object DataSaverConfig {
    var DEBUG = true
    var LIST_SEPARATOR = "#@#"
}
```



### 异步保存

v1.1.0 对`DataSaverInterface` 新增了 `suspend fun saveDataAsync` ，用于异步保存。默认情况下，它等同于 `saveData`。对于支持协程的框架（如`DataStore`），使用此实现有助于充分利用协程优势（默认给出的`DataStorePreference`就是如此）。

在`mutableDataSavarStateOf` 和 `rememberMutableDataSavarState` 函数调用处可以设置`async`以启用异步保存，默认为`true`。



### Null 支持

v1.1.4 放宽了自定义类型时为 `null` 的情况，可以通过

```kotlin
registerTypeConverters<ExampleBean?>(
    save = { bean -> Json.encodeToString(bean) },
    restore = { str -> Json.decodeFromString(str) }
)
```

来支持 `null` 作默认值

```kotlin
val nullableCustomBeanState: DataSaverMutableState<ExampleBean?> = rememberDataSaverState(key = "nullable_bean", initialValue = null)
```

请注意，出于代码的实现上的考虑，设置 `state.value = null` 或 `dataSaverInterface.saveData(key, null)` 实际**将调用对应 `remove` 方法直接移除对应值**。这意味着，框架的默认实现没有办法正确的保存 “null” 值。当 `state.value = null` 设置完且下次重新打开应用后，**框架会认为此 `key` 对应的本地值不存在，会将 value 设为 initialValue**。  
如果您需要真的存储 “null” 且 `initialValue != null`，请手动处理这部分逻辑。比如，设置一个特殊的值来代表 “null” ，比如 `ExampleBean(-1, "null")`；如果您有更好的方案，欢迎 PR！


### @Preview 支持
项目自 v1.1.6 起支持了 @Preview。具体来说，由于 @Preview 模式下无法正常使用 `CompositionLocalProvider`，因此额外实现了 `DataSaverInMemory`，它使用 `HashMap` 来存储数据，从而不依赖于本地存储以及 `CompositionLocalProvider`。

```kotlin
@Composable
@ReadOnlyComposable
fun getLocalDataSaverInterface() =
    if (LocalInspectionMode.current) DefaultDataSaverInMemory else LocalDataSaver.current
```

@Preview 模式下，您可能需要重新调用一遍 `registerTypeConverter` 以重新注册类型转换器。

## 使用的项目

目前，此库已在下列项目中使用：

- [译站：基于 KMP + CMP 实现的 AI 翻译软件](https://github.com/FunnySaltyFish/Transtation-KMP)
- [Github 中搜索](https://github.com/search?q=mutableDataSaverStateOf&type=code)

如果您正在使用此项目，也欢迎您告知我以补充。

有任何建议或bug报告，欢迎提交issue。PR 就更好啦。

