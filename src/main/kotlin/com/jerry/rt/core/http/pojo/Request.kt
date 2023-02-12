package com.jerry.rt.core.http.pojo

import com.jerry.rt.core.RtContext
import com.jerry.rt.core.http.protocol.RtContentType
import com.jerry.rt.core.http.request.interfaces.DataReadListener
import com.jerry.rt.core.http.request.model.MultipartFormData
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
        if (isMultipart()){
            return
        }
        socketData.readData(byteArray,len)
    }

    override fun readData(byteArray: ByteArray, offset: Int, len: Int) {
        if (isMultipart()){
            return
        }
        socketData.readData(byteArray,offset, len)
    }

    override fun readAllData(): ByteArray {
        if (isMultipart()){
            return ByteArray(0)
        }
        return socketData.readAllData()
    }

    override fun readLine():String? {
        if (isMultipart()){
            return null
        }
        return socketData.readLine()
    }

    override fun skipData() {
        socketData.skipData()
    }

    fun getMultipartFormData():MultipartFormData?{
        if (isMultipart()){
            return MultipartFormData(rtContext,protocolPackage,socketData.getSocketBody())
        }
        return null
    }

    private fun isMultipart():Boolean{
        val contentType = protocolPackage.getHeader().getContentType().lowercase()
        return contentType.startsWith(RtContentType.MULTIPART.content) || contentType==RtContentType.FORM_URLENCODED.content
    }
}