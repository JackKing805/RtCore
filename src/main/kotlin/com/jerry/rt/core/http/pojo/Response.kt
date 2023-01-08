package com.jerry.rt.core.http.pojo

import com.jerry.rt.core.Context
import com.jerry.rt.core.http.protocol.RtCode
import com.jerry.rt.core.http.protocol.RtHeader
import com.jerry.rt.core.http.response.impl.ByteResponseWriter
import com.jerry.rt.extensions.getElse
import com.jerry.rt.extensions.getMimeType
import com.jerry.rt.utils.URLEncodeUtil
import sun.net.util.URLUtil
import java.io.File
import java.io.FileInputStream
import java.io.OutputStream
import java.io.PrintWriter
import java.net.URLEncoder

/**
 * @className: Response
 * @description: 返回
 * @author: Jerry
 * @date: 2023/1/6:19:47
 **/
class Response(private val context: Context,private val protocolPackage: ProtocolPackage, private val output:OutputStream) {
    private val byteResponseWriter = ByteResponseWriter(output)
    private val charset = Charsets.UTF_8

    private val header = mutableMapOf<String,String>()
    private var isSendResponse = false
    private var statusCode = 200

    fun getPackage() = protocolPackage

    fun getContext() = context

    fun setHeader(key:String,value:String){
        header[key] = value
    }

    fun setHeaders(headers:MutableMap<String,String>){
        header.putAll(headers)
    }

    fun setContentType(contentType: String){
        val result=if (contentType.contains(";")){
            contentType
        }else{
            contentType+";"+charset.name()
        }
        header["Content-Type"] = result
    }

    fun setStatusCode(code:Int){
        statusCode = code
    }

    fun setContentLength(length:Int){
        header["Content-Length"] = length.toString()
    }

    fun sendHeader(){
      write("")
    }

    fun write(body:ByteArray,contentType:String,length: Int= body.size){
        if (isSendResponse){
            throw RuntimeException("response is send")
        }
        isSendResponse = true

        if (!header.contains("Content-Type")){
            setContentType(contentType)
        }
        if (!header.contains("Content-Length")){
            setContentLength(length)
        }
        byteResponseWriter.writeFirstLine(protocolPackage.protocol,statusCode, RtCode.match(statusCode).message)
        header.entries.forEach {
            byteResponseWriter.writeHeader(it.key,it.value)
        }
        if (length!=0){
            byteResponseWriter.writeBody(body)
        }
        byteResponseWriter.endWrite()
    }

    fun write(body:String,contentType: String){
        write(body.toByteArray(),contentType,body.length)
    }

    fun write(body:String){
        val contentType = header["Content-Type"] ?: throw IllegalStateException("Please provider Content-Type")
        write(body.toByteArray(),contentType,body.length)
    }

    fun write(file: File,contentType: String?=null){
        val fileSize = file.length()
        if (fileSize > 2147483647L) {
            throw IllegalArgumentException("File size is too bigger than 2147483647")
        } else {
            val rcontentType = contentType?:getElse(file.name.getMimeType(),"application/octet-stream")
            if (!rcontentType.startsWith("text/")) {
                setHeader(
                    RtHeader.CONTENT_DISPOSITION.content,
                    "attachment;filename=${URLEncodeUtil.encode(file.name,charset)}}"
                )
            }

            val fileInputStream = FileInputStream(file)
            fileInputStream.use {
                write(it.readBytes(),rcontentType,it.available())
            }
        }
    }

    fun getPrintWriter() = PrintWriter(output)
}