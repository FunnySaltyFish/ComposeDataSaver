package com.funny.data_saver.core

import kotlin.test.Test
import kotlin.test.assertEquals

class DataSaverConverterGenericTest {

    data class TestBeanA(val value: String)
    data class TestBeanB(val value: String)
    data class TestBeanC(val value: String)

    @Test
    fun shouldUseDeclaredGenericTypeToResolveConverters() {
        DataSaverConverter.registerTypeConverters<List<TestBeanA>>(
            save = { list -> "A:" + list.joinToString("|") { it.value } },
            restore = { str ->
                str.removePrefix("A:")
                    .takeIf { it.isNotEmpty() }
                    ?.split("|")
                    ?.map(::TestBeanA)
                    ?: emptyList()
            }
        )
        DataSaverConverter.registerTypeConverters<List<TestBeanB>>(
            save = { list -> "B:" + list.joinToString("|") { it.value } },
            restore = { str ->
                str.removePrefix("B:")
                    .takeIf { it.isNotEmpty() }
                    ?.split("|")
                    ?.map(::TestBeanB)
                    ?: emptyList()
            }
        )

        val dataSaver = DataSaverInMemory()
        val expectedA = listOf(TestBeanA("a1"), TestBeanA("a2"))
        val expectedB = listOf(TestBeanB("b1"), TestBeanB("b2"))

        mutableDataSaverStateOf(
            dataSaverInterface = dataSaver,
            key = "list_a",
            initialValue = emptyList<TestBeanA>(),
            async = false
        ).value = expectedA
        mutableDataSaverStateOf(
            dataSaverInterface = dataSaver,
            key = "list_b",
            initialValue = emptyList<TestBeanB>(),
            async = false
        ).value = expectedB

        val restoredA = mutableDataSaverStateOf(
            dataSaverInterface = dataSaver,
            key = "list_a",
            initialValue = emptyList<TestBeanA>(),
            async = false
        ).value
        val restoredB = mutableDataSaverStateOf(
            dataSaverInterface = dataSaver,
            key = "list_b",
            initialValue = emptyList<TestBeanB>(),
            async = false
        ).value

        assertEquals(expectedA, restoredA)
        assertEquals(expectedB, restoredB)
    }

    @Test
    fun shouldPreferNonNullableConverterWhenValueIsNonNull() {
        DataSaverConverter.registerTypeConverters<Int?>(
            save = { value -> "nullable:$value" },
            restore = { value -> value.removePrefix("nullable:").toInt() }
        )
        DataSaverConverter.registerTypeConverters<Int>(
            save = { value -> "non-null:$value" },
            restore = { value -> value.removePrefix("non-null:").toInt() }
        )

        val dataSaver = DataSaverInMemory()
        val state = mutableDataSaverStateOf<Int?>(
            dataSaverInterface = dataSaver,
            key = "nullable_int",
            initialValue = null,
            async = false
        )
        state.value = 42

        assertEquals("non-null:42", dataSaver.readData("nullable_int", ""))

        val restored = mutableDataSaverStateOf<Int?>(
            dataSaverInterface = dataSaver,
            key = "nullable_int",
            initialValue = null,
            async = false
        ).value
        assertEquals(42, restored)
    }

    @Test
    fun shouldAllowMutableCollectionsToUseReadOnlyCollectionConverters() {
        DataSaverConverter.registerTypeConverters<List<TestBeanC>>(
            save = { list -> "LIST:" + list.joinToString("|") { it.value } },
            restore = { str ->
                str.removePrefix("LIST:")
                    .takeIf { it.isNotEmpty() }
                    ?.split("|")
                    ?.mapTo(mutableListOf(), ::TestBeanC)
                    ?: mutableListOf()
            }
        )
        DataSaverConverter.registerTypeConverters<Set<String>>(
            save = { set -> "SET:" + set.sorted().joinToString("|") },
            restore = { str ->
                str.removePrefix("SET:")
                    .takeIf { it.isNotEmpty() }
                    ?.split("|")
                    ?.toMutableSet()
                    ?: mutableSetOf()
            }
        )
        DataSaverConverter.registerTypeConverters<Map<String, Int>>(
            save = { map ->
                "MAP:" + map.entries
                    .sortedBy { it.key }
                    .joinToString("|") { (key, value) -> "$key=$value" }
            },
            restore = { str ->
                str.removePrefix("MAP:")
                    .takeIf { it.isNotEmpty() }
                    ?.split("|")
                    ?.associateTo(mutableMapOf()) { entry ->
                        val (key, value) = entry.split("=")
                        key to value.toInt()
                    }
                    ?: mutableMapOf()
            }
        )

        val dataSaver = DataSaverInMemory()
        val expectedList = mutableListOf(TestBeanC("c1"), TestBeanC("c2"))
        val expectedSet = mutableSetOf("a", "b")
        val expectedMap = mutableMapOf("a" to 1, "b" to 2)

        mutableDataSaverStateOf(
            dataSaverInterface = dataSaver,
            key = "mutable_list",
            initialValue = mutableListOf<TestBeanC>(),
            async = false
        ).value = expectedList
        mutableDataSaverStateOf(
            dataSaverInterface = dataSaver,
            key = "mutable_set",
            initialValue = mutableSetOf<String>(),
            async = false
        ).value = expectedSet
        mutableDataSaverStateOf(
            dataSaverInterface = dataSaver,
            key = "mutable_map",
            initialValue = mutableMapOf<String, Int>(),
            async = false
        ).value = expectedMap

        assertEquals("LIST:c1|c2", dataSaver.readData("mutable_list", ""))
        assertEquals("SET:a|b", dataSaver.readData("mutable_set", ""))
        assertEquals("MAP:a=1|b=2", dataSaver.readData("mutable_map", ""))

        val restoredList = mutableDataSaverStateOf(
            dataSaverInterface = dataSaver,
            key = "mutable_list",
            initialValue = mutableListOf<TestBeanC>(),
            async = false
        ).value
        val restoredSet = mutableDataSaverStateOf(
            dataSaverInterface = dataSaver,
            key = "mutable_set",
            initialValue = mutableSetOf<String>(),
            async = false
        ).value
        val restoredMap = mutableDataSaverStateOf(
            dataSaverInterface = dataSaver,
            key = "mutable_map",
            initialValue = mutableMapOf<String, Int>(),
            async = false
        ).value

        assertEquals(expectedList, restoredList)
        assertEquals(expectedSet, restoredSet)
        assertEquals(expectedMap, restoredMap)
    }
}
