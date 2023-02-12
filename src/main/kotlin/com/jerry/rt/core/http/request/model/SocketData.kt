package com.jerry.rt.core.http.request.model

import com.jerry.rt.core.http.request.interfaces.DataReadListener
import java.io.InputStream

/**
 * @className: SocketData
 * @author: Jerry
 * @date: 2023/2/12:13:50
 **/
class SocketData(
    val messageRtProtocol: MessageRtProtocol,
    private val inputStream: InputStream
) :DataReadListener{
    private val socketBody = SocketBody(messageRtProtocol.getContentLength(),inputStream)

    fun getSocketBody() = socketBody

    override fun readData(byteArray: ByteArray, len: Int) {
        socketBody.readData(byteArray, len)
    }

    override fun readData(byteArray: ByteArray, offset: Int, len: Int) {
        socketBody.readData(byteArray,offset, len)
    }

    override fun readAllData(): ByteArray {
        return socketBody.readAllData()
    }

    override fun readLine():String? {
        return socketBody.readLine()
    }

    override fun skipData() {
        socketBody.skipData()
    }
}