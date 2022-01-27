# ComposeDataSaver

| [![Version](https://jitpack.io/v/FunnySaltyFish/ComposeDataSaver.svg)](https://jitpack.io/#FunnySaltyFish/CMaterialColors) | [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0) |
| ------------------------------------------------------------ | ------------------------------------------------------------ |

An elegant way to do data persistence in Jetpack Compose.

```kotlin
var booleanExample by rememberDataSaverState(KEY_BOOLEAN_EXAMPLE, false)
Switch(checked = booleanExample, onCheckedChange = {
	booleanExample = it
})
```



- :tada: Brevity: a nearly native style of coding
- :tada: Low coupling: using abstract interface that does not restrict the implementation of the underlying saving algorithm
- :tada: Lightweight: no third-party libraries other than Compose are introduced by default
- :tada: Flexibility: both basic data types and custom beans are supported



<img src="https://gitee.com/funnysaltyfish/blog-drawing-bed/raw/master/img/202201251711405.png" alt="Example" style="zoom:30%;" />



---

## Implement

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
        implementation 'com.github.FunnySaltyFish.ComposeDataSaver:data-saver:v1.0.2
'
}
```


## Basic Usage

This library uses classes which implements interface `DataSaverInterface` to save data，thus you need to provide an instance of it.

This library includes a default implementing class using `Preference` to save data, that is `DataSaverPreferences`. You can initialize it like this:

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

So that's it !


## Custom storage framework
We provide the basic implementations of using [MMKV](https://github.com/Tencent/MMKV) or [DataStorePreference](https://developer.android.google.cn/jetpack/androidx/releases/datastore).

### MMKV
1. Add extra implementations as below:
```bash
// if you want to use mmkv
implementation "com.github.FunnySaltyFish.ComposeDataSaver:data-saver-mmkv:v1.0.2"
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
implementation "com.github.FunnySaltyFish.ComposeDataSaver:data-saver-data-store-preferences:v1.0.2"
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

Just need to implement interface`DataSaverInterface` and override`saveData` and `readData` methods.

```kotlin
interface DataSaverInterface{
    fun <T> saveData(key:String, data : T)
    fun <T> readData(key: String, default : T) : T
}
```

Then change the object provided by `LocalDataSaver` to your own class instance

```kotlin
// assume that you want to use DataStore to save data
val dataStore = DataSaverDataStore()
CompositionLocalProvider(LocalDataSaver provides dataStore){
	ExampleComposable()
}
```

Other usages remain unchanged.



## Save Entity Classes

The default `DataSaverPreferences` does not provide custom type saving (an error will be threw  when trying to do so). Although persisting entity classes is not recommended, you can still do so. You can choose the following ways to achieve this goal.

1. Implement your `DataSaverInterface` class（see above）and override required methods
2. Serialize the entity class into other basic types (such as String) and store it

For the second method, you need to add a converter for the corresponding entity class to automatically convert it to String when saving. The method is as follows:

```kotlin
@Serializable
data class ExampleBean(var id:Int, val label:String)
// ------------ //

// call the method [registerTypeConverters] to register the corresponding conversion method during initialization. 
// this method receives two parameters: the entity class Class and the corresponding conversion method (Lambda expression)
registerTypeConverters(ExampleBean::class.java) {
    val bean = it as ExampleBean
    Json.encodeToString(bean)
}
```

To check the full code, see [example](/app/src/main/java/com/funny/composedatasaver/ExampleActivity.kt)



## Other Settings

1. If in some cases you don't want to do persistence so frequently，you can set `rememberDataSaverState`'s parameter `autoSave` to `false`，At this time, the assignment operation of the object will not execute the persistence operation. You should save data manually at the location you need：
`LocalDataSaver.current.saveData()`



If this library do help, **a star** is a great encouragement to me!

