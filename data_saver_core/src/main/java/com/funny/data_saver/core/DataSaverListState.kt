package com.funny.data_saver.core

import androidx.compose.runtime.*
import com.funny.data_saver.core.DataSaverConverter.findRestorer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.reflect.KProperty

/**
 * A state which holds a list as value. It implements the [MutableState] interface, so you can use
 * it just like a normal state.
 *
 * When assign a new value to it, it will do data persistence according to [SavePolicy], which is IMMEDIATELY
 * by default. If you want to save data manually, you can call [saveData].
 *
 * You can call `val (value, setValue) = state` to get its `set` function.
 *
 * @param T the class of each element in the list
 * @param dataSaverInterface the interface to read/save data, see [DataSaverInterface]
 * @param key persistence key
 * @param initialValue NOTE: YOU SHOULD READ THE SAVED VALUE AND PASSED IT AS THIS PARAMETER BY YOURSELF(see: [rememberDataSaverListState])
 * @param savePolicy how and when to save data, see [SavePolicy]
 * @param async Boolean whether to save data asynchronously
 */
class DataSaverMutableListState<T>(
    private val dataSaverInterface: DataSaverInterface,
    private val key: String,
    private val initialValue: List<T> = emptyList(),
    private val savePolicy: SavePolicy = SavePolicy.IMMEDIATELY,
    private val async: Boolean = false,
) : MutableState<List<T>> {
    private val list = mutableStateOf(initialValue)
    private var job: Job? = null
    override var value: List<T>
        get() = list.value
        set(value) {
            doSetValue(value)
        }

    @Deprecated(
        "请优先使用带`savePolicy`参数的构造函数(The constructor with parameter `savePolicy` is preferred.)",
    )
    constructor(
        dataSaverInterface: DataSaverInterface,
        key: String,
        value: List<T>,
        autoSave: Boolean = true,
    ) : this(
        dataSaverInterface,
        key,
        value,
        if (autoSave) SavePolicy.IMMEDIATELY else SavePolicy.NEVER
    )

    operator fun setValue(thisObj: Any?, property: KProperty<*>, value: List<T>) {
        doSetValue(value)
    }

    operator fun getValue(thisObj: Any?, property: KProperty<*>): List<T> = list.value

    fun saveData() {
        val value = value
        if (async) {
            job?.cancel()
            job = scope.launch {
                dataSaverInterface.saveData(key, DataSaverConverter.convertListToString(value).also {
                    log("saveConvertedData(async: $async): $key -> $value(as $it)")
                })
            }
        } else {
            dataSaverInterface.saveData(key, DataSaverConverter.convertListToString(value).also {
                log("saveConvertedData(async: $async): $key -> $value(as $it)")
            })
        }
    }

    fun valueChangedSinceInit() = list.value.deepEquals(initialValue.toList())

    /**
     * remove the key and set the value to `replacement`
     * @param replacement List<T> new value of the state, `initialValue` by default
     */
    fun remove(replacement: List<T> = initialValue) {
        dataSaverInterface.remove(key)
        list.value = replacement
        log("remove: key: $key, replace the value to $replacement")
    }

    private fun doSetValue(value: List<T>) {
        val oldValue = this.list
        this.list.value = value
        if (oldValue != value && savePolicy == SavePolicy.IMMEDIATELY)
            saveData()
    }

    companion object {
        const val TAG = "DataSaverState"
        private val scope by lazy(LazyThreadSafetyMode.PUBLICATION) {
            CoroutineScope(Dispatchers.IO)
        }

        private val logger by lazy(LazyThreadSafetyMode.PUBLICATION) {
            DataSaverLogger(DataSaverMutableState.TAG)
        }

        private fun log(msg: String) {
            logger.d(msg)
        }
    }

    override fun component1(): List<T> = value

    override fun component2(): (List<T>) -> Unit = ::doSetValue
}

/**
 * This function provide an elegant way to do data persistence.
 * Check the example in `README.md` to see how to use it.
 *
 * NOTE: Use another function with parameter `savePolicy` instead
 *
 * @param key String
 * @param default T default value if it is initialized the first time
 * @param autoSave Boolean whether to do data persistence each time you do assignment
 * @return DataSaverMutableState<T>
 */
@Composable
inline fun <reified T : Any> rememberDataSaverListState(
    key: String,
    default: List<T>,
    autoSave: Boolean = true
): DataSaverMutableListState<T> = rememberDataSaverListState(
    key = key,
    initialValue = default,
    savePolicy = if (autoSave) SavePolicy.IMMEDIATELY else SavePolicy.NEVER
)

/**
 * This function READ AND CONVERT the saved data and return a remembered [DataSaverMutableListState].
 * Check the example in `README.md` to see how to use it.
 * -------------------------
 * 此函数 **读取并转换** 已保存的数据，返回remember后的State
 *
 * @param key String
 * @param initialValue T default value if it is initialized the first time
 * @param savePolicy how and when to save data, see [SavePolicy]
 * @param async  whether to save data asynchronously
 * @return DataSaverMutableListState<T>
 */
@Composable
inline fun <reified T : Any> rememberDataSaverListState(
    key: String,
    initialValue: List<T>,
    savePolicy: SavePolicy = SavePolicy.IMMEDIATELY,
    async: Boolean = true
): DataSaverMutableListState<T> {
    val saverInterface = getLocalDataSaverInterface()
    var state: DataSaverMutableListState<T>? = null
    DisposableEffect(key, savePolicy) {
        onDispose {
            DataSaverLogger.log("rememberDataSaverListState: state of key=\"$key\" onDisposed!")
            if (savePolicy == SavePolicy.DISPOSED && state != null && state!!.valueChangedSinceInit()) {
                state!!.saveData()
            }
        }
    }
    return remember(saverInterface, key, async) {
        mutableDataSaverListStateOf(
            saverInterface,
            key,
            initialValue,
            savePolicy,
            async
        ).also { state = it }
    }
}

/**
 * This function READ AND CONVERT the saved data and return a [DataSaverMutableListState].
 * Check the example in `README.md` to see how to use it.
 * -------------------------
 * 此函数 **读取并转换** 已保存的数据，返回 [DataSaverMutableListState]
 *
 * @param key String
 * @param initialValue T default value if no data persistence has been done
 * @param savePolicy how and when to save data, see [SavePolicy]
 * @param async  whether to save data asynchronously
 * @return DataSaverMutableListState<T>
 */
inline fun <reified T> mutableDataSaverListStateOf(
    dataSaverInterface: DataSaverInterface,
    key: String,
    initialValue: List<T> = emptyList(),
    savePolicy: SavePolicy = SavePolicy.IMMEDIATELY,
    async: Boolean = false,
): DataSaverMutableListState<T> {
    val data = try {
        if (!dataSaverInterface.contains(key)) initialValue
        else dataSaverInterface.readData(key, initialValue)
    } catch (e: Exception) {
        val restore = findRestorer<T>()
        restore ?: throw e
        DataSaverConverter.convertStringToList<T>(dataSaverInterface.readData(key, "[]"), restore)
    }
    return DataSaverMutableListState(dataSaverInterface, key, data, savePolicy, async)
}

internal fun <T> List<T>.deepEquals(other: List<T>): Boolean {
    if (size != other.size) return false
    for (i in indices) {
        if (this[i] != other[i]) return false
    }
    return true
}