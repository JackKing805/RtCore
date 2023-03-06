package com.jerry.rt.extensions

import java.io.BufferedReader
import java.io.DataInputStream
import java.io.InputStream
import java.io.InputStreamReader

/**
 * @className: DataExtensions
 * @description: 数据转化工具
 * @author: Jerry
 * @date: 2023/1/1:15:05
 **/

internal fun DataInputStream.readLength(length:Long):MutableList<ByteArray>{
    val list = mutableListOf<ByteArray>()
    val totalLength = length.toInt()
    var readLength =0
    while (readLength<totalLength){
        var count =-1
        val offset = (totalLength - readLength).coerceAtMost(1024)
        if (offset==0){
            break
        }

        val byteArray = ByteArray(offset)
        try {
            count = read(byteArray,0,offset)
            list.add(byteArray)
        }catch (e:Exception){
            e.printStackTrace()
            break
        }

        if (count!=-1){
            readLength+=count
        }
    }
    return list
}

internal fun InputStream.readLength(length:Long):MutableList<ByteArray>{
    val list = mutableListOf<ByteArray>()
    val totalLength = length.toInt()
    var readLength =0
    while (readLength<totalLength){
        var count =-1
        val offset = (totalLength - readLength).coerceAtMost(1024)
        if (offset==0){
            break
        }

        val byteArray = ByteArray(offset)
        try {
            count = read(byteArray,0,offset)
            list.add(byteArray)
        }catch (e:Exception){
            e.printStackTrace()
            break
        }

        if (count!=-1){
            readLength+=count
        }
    }
    return list
}

internal fun InputStream.skipNotConsumptionByte(length: Long):Long{
    if (length==0L){
        return 0
    }
    return skip(length)
}

internal fun InputStream.asDataInputStream() = DataInputStream(this)
internal fun InputStream.asBufferReader() = BufferedReader(InputStreamReader(this,"GBK"))

fun MutableList<ByteArray>.getIndex(index:Int):Byte{
    var position = -1
    for (i in 0 until size){
        for (j in 0 until get(i).size){
            position++
            if (position==index){
                return get(i)[j]
            }
        }
    }
    throw NullPointerException("error index")
}

fun MutableList<ByteArray>.toByteArray():ByteArray{
    var size = 0
    forEach {
        size+=it.size
    }
    val byteArray = ByteArray(size){
        getIndex(it)
    }
    return byteArray
}