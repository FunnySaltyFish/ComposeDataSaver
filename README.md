# ComposeDataSaver

| [![Maven Central](https://img.shields.io/maven-central/v/io.github.FunnySaltyFish/data-saver-core)](https://central.sonatype.com/artifact/io.github.FunnySaltyFish/data-saver-core) | [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0) |
|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------| ------------------------------------------------------------ |

| [English Version](README_en.md) |
> 英文的 README 是由[译站](https://github.com/FunnySaltyFish/Transtation-KMP)的长文翻译功能直接一键翻译自中文版本的。这是一个强大的翻译应用程序，利用大型语言模型的力量进行翻译，也由我开发。它还是一个**开源的Compose跨平台应用，并使用这个库来保存数据**。如果你在寻找一个完整的项目，可以去那看看

优雅地在 Compose Multiplatform ( Android / JVM Desktop ) 中完成数据持久化

```kotlin
// booleanExample 初始化值为 false
// 之后会自动读取本地数据
var booleanExample by rememberDataSaverState("KEY_BOOLEAN_EXAMPLE", false)
// 直接赋值即可完成持久化
booleanExample = true
```

- :tada: 简洁：近似原生 Compose 函数的写法
- :tada: 低耦合：抽象接口，不限制底层保存算法实现
- :tada: 强大：支持基本的数据类型和自定义类型

**注：此库是对Compose中使用其他框架（比如 Preference、MMKV、DataStore 等）的封装，不是一个单独的数据保存框架**。您可以参考[此链接](https://juejin.cn/post/7144750071156834312)以了解它的设计思想。

<img src="screenshot.png" alt="Example" style="zoom: 15%;" />

您可以点击 [这里下载demo体验](demo.apk)（Debug 包，相较于 release 包较卡顿）

---

## 引入

在`settings.gradle`引入仓库位置

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

## 配置

项目使用 `DataSaverInterface` 的实现类来保存数据，因此**您需要先提供一个此类对象。**

### Android
#### Perference
项目默认包含了使用 `Preference` 保存数据的实现类 `DataSaverPreferences`，可如下初始化：

```kotlin
// init preferences
val dataSaverPreferences = DataSaverPreferences(applicationContext)
CompositionLocalProvider(LocalDataSaver provides dataSaverPreferences){
	ExampleComposable()
}
```

除此之外， 我们也提供了基于 [MMKV](https://github.com/Tencent/MMKV) 或者 [DataStorePreference](https://developer.android.google.cn/jetpack/androidx/releases/datastore) 的简单实现

#### MMKV

1. 在上述依赖基础上，额外添加

```bash
// if you want to use mmkv
implementation "io.github.FunnySaltyFish:data-saver-mmkv:{tag}"
implementation 'com.tencent:mmkv:1.2.14'
```

2. 如下初始化

```kotlin
// 全局初始化 MMKV，比如在 Application 的 onCreate 中
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

#### DataStorePreference

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


### JVM Desktop
默认包含了基于 `java.util.Properties` 的实现类 `DataSaverProperties`，您可以如下初始化：

```kotlin
// init properties
val dataSaver = DataSaverProperties("$userHome/$projectName/config.properties")
CompositionLocalProvider(LocalDataSaver provides dataSaver){
    ExampleComposable()
}
```

如果您需要加密存储，可以使用 `DataSaverEncryptedProperties` 的实现。它基于 AES 算法加密每一项值，您需要提供一个密钥。

```kotlin
val dataSaver = DataSaverEncryptedProperties("$userHome/$projectName/data_saver_encrypted.properties", "FunnySaltyFish")
CompositionLocalProvider(LocalDataSaver provides dataSaver){
    ExampleComposable()
}
```



几者默认支持的类型如下所示

|   类型    | DataSaverPreference | DataSaverMMKV | DataSaverDataStorePreferences | DataSaverProperties/DataSaverEncryptedProperties |
| :-------: | :-----------------: | :-----------: | :---------------------------: | :----------------------------------------------: |
|    Int    |          Y          |       Y       |               Y               |                        Y                         |
|  Boolean  |          Y          |       Y       |               Y               |                        Y                         |
|  String   |          Y          |       Y       |               Y               |                        Y                         |
|   Long    |          Y          |       Y       |               Y               |                        Y                         |
|   Float   |          Y          |       Y       |               Y               |                        Y                         |
|  Double   |                     |       Y       |               Y               |                        Y                         |
| Parceable |                     |       Y       |                               |                                                  |
| ByteArray |                     |       Y       |                               |                                                  |



## 保存数据 

完成了 CompositionLocalProvider 的赋值后，在其子微件内部可使用 `getLocalDataSaverInterface()` 获取当前 `DataSaverInterface` 实例

对于基本数据类型（如String/Int/Boolean）等：

```kotlin
// booleanExample 初始化值为 false
// 之后会自动读取本地数据
var booleanExample by rememberDataSaverState("KEY_BOOLEAN_EXAMPLE", false)
// 直接赋值即可完成持久化
booleanExample = true
```

通过赋值，数据即可自动转换、存于本地。就这么简单！

而对于其他数据类型，您需要自己注册类型转换器，告诉框架如何将您的数据转换为字符串，以及如何从字符串还原：

```kotlin
@Serializable
data class ExampleBean(var id: Int, val label: String)
// ------------ //

// 在初始化时调用registerTypeConverters方法注册对应转换方法
// 该方法接收两个参数：分别用于 转成可序列化类型以保存 和 反序列化为您的Bean
// 此处使用 Json.encodeToString 和 Json.decodeFromString， 您也可以用 Gson、Fastjson 等
registerTypeConverters<ExampleBean>(
	save = { bean -> Json.encodeToString(bean) },
	restore = { str -> Json.decodeFromString(str) }
)

// 或者，如果你只需要对某个 state 编写转换器，可以直接传入 `typeConverter` 参数
// 此参数如有，则其优先级高于 `registerTypeConverters` 方法注册的全局转换器
var array by rememberDataSaverState(
  "custom_type_converter_example",
  intArrayOf(1, 2, 3, 4, 5),
  // 参数类型为 ITypeConverter，这里的 ClassTypeConverter 是基于 type 类型 accept 的子类
  typeConverter = object : ClassTypeConverter(type = typeOf<IntArray>()) {
    override fun save(data: Any?): String {
      return (data as IntArray).joinToString(",")
    }

    override fun restore(str: String): Any {
      return str.split(",").map { it.toInt() }.toIntArray()
    }
  }
)
```

如果您需要存储可空变量，请使用 `registerTypeConverters<ExampleBean?>`。

> 请注意，出于代码的实现上的考虑，对于可空类型，设置 `state.value = null` 或 `dataSaverInterface.saveData(key, null)` 实际**将调用对应 `remove` 方法直接移除对应值**。这意味着，框架的默认实现没有办法正确的保存 “null” 值。当 `state.value = null` 设置完且下次重新打开应用后，**框架会认为此 `key` 对应的本地值不存在，会将 value 设为 initialValue**。  
> 如果您需要真的存储 “null” 且 `initialValue != null`，请手动处理这部分逻辑。比如，设置一个特殊的值来代表 “null” ，比如 `ExampleBean(-1, "null")`；如果您有更好的方案，欢迎 PR！


自 v1.2.1 起，您除了使用类型信息来注册转换器，也可以自己写上其他判定条件：

```kotlin
inline fun <reified T> registerTypeConverters(
      noinline save: (T) -> String,
      noinline restore: (String) -> T,
      noinline acceptCondition: (T) -> Boolean
)
```

当 `acceptCondition` 为 `true` 时，框架会调用对应 `save` 和 `restore` 方法转换对应数据。

> **注意：**
>
> 1. registerTypeConverters 请在初始化时调用，确保早于使用 `rememberDataSaverState("key", ExampleBean())` 之前
> 2. 多个类型转换器会按照注册顺序反向依次尝试，直到找到合适的转换器。因此，如果您注册了多个相同类型的转换器，框架会使用**最后一个符合条件的**转换器。
> 3. 您可以通过 `DataSaverConverters.typeConverters` 获取到注册的全部转换器列表，初始会有默认的一些，如对 `String` 的支持


## 在 Composable 函数外使用

有些情况下，您可能需要将 `DataSaverState` 置于 `@Composable` 函数外面，比如放在 `ViewModel` 中。v1.1.0 提供了 `mutableDataSavarStateOf` 函数用于此用途，该函数将会自动读取并转换已保存的值，并返回 State。

```Kotlin
object AppConfig {
    val dataSaver = DataSaverMMKV(...)
}

class MyViewModel: ViewModel() {
    var username: String by mutableDataSavarStateOf(AppConfig.dataSaver, "username", "")
}
```


## 使用其他存储框架

如果默认提供的几种实现无法满足您的需求，您也可以自行继承 `DataSaverInterface`，并重写 `saveData` 和 `readData` 方法分别用于保存数据和读取数据。对于一些支持协程的框架（如DataStore），您也可以重写 `saveDataAsync` 以实现异步的保存

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

然后将 LocalDataSaver 提供的对象更改为您自己的类实例

```kotlin
val dataSaverXXX = DataSaverXXX()
CompositionLocalProvider(LocalDataSaver provides dataSaverXXX){
    ExampleComposable()
}
```

后续相同使用即可。


## 感知外部数据变化
自 v1.1.6 起，框架加入了**有限的对外部数据变化感知的支持**，具体来说，就是当您在外部修改了某个 key 对应的值时，框架会自动感知到并更新对应的 `MutableDataSaverState`，从而触发 Composable 的更新。

目前，仅有 `rememberDataSaverState` 支持此功能，您需要设置 `senseExternalDataChange` 参数为 `true`。同时，对应的 `DataSaverInterface` 也需要设置 `senseExternalDataChange` 为 true

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
其中，MMKV 本身不支持感知数据变化，因此它的数据变化是 `DataSaverMMKV` 手动提交的。如果你在使用 MMKV 时需要感知数据变化，那么需要调用 `DataSaverMMKV::saveData` 来做数据保存才可以；Desktop 的基于 Properties 的实现均不支持感知外部数据变化

请注意，当新数据为 null 时，会有以下情况：
- 当使用 `rememberDataSaverState` 时
  - 如果 T 为可空类型，比如 ExampleBean? ，那么正确的设置为 null
  - 如果 T 为非空类型，比如 ExampleBean ，那么 State 的 value 会重新变为 initialValue

## 高级设置

### 控制保存策略

v1.1.0 将原先的 `autoSave` 升级为了 `savePolicy`，以控制是否做、什么时候做数据持久化，该值默认为`IMEDIATELY`

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
 */
object DataSaverConfig {
    var DEBUG = true
}
```

### 异步保存

v1.1.0 对 `DataSaverInterface` 新增了 `suspend fun saveDataAsync` ，用于异步保存。默认情况下，它等同于 `saveData`。对于支持协程的框架（如`DataStore`），使用此实现有助于充分利用协程优势（默认给出的`DataStorePreference`就是如此）。

在`mutableDataSavarStateOf`的函数调用处可以设置`async`以启用异步保存，默认为`true`。


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

- [译站：基于 KMP + CMP 实现的 AI 翻译软件 | 总 Star 380+](https://github.com/FunnySaltyFish/Transtation-KMP)
- [tts-server-android | Star 3k+ ](https://github.com/jing332/tts-server-android)
- [Github 中搜索](https://github.com/search?q=mutableDataSaverStateOf&type=code)

如果您正在使用此项目，也欢迎您告知我以补充。

有任何建议或bug报告，欢迎提交issue。PR 就更好啦。

