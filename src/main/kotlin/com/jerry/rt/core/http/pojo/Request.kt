package com.jerry.rt.core.http.pojo

import com.jerry.rt.core.RtContext
import com.jerry.rt.core.http.request.interfaces.DataReadListener
import com.jerry.rt.core.http.request.model.SocketData

/**
 * @className: Request
 * @description: 请求
 * @author: Jerry
 * @date: 2023/1/6:19:47
 **/
data class Request(
    private val rtContext: RtContext,
    private val socketData: SocketData
):DataReadListener {
    private val protocolPackage = ProtocolPackage(
        rtContext, socketData.messageRtProtocol.method, socketData.messageRtProtocol.url, socketData.messageRtProtocol.protocolString,
        ProtocolPackage.Header(socketData.messageRtProtocol.header)
    )
    
    fun getPackage() = protocolPackage
    
    

    fun getContext() = rtContext

    override fun readData(byteArray: ByteArray, len: Int) {
        socketData.readData(byteArray,len)
    }

    override fun readData(byteArray: ByteArray, offset: Int, len: Int) {
        socketData.readData(byteArray,offset, len)
    }

    override fun readAllData(): ByteArray {
        return socketData.readAllData()
    }

    override fun skipData() {
        socketData.skipData()
    }
}