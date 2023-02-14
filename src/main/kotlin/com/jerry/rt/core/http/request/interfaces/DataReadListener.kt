package com.jerry.rt.core.http.request.interfaces

/**
 * @className: DataReadListener
 * @author: Jerry
 * @date: 2023/2/12:14:01
 **/
interface DataReadListener {
    @Throws(exceptionClasses = [Exception::class])
    fun readData(byteArray: ByteArray,len: Int)

    @Throws(exceptionClasses = [Exception::class])
    fun readData(byteArray: ByteArray,offset:Int,len:Int)

    @Throws(exceptionClasses = [Exception::class])
    fun readAllData():ByteArray

    @Throws(exceptionClasses = [Exception::class])
    fun skipData()
}