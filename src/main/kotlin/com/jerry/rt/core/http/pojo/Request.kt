package com.jerry.rt.core.http.pojo

import com.jerry.rt.core.RtContext
import com.jerry.rt.core.http.protocol.RtContentType
import com.jerry.rt.core.http.request.interfaces.DataReadListener
import com.jerry.rt.core.http.request.model.MultipartFile
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
        if (isFileUpload()){
            return
        }
        socketData.readData(byteArray,len)
    }

    override fun readData(byteArray: ByteArray, offset: Int, len: Int) {
        if (isFileUpload()){
            return
        }
        socketData.readData(byteArray,offset, len)
    }

    override fun readAllData(): ByteArray {
        if (isFileUpload()){
            return ByteArray(0)
        }
        return socketData.readAllData()
    }

    override fun readLine():String? {
        if (isFileUpload()){
            return null
        }
        return socketData.readLine()
    }

    override fun skipData() {
        socketData.skipData()
    }

    fun getMultipartFile():MultipartFile?{
        if (isFileUpload()){
            return MultipartFile(protocolPackage,socketData.getSocketBody())
        }
        return null
    }

    private fun isFileUpload():Boolean{
        val contentType = protocolPackage.getHeader().getContentType()
        return contentType.startsWith(RtContentType.MULTIPART.content)
    }
}