package com.funny.data_saver.core

import android.util.Log
import androidx.compose.runtime.*
import com.funny.data_saver.core.DataSaverConverter.typeRestoreConverters
import com.funny.data_saver.core.DataSaverConverter.typeSaveConverters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.reflect.KProperty

/**
 * A state which holds the value.  It implements the [MutableState] interface, so you can use
 * it just like a normal state. see [mutableDataSaverStateOf]
 *
 * When assign a new value to it, it will do data persistence according to [SavePolicy], which is [SavePolicy.IMMEDIATELY]
 * by default. If you want to save data manually, you can call [saveData].
 *
 * You can call `val (value, setValue) = state` to get its `set` function.
 *
 * @param T the class of data
 * @param dataSaverInterface the interface to read/save data, see [DataSaverInterface]
 * @param key persistence key
 * @param initialValue NOTE: YOU SHOULD READ THE SAVED VALUE AND PASSED IT AS THIS PARAMETER BY YOURSELF(see: [mutableDataSaverStateOf])
 * @param savePolicy how and when to save data, see [SavePolicy]
 * @param async Boolean whether to save data asynchronously
 */
class DataSaverMutableState<T>(
    private val dataSaverInterface: DataSaverInterface,
    private val key: String,
    private val initialValue: T,
    private val savePolicy: SavePolicy = SavePolicy.IMMEDIATELY,
    private val async: Boolean = false
) : MutableState<T> {
    private val state = mutableStateOf(initialValue)

    override var value: T
        get() = state.value
        set(value) {
            doSetValue(value)
        }

    @Deprecated(
        "请优先使用带`savePolicy`参数的构造函数(The constructor with parameter `savePolicy` is preferred.)",
    )
    constructor(
        dataSaverInterface: DataSaverInterface,
        key: String,
        value: T,
        autoSave: Boolean = true,
    ) : this(
        dataSaverInterface,
        key,
        value,
        if (autoSave) SavePolicy.IMMEDIATELY else SavePolicy.NEVER
    )

    operator fun setValue(thisObj: Any?, property: KProperty<*>, value: T) {
        doSetValue(value)
    }


    operator fun getValue(thisObj: Any?, property: KProperty<*>): T = state.value

    /**
     * This function will convert and save current data.
     * If `async` is true, it will `launch` a coroutine
     * to do that.
     */
    fun saveData() {
        if (value == null){
            dataSaverInterface.remove(key)
            return
        }
        val value = value!!
        if (async) {
            scope.launch {
                val typeConverter = typeSaveConverters[value::class.java]
                if (typeConverter != null) {
                    val convertedData = typeConverter(value)
                    log("saveConvertedData(async: $async): $key -> $value(as $convertedData)")
                    dataSaverInterface.saveDataAsync(key, convertedData)
                } else {
                    log("saveData(async: $async): $key -> $value")
                    dataSaverInterface.saveDataAsync(key, value)
                }
            }
        } else {
            val typeConverter = typeSaveConverters[value::class.java]
            if (typeConverter != null) {
                val convertedData = typeConverter(value)
                log("saveConvertedData(async: $async): $key -> $value(as $convertedData)")
                dataSaverInterface.saveData(key, convertedData)
            } else {
                log("saveData(async: $async): $key -> $value")
                dataSaverInterface.saveData(key, value)
            }
        }
    }

    /**
     * remove the key and set the value to `replacement`
     * @param replacement List<T> new value of the state, `initialValue` by default
     */
    fun remove(replacement: T = initialValue) {
        dataSaverInterface.remove(key)
        state.value = replacement
    }

    fun valueChangedSinceInit() = state.value != initialValue


    private fun doSetValue(value: T) {
        val oldValue = this.state.value
        this.state.value = value
        if (oldValue != value && savePolicy == SavePolicy.IMMEDIATELY)
            saveData()
    }

    private fun log(msg: String) {
        if (DataSaverConfig.DEBUG) Log.d(TAG, msg)
    }

    companion object {
        const val TAG = "DataSaverState"

        private val scope by lazy(LazyThreadSafetyMode.PUBLICATION) {
            CoroutineScope(Dispatchers.IO)
        }
    }

    override operator fun component1() = state.value

    override operator fun component2(): (T) -> Unit = {
        doSetValue(it)
    }
}

/**
 * This function provide an elegant way to do data persistence.
 * Check the example in `README.md` to see how to use it.
 *
 * NOTE: THE VERSION WITH PARAMETER `savePolicy` IS PREFERRED.
 *
 * @param key String
 * @param default T default value if it is initialized the first time
 * @param autoSave Boolean whether to do data persistence each time you do assignment
 * @return DataSaverMutableState<T>
 */
@Composable
inline fun <reified T> rememberDataSaverState(
    key: String,
    default: T,
    autoSave: Boolean = true
): DataSaverMutableState<T> = rememberDataSaverState(
    key = key,
    initialValue = default,
    savePolicy = if (autoSave) SavePolicy.IMMEDIATELY else SavePolicy.NEVER
)


/**
 * This function READ AND CONVERT the saved data and return a remembered [DataSaverMutableState].
 * Check the example in `README.md` to see how to use it.
 * ================================
 *
 * 此函数 **读取并转换** 已保存的数据，返回remember后的 [DataSaverMutableState]
 *
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
): DataSaverMutableState<T> {
    val saverInterface = LocalDataSaver.current
    var state: DataSaverMutableState<T>? = null
    DisposableEffect(key, savePolicy) {
        onDispose {
            if (DataSaverConfig.DEBUG) Log.d(
                "rememberDataSaver",
                "rememberDataSaverState: onDisposed!"
            )
            if (savePolicy == SavePolicy.DISPOSED && state != null && state!!.valueChangedSinceInit()) {
                state!!.saveData()
            }
        }
    }
    return remember(saverInterface, key, async) {
        mutableDataSaverStateOf(saverInterface, key, initialValue, savePolicy, async).also {
            state = it
        }
    }
}

/**
 * This function READ AND CONVERT the saved data and return a [DataSaverMutableState].
 * Check the example in `README.md` to see how to use it.
 *
 * 此函数 **读取并转换** 已保存的数据，返回 [DataSaverMutableState]
 *
 * @param key String
 * @param initialValue T default value if it is initialized the first time
 * @param savePolicy how and when to save data, see [SavePolicy]
 * @param async  whether to save data asynchronously
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
): DataSaverMutableState<T> {
    val data = try {
        dataSaverInterface.readData(key, initialValue)
    } catch (e: Exception) {
        val restore = typeRestoreConverters[T::class.java]
        restore ?: throw e
        val jsonData = dataSaverInterface.readData(key, "")
        if (jsonData == "") initialValue
        else restore(jsonData) as T
    }
    return DataSaverMutableState(dataSaverInterface, key, data, savePolicy, async)
}



