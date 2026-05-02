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
    internal val type: KType
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

    internal fun matchTypeScore(targetType: KType): Int {
        if (type.arguments != targetType.arguments) return NO_MATCH_SCORE
        val typeClassifier = type.classifier as? KClass<*> ?: return NO_MATCH_SCORE
        val targetClassifier = targetType.classifier as? KClass<*> ?: return NO_MATCH_SCORE
        val classifierScore = when {
            typeClassifier == targetClassifier -> EXACT_CLASSIFIER_SCORE
            isReadOnlyCollectionMatch(typeClassifier, targetClassifier) -> COLLECTION_COMPATIBLE_SCORE
            else -> return NO_MATCH_SCORE
        }
        return classifierScore + nullabilityScore()
    }

    internal fun matchValueScore(data: Any?): Int {
        if (!accept(data)) return NO_MATCH_SCORE
        if (data == null) return NULL_VALUE_SCORE + nullabilityScore()

        val runtimeClassifier = data::class
        val typeClassifier = type.classifier as? KClass<*> ?: return NO_MATCH_SCORE
        val classifierScore = when {
            typeClassifier == runtimeClassifier -> EXACT_CLASSIFIER_SCORE
            isReadOnlyCollectionValueMatch(typeClassifier, data) -> COLLECTION_COMPATIBLE_SCORE
            else -> RUNTIME_INSTANCE_SCORE
        }
        return classifierScore + nullabilityScore()
    }

    private fun nullabilityScore(): Int {
        return if (type.isMarkedNullable) NULLABLE_SCORE else NON_NULLABLE_SCORE
    }

    private fun isReadOnlyCollectionMatch(
        typeClassifier: KClass<*>,
        targetClassifier: KClass<*>
    ): Boolean {
        return (typeClassifier == List::class && targetClassifier == MutableList::class) ||
            (typeClassifier == Set::class && targetClassifier == MutableSet::class) ||
            (typeClassifier == Map::class && targetClassifier == MutableMap::class)
    }

    private fun isReadOnlyCollectionValueMatch(
        typeClassifier: KClass<*>,
        data: Any
    ): Boolean {
        return (typeClassifier == List::class && data is MutableList<*>) ||
            (typeClassifier == Set::class && data is MutableSet<*>) ||
            (typeClassifier == Map::class && data is MutableMap<*, *>)
    }

    private companion object {
        const val NO_MATCH_SCORE = Int.MIN_VALUE
        const val EXACT_CLASSIFIER_SCORE = 200
        const val COLLECTION_COMPATIBLE_SCORE = 100
        const val RUNTIME_INSTANCE_SCORE = 50
        const val NULL_VALUE_SCORE = 10
        const val NON_NULLABLE_SCORE = 2
        const val NULLABLE_SCORE = 1
    }
}

object DataSaverConverter {
    val typeConverters: MutableList<ITypeConverter> by lazy(LazyThreadSafetyMode.PUBLICATION) { mutableListOf() }
    private val exactTypeConverterCache: MutableMap<KType, ITypeConverter?> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        mutableMapOf()
    }

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
        clearExactTypeConverterCache()
    }

    /**
     * Register a type converter for some specific types
     * @param converter ITypeConverter
     */
    fun registerTypeConverters(
        vararg converter: ITypeConverter
    ) {
        typeConverters.addAll(converter)
        clearExactTypeConverterCache()
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
        clearExactTypeConverterCache()
    }

    inline fun <reified T> findRestorer(data: T): ((String) -> Any?)? {
        return findTypeConverter(typeOf<T>(), data)?.let {
            it::restore
        }
    }

    fun <T> findSaver(data: T): ((Any?) -> String)? {
        return findTypeConverterByValue(data)?.also {
            logger.d("findSaver for data($data): $it")
        }?.let {
            it::save
        }
    }

    @PublishedApi
    internal fun findSaver(type: KType): ((Any?) -> String)? {
        return findTypeConverterByType(type)?.also {
            logger.d("findSaver for type($type): $it")
        }?.let {
            it::save
        }
    }

    @PublishedApi
    internal fun findSaver(type: KType?, data: Any?): ((Any?) -> String)? {
        return findTypeConverter(type, data)?.also {
            logger.d("findSaver for type($type), data($data): $it")
        }?.let {
            it::save
        }
    }

    inline fun <reified T> findTypeConverter(data: T): ITypeConverter? {
        return findTypeConverter(typeOf<T>(), data).also {
            logger.d("findTypeConverter for data($data): $it")
        }
    }

    @PublishedApi
    internal fun findRestorer(type: KType): ((String) -> Any?)? {
        return findTypeConverterByType(type)?.also {
            logger.d("findRestorer for type($type): $it")
        }?.let {
            it::restore
        }
    }

    @PublishedApi
    internal fun findTypeConverter(type: KType?, data: Any?): ITypeConverter? {
        return type?.let(::findTypeConverterByType) ?: findTypeConverterByValue(data)
    }

    @PublishedApi
    internal fun findTypeConverterByType(type: KType): ITypeConverter? {
        if (exactTypeConverterCache.containsKey(type)) {
            return exactTypeConverterCache[type]
        }
        var bestScore = Int.MIN_VALUE
        var converter: ITypeConverter? = null
        typeConverters.forEach { candidate ->
            val score = (candidate as? ClassTypeConverter)?.matchTypeScore(type) ?: Int.MIN_VALUE
            if (score >= bestScore && score != Int.MIN_VALUE) {
                bestScore = score
                converter = candidate
            }
        }
        exactTypeConverterCache[type] = converter
        return converter
    }

    @PublishedApi
    internal fun findTypeConverterByValue(data: Any?): ITypeConverter? {
        var bestScore = Int.MIN_VALUE
        var converter: ITypeConverter? = null
        typeConverters.forEach { candidate ->
            val score = (candidate as? ClassTypeConverter)?.matchValueScore(data)
                ?: if (candidate.accept(data)) 0 else Int.MIN_VALUE
            if (score >= bestScore && score != Int.MIN_VALUE) {
                bestScore = score
                converter = candidate
            }
        }
        return converter
    }

    @PublishedApi
    internal fun clearExactTypeConverterCache() {
        exactTypeConverterCache.clear()
    }

    fun unsupportedType(data: Any?, action: String = "save"): Nothing =
        error("Unable to $action data: type of $data (class: ${if (data == null) "null" else data::class} is not supported, please call [registerTypeConverters] at first!")


    private fun registerDefaultTypeConverters() {
        // String
        registerTypeConverters<String>(
            save = { it },
            restore = { it }
        )
    }
}

