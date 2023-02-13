package com.jerry.rt.core.http.pojo

import com.jerry.rt.core.RtContext
import com.jerry.rt.core.http.request.model.MultipartFormData
import com.jerry.rt.core.http.request.model.SocketData
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern

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
        ProtocolPackage.Header(socketData.messageRtProtocol.header)
    )
    private val CHARSET_PATTERN = Pattern.compile("charset\\s*=\\s*([a-z0-9-]*)", Pattern.CASE_INSENSITIVE)

    private var charset:Charset?=null
    private var bodyCache:ByteArray? = null

    init {
       initCharset()
    }

    private fun initCharset(){
        val contentType = protocolPackage.getHeader().getContentType()

        if (contentType.isNotEmpty()){
            val matcher = CHARSET_PATTERN.matcher(contentType)
            if (matcher.find()){
                charset = Charset.forName(matcher.group(1))
            }
        }

        if (charset==null){
            charset = StandardCharsets.UTF_8
        }
    }

    fun getPackage() = protocolPackage

    fun getContext() = rtContext

    fun getCharset():Charset = charset!!

    fun getByteBody():ByteArray?{
        return if (isMultipart()){
            null
        }else{
            if (bodyCache==null){
                bodyCache = ByteArray(protocolPackage.getHeader().getContentLength())
                socketData.readData(bodyCache!!,0,bodyCache!!.size)
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