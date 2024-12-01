package com.funny.data_saver.core

import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

interface ITypeConverter {
    fun save(data: Any?): String
    fun restore(str: String): Any?
    fun accept(data: Any?): Boolean
}

/**
 * Use class information to accept data, use `clazz.isInstance(data)` and extra nullable check to determine whether the data is accepted.
 * @property type The type of the data, use [typeOf] to obtain
 */
abstract class ClassTypeConverter(
    private val type: KType
): ITypeConverter {
    private val clazz by lazy {
        type.classifier as KClass<*>
    }

    override fun accept(data: Any?): Boolean {
        return (data != null && clazz.isInstance(data)) ||
                (type.isMarkedNullable && data == null)
    }

    override fun toString(): String {
        return "ClassTypeConverter(type=$type)"
    }
}

object DataSaverConverter {
    val typeConverters: MutableList<ITypeConverter> by lazy(LazyThreadSafetyMode.PUBLICATION) { mutableListOf() }

    @PublishedApi
    internal val logger by lazy(LazyThreadSafetyMode.PUBLICATION) {
        DataSaverLogger("DataSaverConverter")
    }

    init {
        registerDefaultTypeConverters()
    }

    /**
     * Use this function to convert your entity class into basic data type to store.
     * Check the example of this repository to see how to use it.
     * [Example](https://github.com/FunnySaltyFish/ComposeDataSaver/blob/master/app/src/main/java/com/funny/composedatasaver/ExampleActivity.kt)
     *
     * @param save Function1<T, Any>? save your entity bean into [String]
     * @param restore Function1<Any, T>? restore your entity bean from the saved [String] value
     */
    inline fun <reified T : Any?> registerTypeConverters(
        noinline save: (T) -> String,
        noinline restore: (String) -> T
    ) {
        val converter = object : ClassTypeConverter(type = typeOf<T>()) {
            override fun save(data: Any?): String {
                return save(data as T)
            }

            override fun restore(str: String): Any? {
                return restore(str)
            }
        }
        typeConverters.add(converter)
    }

    /**
     * Register a type converter for some specific types
     * @param converter ITypeConverter
     */
    fun registerTypeConverters(
        vararg converter: ITypeConverter
    ) {
        typeConverters.addAll(converter)
    }

    /**
     * Register a type converter for a specific type.
     * @param save How to save the data into String
     * @param restore How to restore thr data from String
     * @param acceptCondition The condition to accept the data
     */
    inline fun <reified T> registerTypeConverters(
        noinline save: (T) -> String,
        noinline restore: (String) -> T,
        noinline acceptCondition: (T) -> Boolean
    ) {
        val converter = object : ITypeConverter {
            override fun save(data: Any?): String {
                return save(data as T)
            }

            override fun restore(str: String): Any? {
                return restore(str)
            }

            override fun accept(data: Any?): Boolean {
                return acceptCondition(data as T)
            }
        }
        typeConverters.add(converter)
    }

    inline fun <reified T> findRestorer(data: T): ((String) -> Any?)? {
        return findTypeConverter(data)?.let {
            it::restore
        }
    }

    fun <T> findSaver(data: T): ((Any?) -> String)? {
        return typeConverters.findLast { it.accept(data) }?.also {
            logger.d("findSaver for data($data): $it")
        }?.let {
            it::save
        }
    }

    inline fun <reified T> findTypeConverter(data: T): ITypeConverter? {
        return typeConverters.findLast { it.accept(data) }.also {
            logger.d("findTypeConverter for data($data): $it")
        }
    }

    fun unsupportedType(data: Any?, action: String = "save"): Nothing =
        error("Unable to $action data: type of $data (class: ${if (data == null)"null" else data::class.java} is not supported, please call [registerTypeConverters] at first!")


    private fun registerDefaultTypeConverters() {
        // String
        registerTypeConverters<String>(
            save = { it },
            restore = { it }
        )
    }
}

