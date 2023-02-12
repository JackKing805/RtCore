package com.jerry.rt.core.http.request.interfaces

import com.jerry.rt.core.http.request.exceptions.LimitLengthException
import com.jerry.rt.core.http.request.exceptions.NoLengthReadException
import com.jerry.rt.extensions.readLength
import com.jerry.rt.extensions.skipNotConsumptionByte
import com.jerry.rt.extensions.toByteArray

/**
 * @className: DataReadListener
 * @author: Jerry
 * @date: 2023/2/12:14:01
 **/
interface DataReadListener {
    @Throws(exceptionClasses = [NoLengthReadException::class, LimitLengthException::class])
    fun readData(byteArray: ByteArray,len: Int)

    @Throws(exceptionClasses = [NoLengthReadException::class, LimitLengthException::class])
    fun readData(byteArray: ByteArray,offset:Int,len:Int)

    @Throws(exceptionClasses = [LimitLengthException::class])
    fun readAllData():ByteArray

    @Throws(exceptionClasses = [Exception::class])
    fun skipData()
}