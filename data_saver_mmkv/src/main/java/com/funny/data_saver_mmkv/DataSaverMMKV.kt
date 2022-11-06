package com.funny.data_saver_mmkv

import android.os.Parcelable
import com.funny.data_saver.core.DataSaverInterface
import com.tencent.mmkv.MMKV

class DataSaverMMKV : DataSaverInterface {
    companion object {
        lateinit var kv: MMKV
        fun setKV(newKV: MMKV) {
            kv = newKV
        }
    }


    override fun <T> saveData(key: String, data: T) {
        with(kv) {
            when (data) {
                is Long -> encode(key, data)
                is Int -> encode(key, data)
                is String -> encode(key, data)
                is Boolean -> encode(key, data)
                is Float -> encode(key, data)
                is Double -> encode(key, data)
                is Parcelable -> encode(key, data)
                is ByteArray -> encode(key, data)
                else -> throw IllegalArgumentException("This type of data is not supported!")
            }
        }
    }

    override fun <T> readData(key: String, default: T): T = with(kv) {
        val res: Any = when (default) {
            is Long -> decodeLong(key, default)
            is Int -> decodeInt(key, default)
            is String -> decodeString(key, default)!!
            is Boolean -> decodeBool(key, default)
            is Float -> decodeFloat(key, default)
            is Double -> decodeDouble(key, default)
            is Parcelable -> decodeParcelable(key, (default as Parcelable)::class.java) ?: default
            is ByteArray -> decodeBytes(key, default)!!
            else -> throw IllegalArgumentException("This type of data is not supported!")
        }
        return@with res as T
    }
}