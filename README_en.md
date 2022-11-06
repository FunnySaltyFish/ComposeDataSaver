# ComposeDataSaver

| [![Version](https://jitpack.io/v/FunnySaltyFish/ComposeDataSaver.svg)](https://jitpack.io/#FunnySaltyFish/CMaterialColors) | [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0) |
| ------------------------------------------------------------ | ------------------------------------------------------------ |

An elegant way to do data persistence in Jetpack Compose.

```kotlin
// booleanExample will be initialized to false
// and will be automatically read from local storage later
var booleanExample by rememberDataSaverState(KEY_BOOLEAN_EXAMPLE, false)
Switch(checked = booleanExample, onCheckedChange = {
    // Persistence can be easily completed by assignment.
    booleanExample = it
})
```



- :tada: Brevity: a nearly native style of coding
- :tada: Low coupling: using abstract interface that does not restrict the implementation of the underlying persistence framework
- :tada: Lightweight: no third-party libraries other than Compose are included by default. (the size of `sources-jar` is only about **10kb** for `core` and **1kb** per optional implementation)
- :tada: Flexibility: both basic data types and custom beans are supported


<center>
<img src="screenshot.png" alt="Example" style="zoom: 25%;" /></center>

You can download the demo [here](demo.apk).

---

## Implementation

Add jitpack's url in `settings.gradle`

```bash
dependencyResolutionManagement {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```

add implementation in module's `build.gradle`

```bash
dependencies {
    implementation "com.github.FunnySaltyFish.ComposeDataSaver:data-saver:{version}"
}
```


## Basic Usage

This library uses classes which implements interface `DataSaverInterface` to save data，thus you need to provide an instance of it.

This library includes a default implementation class using `Preference` to save data, that is `DataSaverPreferences`. You can initialize it like this:

```kotlin
// init preferences
val dataSaverPreferences = DataSaverPreferences().apply {
    setContext(context = applicationContext)
}
CompositionLocalProvider(LocalDataSaver provides dataSaverPreferences){
    ExampleComposable()
}
```

After that, you can use `LocalDataSaver.current` to access to the instance inside  `ExampleComposable` and its children.

For basic data types like String/Int/Boolean :

```kotlin
// booleanExample is initialized as false
// later it will read local data automatically
var booleanExample by rememberDataSaverState(KEY_BOOLEAN_EXAMPLE, false)
// Persistence can be easily completed by direct assignment.
booleanExample = true
```

After v1.1.0, we also support list type:

```kotlin
var listExample by rememberDataSaverListState(key = "key_list_example", default = listOf(...))
// drop one item
onClick = { listExample = listExample.dropLast(1) }
```

## Use It Outside Composable Function
Some times, you may want to use it outside composable function, like in `ViewModel` or `object`. You can use `mutableDataSavarStateOf` to create a `DataSaverMutableState`. The function will read the saved value and convert it automatically (if that type is supported).

The signature of `mutableDataSavarStateOf` is:

```kotlin
/**
 * This function READ AND CONVERT the saved data and return a remembered [DataSaverMutableState].

 * @param key String
 * @param initialValue T default value if it is initialized the first time
 * @param savePolicy how and when to save data, see [SavePolicy]
 * @param async  whether to save data asynchronously
 * @return DataSaverMutableState<T>
 *
 * @see DataSaverMutableState
 */
@Composable
inline fun <reified T> rememberDataSaverState(
    key: String,
    initialValue: T,
    savePolicy: SavePolicy = SavePolicy.IMMEDIATELY,
    async: Boolean = true
): DataSaverMutableState<T>
```


## Custom storage framework
We provide the basic implementations of using [MMKV](https://github.com/Tencent/MMKV) or [DataStorePreference](https://developer.android.google.cn/jetpack/androidx/releases/datastore).

### MMKV
1. Add extra implementations as below:
```bash
// if you want to use mmkv
implementation "com.github.FunnySaltyFish.ComposeDataSaver:data-saver-mmkv:{version}"
implementation 'com.tencent:mmkv:1.2.12'
```
2. Initialize it as below:

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

1. Add extra implementations as below:
```bash
// if you want to use DataStore
implementation "com.github.FunnySaltyFish.ComposeDataSaver:data-saver-data-store-preferences:{version}"
def data_store_version = "1.0.0"
implementation "androidx.datastore:datastore:$data_store_version"
implementation "androidx.datastore:datastore-preferences:$data_store_version"
```
2. Initialize it as below:

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

### Others

Your class just needs to implement the interface`DataSaverInterface` and override`saveData` and `readData` methods.  
For some frameworks that support `Coroutine`, you can ovveride `saveDataAsync` to save data asynchronously.

```kotlin
interface DataSaverInterface{
    fun <T> saveData(key:String, data : T)
    fun <T> readData(key: String, default : T) : T
    suspend fun <T> saveDataAsync(key:String, data : T) = saveData(key, data)
}
```

Then change the object provided by `LocalDataSaver` to your own class instance

```kotlin
val dataSaverXXX = DataSaverXXX()
CompositionLocalProvider(LocalDataSaver provides dataSaverXXX){
    ExampleComposable()
}
```

Other usages remain unchanged.



## Save Entity Classes

The default `DataSaverPreferences` does not provide custom type saving (an error will be threw  when trying to do so). So you can choose **one** of the following ways to achieve this goal.

1. Convert the custom type to a `String` by using function `registerTypeConverters`. 
2. Implement your `DataSaverInterface` class（see content above）and override required methods

For the first method, you need to add a converter for the corresponding entity class to automatically convert it to String when saving and load it from String. The method is as follows:

```kotlin
@Serializable
data class ExampleBean(var id:Int, val label:String)
// ------------ //

// call the method [registerTypeConverters] to register the corresponding conversion method during initialization (BEFORE CALLING `rememberDataSaverState`). 
// this method receives two lambda functions, which are used to convert the entity class to String and read it from String respectively. 
// we use `Json` here, you can use other frameworks(Gson/Fastjson/...).
registerTypeConverters<ExampleBean>(
	save = { bean -> Json.encodeToString(bean) },
	restore = { str -> Json.decodeFromString(str) }
)
```

By doing this, you can use `rememberDataSaverState` to save and read the entity directly. Even more, you can use `rememberDataSaverListState` to save and read the list of corresponding class's entity without any additional code.


To check the full code, see [example](/app/src/main/java/com/funny/composedatasaver/ExampleActivity.kt)


## Advanced Settings

### SavePolicy
Since v1.1.0, we change the original `autoSave` to `savePolicy`. The default value is `SavePolicy.IMMEDIATELY`, which means that the data will be saved immediately after the value is changed. You can also set it to other values:

```kotlin
/**
 * Controls whether and when to do data persistence. Includes [IMMEDIATELY], [DISPOSED] and [NEVER] by default.
 *
 */
open class SavePolicy {
    /**
     * Default mode, do data persistence every time you assign a new value to the state.
     */
    object IMMEDIATELY : SavePolicy()

    /**
     * do data persistence when the Composable enters `onDispose`. NOTE: USE THIS MODE CAREFULLY, BECAUSE SOMETIME
     * `onDispose` will not be called
     */
    object DISPOSED: SavePolicy()

    /**
     * NEVER do data persistence automatically. Please call `state.saveData()` manually.
     *
     * Example: `onClick = { state.saveData() }`
     */
    object NEVER : SavePolicy()
}
```

### Configs 
We provide some configurations for you to customize some behaviors of the library. 

```kotlin
/**
 * Some config that you can set:
 * 1. DEBUG: whether to output some debug info
 * 2. LIST_SEPARATOR: the separator used to convert a list into string, '#@#' by default (**don't use ',' which will occurs in json itself** )
 */
object DataSaverConfig {
    var DEBUG = true
    var LIST_SEPARATOR = "#@#"
}
```

## Async Saving
Since v1.1.0, we provide the ability to save data asynchronously. You can use `rememberDataSaverState` or `rememberDataSaverListState` to save data asynchronously by setting the `async` parameter to `true`. 

The implementaion using `DataStorePreference` supports this feature well by defualt.

## Projects using this library
The library has been used in the following projects:

- [FunnySaltyFish/FunnyTranslation: 基于Jetpack Compose开发的翻译软件，支持多引擎、插件化~ | Jetpack Compose+MVVM+协程+Room](https://github.com/FunnySaltyFish/FunnyTranslation)
- [cy745/LMusic: 一个简洁且独特的音乐播放器，在其中学习使用了MVVM架构 ](https://github.com/cy745/LMusic)

If you are using this library in your project, please let me know and I will add it to the list.

If you have any questions, please feel free to create an issue. PRs are also welcome.

If this library do help, **a star** will be appreciated. Thanks!

