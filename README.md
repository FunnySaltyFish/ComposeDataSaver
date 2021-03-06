# ComposeDataSaver

| [![Version](https://jitpack.io/v/FunnySaltyFish/ComposeDataSaver.svg)](https://jitpack.io/#FunnySaltyFish/CMaterialColors) | [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0) |
| ------------------------------------------------------------ | ------------------------------------------------------------ |

| [English Version](README_en.md) |

优雅地在Jetpack Compose中完成数据持久化

```kotlin
var booleanExample by rememberDataSaverState(KEY_BOOLEAN_EXAMPLE, false)
Switch(checked = booleanExample, onCheckedChange = {
	booleanExample = it
})
```



- [x] 简洁：近似原生的写法
- [x] 低耦合：抽象接口，不限制底层保存算法实现
- [x] 轻巧：默认不引入除Compose外任何第三方库
- [x] 灵活：支持基本的数据类型和自定义类型



<img src="https://gitee.com/funnysaltyfish/blog-drawing-bed/raw/master/img/202201251711405.png" alt="Example" style="zoom:30%;" />



---

## 引入

在`settings.gradle`引入jitpack仓库位置

```bash
dependencyResolutionManagement {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```

在项目`build.gradle`引入

```bash
dependencies {
        implementation 'com.github.FunnySaltyFish.ComposeDataSaver:data-saver:v1.0.2
'
}
```





## 基本使用

项目使用`DataSaverInterface`接口的实现类来保存数据，因此您需要先提供一个此类对象。

项目默认包含了使用`Preference`保存数据的实现类`DataSaverPreferences`，可如下初始化：

```kotlin
// init preferences
val dataSaverPreferences = DataSaverPreferences().apply {
	setContext(context = applicationContext)
}
CompositionLocalProvider(LocalDataSaver provides dataSaverPreferences){
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

就这么简单！



## 自定义存储框架

我们提供了基于 [MMKV](https://github.com/Tencent/MMKV) 或者 [DataStorePreference](https://developer.android.google.cn/jetpack/androidx/releases/datastore) 的简单实现

### MMKV

1. 在上述依赖基础上，额外添加

```bash
// if you want to use mmkv
implementation "com.github.FunnySaltyFish.ComposeDataSaver:data-saver-mmkv:v1.0.2"
implementation 'com.tencent:mmkv:1.2.12'
```

2. 如下初始化

```kotlin
MMKV.initialize(applicationContext)
val dataSaverMMKV = DataSaverMMKV().apply {
    setKV(newKV = MMKV.defaultMMKV())
}

CompositionLocalProvider(LocalDataSaver provides dataSaverMMKV){
    // ...
}
```

---

### DataStorePreference

1. 在上述依赖基础上，额外添加

```bash
// if you want to use DataStore
implementation "com.github.FunnySaltyFish.ComposeDataSaver:data-saver-data-store-preferences:v1.0.2"
def data_store_version = "1.0.0"
implementation "androidx.datastore:datastore:$data_store_version"
implementation "androidx.datastore:datastore-preferences:$data_store_version"
```

2. 如下初始化

```kotlin
val Context.dataStore : DataStore<Preferences> by preferencesDataStore("dataStore")
val dataSaverDataStorePreferences = DataSaverDataStorePreferences().apply {
	setDataStorePreferences(applicationContext.dataStore)
}

CompositionLocalProvider(LocalDataSaver provides dataSaverDataStorePreferences){
    // ...
}
```

---

### 其他存储框架

只需要实现`DataSaverInterface`类，并重写`saveData`和`readData`方法分别用于保存数据和读取数据

```kotlin
interface DataSaverInterface{
    fun <T> saveData(key:String, data : T)
    fun <T> readData(key: String, default : T) : T
}
```

然后将LocalDataSaver提供的对象更改为您自己的类实例

```kotlin
val dataStore = DataSaverDataStore()
CompositionLocalProvider(LocalDataSaver provides dataStore){
	ExampleComposable()
}
```

后续相同使用即可。



## 保存自定义类型

默认的`DataSaverPreferences`并不提供自定义类型的保存（当尝试这样做时会报错）。尽管不建议持久化实体类，但您仍可以这样做。您可以选择以下方式实现这一目标。

1. 重写自己的`DataSaverInterface`实现类（见上）并实现相关的保存方法
2. 将实体类序列化为其他基本类型（如String）再储存

对于第二种方式，您需要为对应实体类添加转换器以实现保存时自动转换为String。方法如下：

```kotlin
@Serializable
data class ExampleBean(var id:Int, val label:String)
// ------------ //

// 在初始化时调用registerTypeConverters方法注册对应转换方法
// 该方法接收两个参数：实体类Class和对应的转换方法（Lambda表达式）
registerTypeConverters(ExampleBean::class.java) {
    val bean = it as ExampleBean
    Json.encodeToString(bean)
}
```

完整例子见[示例项目](/app/src/main/java/com/funny/composedatasaver/ExampleActivity.kt)



## 更多设置

1. 如果在某些情况下你不想频繁持久化保存，可设置`rememberDataSaverState`的`autoSave`参数为`false`，此时对象的赋值操作将不会执行持久化操作，您在需要保存的位置手动保存：`LocalDataSaver.current.saveData()`

