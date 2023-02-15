package com.jerry.rt.core.http.pojo

import com.jerry.rt.core.RtContext
import com.jerry.rt.core.http.request.exceptions.LimitLengthException
import com.jerry.rt.core.http.request.exceptions.NoLengthReadException
import com.jerry.rt.core.http.request.model.MultipartFormData
import com.jerry.rt.core.http.request.model.SocketData
import java.nio.charset.Charset

/**
 * @className: Request
 * @description: 请求
 * @author: Jerry
 * @date: 2023/1/6:19:47
 **/
data class Request(
    private val rtContext: RtContext,
    private val socketData: SocketData
) {
    private val protocolPackage = ProtocolPackage(
        rtContext, socketData.messageRtProtocol.method, socketData.messageRtProtocol.url, socketData.messageRtProtocol.protocolString,
        ProtocolPackage.Header(socketData.messageRtProtocol.header.toMutableMap())
    )

    private var bodyCache:ByteArray? = null

    fun getPackage() = protocolPackage

    fun getContext() = rtContext

    fun getCharset():Charset = protocolPackage.getCharset()

    fun getByteBody():ByteArray?{
        return if (isMultipart()){
            null
        }else{
            if (bodyCache==null){
                bodyCache = ByteArray(protocolPackage.getHeader().getContentLength())
                try {
                    socketData.readData(bodyCache!!,0,bodyCache!!.size)
                }catch (_: NoLengthReadException){
                }catch (_: LimitLengthException){
                }
            }
            bodyCache!!
        }
    }

    fun getBody() = getBody(getCharset())

    fun getBody(charset: Charset):String?{
        val body = getByteBody()
        if (body!=null){
            return String(body,charset)
        }
        return null
    }


    fun getMultipartFormData():MultipartFormData?{
        if (isMultipart()){
            return MultipartFormData(rtContext,protocolPackage,socketData.getSocketBody(),getCharset())
        }
        return null
    }

    private fun isMultipart():Boolean{
        val contentType = protocolPackage.getHeader().getContentType().lowercase()
        return contentType.startsWith("multipart/")
    }
}