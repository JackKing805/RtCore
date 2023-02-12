package com.jerry.rt.core.http.request.model

import com.jerry.rt.core.http.request.exceptions.LimitLengthException
import com.jerry.rt.core.http.request.exceptions.NoLengthReadException
import com.jerry.rt.core.http.request.interfaces.DataReadListener
import com.jerry.rt.extensions.asBufferReader
import com.jerry.rt.extensions.readLength
import com.jerry.rt.extensions.skipNotConsumptionByte
import com.jerry.rt.extensions.toByteArray
import java.io.InputStream

/**
 * @className: SocketBody
 * @author: Jerry
 * @date: 2023/2/12:14:52
 **/
class SocketBody(private val maxSize:Long,private val inputStream: InputStream):DataReadListener {
    private var readSize = 0L
    private val bufferReader = inputStream.asBufferReader()

    @Throws(exceptionClasses = [NoLengthReadException::class, LimitLengthException::class])
    override fun readData(byteArray: ByteArray,len: Int){
        readData(byteArray,0,len)
    }

    @Throws(exceptionClasses = [NoLengthReadException::class, LimitLengthException::class])
    override fun readData(byteArray: ByteArray,offset:Int,len:Int){
        val total = offset + len
        if (readSize== maxSize){
            throw NoLengthReadException()
        }

        if (readSize+total>maxSize){
            throw LimitLengthException()
        }
        inputStream.read(byteArray,offset,len)
    }

    @Throws(exceptionClasses = [LimitLengthException::class])
    override fun readAllData():ByteArray{
        if (readSize!=0L){
            throw LimitLengthException()
        }
        val byteArray = inputStream.readLength(maxSize).toByteArray()
        readSize = maxSize
        return byteArray
    }

    override fun readLine():String? {
        if (readSize== maxSize){
            throw NoLengthReadException()
        }

        val readLine = bufferReader.readLine()
        readSize+=readLine?.length?:0
        return readLine
    }

    @Throws(exceptionClasses = [Exception::class])
    override fun skipData(){
        val skip = maxSize - readSize
        inputStream.skipNotConsumptionByte(skip)
    }

    fun getInputStream() = inputStream
}