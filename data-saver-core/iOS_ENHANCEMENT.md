# iOS DataSaver 增强功能说明

## 概述

增强版的 `DataSaverNSUserDefaults` 类现在支持更多数据类型和高效的外部变更监听功能，让 iOS 平台的数据存储更加强大和灵活。

## 🆕 新增功能

### 1. 扩展的数据类型支持

除了原有的基本类型（Long, Int, String, Boolean, Float, Double），现在还支持：

#### 复杂数据类型
- **ByteArray** - 二进制数据，如图片、文件等
- **List<*>** - 列表数据（支持基本类型元素）
- **Map<String, *>** - 字典数据（键必须是字符串，值支持基本类型）

#### 特殊类型
- **URL** - 通过 `saveURL()` 和 `readURL()` 方法
- **NSDate** - 通过 `saveDate()` 和 `readDate()` 方法

### 2. 高效的外部变更监听

使用 **KVO (Key-Value Observing)** 机制来精确监听特定键的变化：

```kotlin
val dataSaver = DataSaverNSUserDefaults(senseExternalDataChange = true)

// 当其他代码修改了 UserDefaults 时，会自动触发通知
dataSaver.externalDataChangedFlow?.collect { (key, value) ->
    println("键 $key 被外部修改，新值: $value")
}
```

**优势：**
- ✅ **精确监听** - 只监听真正使用的键
- ✅ **高效性能** - 避免检查所有键的变更
- ✅ **即时通知** - 立即获得特定键的变化和新值
- ✅ **自动管理** - 自动添加/移除 KVO 观察者

### 3. 实用工具方法

- **getObservedKeys()** - 获取当前监听的所有键
- **clearObservation()** - 清理所有监听
- **contains()** - 检查键是否存在（原有功能）
- **remove()** - 删除指定键（原有功能）

## 📖 使用示例

### 基本数据类型

```kotlin
val dataSaver = DataSaverNSUserDefaults()

// 保存
dataSaver.saveData("username", "张三")
dataSaver.saveData("age", 25)
dataSaver.saveData("isPremium", true)

// 读取
val username: String = dataSaver.readData("username", "默认用户")
val age: Int = dataSaver.readData("age", 0)
val isPremium: Boolean = dataSaver.readData("isPremium", false)
```

### 复杂数据类型

```kotlin
// ByteArray - 适合存储小型二进制数据
val imageData = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())
dataSaver.saveData("profileImage", imageData)
val loadedData: ByteArray = dataSaver.readData("profileImage", byteArrayOf())

// List - 存储列表数据
val favoriteColors = listOf("红色", "蓝色", "绿色")
dataSaver.saveData("colors", favoriteColors)
val colors: List<String> = dataSaver.readData("colors", emptyList())

// Map - 存储键值对
val settings = mapOf(
    "theme" to "dark",
    "language" to "zh-CN"
)
dataSaver.saveData("appSettings", settings)
val loadedSettings: Map<String, String> = dataSaver.readData("appSettings", emptyMap())
```

### 特殊类型

```kotlin
// URL
dataSaver.saveURL("website", "https://www.example.com")
val website = dataSaver.readURL("website", "")

// NSDate
val currentDate = NSDate()
dataSaver.saveDate("lastLogin", currentDate)
val lastLogin = dataSaver.readDate("lastLogin")
```

### 高效的外部变更监听

```kotlin
// 创建支持外部变更监听的实例
val dataSaver = DataSaverNSUserDefaults(senseExternalDataChange = true)

// 设置监听器
lifecycleScope.launch {
    dataSaver.externalDataChangedFlow?.collect { (key, newValue) ->
        println("检测到键 '$key' 的变更，新值: $newValue")
        when (key) {
            "username" -> updateUI()
            "settings" -> reloadSettings()
        }
    }
}

// 监听状态管理
println("当前监听的键: ${dataSaver.getObservedKeys()}")

// 现在，即使其他地方修改了 UserDefaults，也会精确收到通知
// 例如：UserDefaults.standard.set("新用户名", forKey: "username")
// 只有 "username" 这个键的变更会被通知，非常高效！
```

## 🔧 技术实现

### 高效的 KVO 监听机制

使用精确的 KVO 监听，而不是监听所有 UserDefaults 变更：

```kotlin
private class UserDefaultsKVOObserver(
    private val onChanged: (String, Any?) -> Unit
) : NSObject() {
    
    override fun observeValueForKeyPath(
        keyPath: String?,
        ofObject: Any?,
        change: Map<Any?, *>?,
        context: kotlinx.cinterop.COpaquePointer?
    ) {
        keyPath?.let { key ->
            val newValue = change?.get(NSKeyValueChangeNewKey)
            onChanged(key, newValue) // 精确知道哪个键变了！
        }
    }
}

// 只为真正使用的键添加监听
private fun addKVOForKey(key: String) {
    if (senseExternalDataChange && !observedKeys.contains(key)) {
        userDefaults.addObserver(
            observer = kvoObserver,
            forKeyPath = key,  // 只监听这个特定的键
            options = NSKeyValueObservingOptionNew,
            context = null
        )
        observedKeys.add(key)
    }
}
```

### 自动监听管理

```kotlin
// 读取数据时自动添加监听
override fun <T> readData(key: String, default: T): T {
    addKVOForKey(key)  // 自动为这个键添加监听
    // ... 读取逻辑
}

// 删除数据时自动移除监听
override fun remove(key: String) {
    removeKVOForKey(key)  // 自动移除监听
    // ... 删除逻辑
}
```

## ⚠️ 注意事项

1. **性能考虑**：NSUserDefaults 适合存储小量数据，大型数据请使用其他存储方案
2. **类型限制**：复杂对象存储受到 NSUserDefaults 支持的类型限制
3. **监听效率**：新的 KVO 机制只监听真正使用的键，性能优异
4. **内存管理**：记得在不需要时调用 `clearObservation()` 清理监听器

## 🆚 监听机制对比

| 方面 | 旧实现（NSUserDefaultsDidChangeNotification） | 新实现（KVO） |
|------|------|--------|
| **精确度** | ❌ 不知道哪个键变了 | ✅ 精确知道变更的键 |
| **效率** | ❌ 每次检查所有观察的键 | ✅ 只通知变更的键 |
| **性能** | ❌ 浪费资源 | ✅ 高效，按需监听 |
| **新值获取** | ❌ 需要重新读取 | ✅ 直接提供新值 |
| **资源使用** | ❌ 监听所有 UserDefaults 变更 | ✅ 只监听关心的键 |

## 🆚 与原版对比

| 功能 | 原版 | 增强版 |
|------|------|--------|
| 基本类型支持 | ✅ | ✅ |
| ByteArray 支持 | ❌ | ✅ |
| List/Map 支持 | ❌ | ✅ |
| URL/Date 支持 | ❌ | ✅ |
| 外部变更监听 | ❌ | ✅ |
| 监听效率 | - | ✅ 高效 KVO |
| 工具方法 | 基础 | 丰富 |

增强版保持了完全的向后兼容性，现有代码无需修改即可获得新功能的好处。 