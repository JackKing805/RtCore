package com.jerry.rt.core.http.pojo

import com.jerry.rt.core.RtContext
import com.jerry.rt.core.http.protocol.RtCode
import com.jerry.rt.core.http.protocol.RtHeader
import com.jerry.rt.core.http.protocol.RtMimeType
import com.jerry.rt.core.http.protocol.RtMimeTypeMatcher
import com.jerry.rt.core.http.response.impl.ByteResponseWriter
import com.jerry.rt.utils.RtUtils
import com.jerry.rt.utils.URLEncodeUtil
import java.io.*
import java.nio.charset.Charset
import java.util.*
import kotlin.jvm.Throws

/**
 * @className: Response
 * @description: 返回
 * @author: Jerry
 * @date: 2023/1/6:19:47
 **/
class Response(
    private val rtContext: RtContext,
    private val output: OutputStream,
    private val protocolPackage: ProtocolPackage
){
    protected val byteResponseWriter = ByteResponseWriter(output)
    protected var charset = Charsets.UTF_8

    protected val header = mutableMapOf<String, String>()
    protected var statusCode = 200

    protected var cookies = mutableListOf<Cookie>()

    fun getPackage() = protocolPackage


    init {
        reset()
    }


    fun getContext() = rtContext

    fun setResponseCharset(charset: Charset) {
        this.charset = charset
    }

    fun getResponseCharset() = charset

    fun setHeader(key: String, value: String) {
        header[key] = value
    }

    fun addCookie(cookie: Cookie){
        cookies.add(cookie)
    }

    fun setHeaders(headers: MutableMap<String, String>) {
        header.putAll(headers.filter {
            it.key != "Content-Type" &&
                    it.key != "Content-Length"
        })
    }

    fun setContentType(contentType: String) {
        val result = if (contentType.startsWith("text")) {
            if (contentType.contains(";")) {
                contentType
            } else {
                contentType + ";" + charset.name()
            }
        } else {
            contentType
        }
        header[RtHeader.CONTENT_TYPE.content] = result
    }

    fun setResponseStatusCode(code: Int) {
        statusCode = code
    }

    fun setContentLength(length: Int) {
        header[RtHeader.CONTENT_LENGTH.content] = length.toString()
    }

    fun sendHeader() {
        write("")
    }

    @Throws(IOException::class)
    fun write(body: ByteArray, contentType: String, length: Int = body.size) {
        if (!header.contains(RtHeader.CONTENT_TYPE.content)) {
            setContentType(contentType)
        }
        if (!header.contains(RtHeader.CONTENT_LENGTH.content)) {
            setContentLength(length)
        }
        byteResponseWriter.writeFirstLine(protocolPackage.protocol.content, statusCode, RtCode.match(statusCode).message)
        header.entries.forEach {
            byteResponseWriter.writeHeader(it.key, it.value)
        }

        cookies.forEach {
            byteResponseWriter.writeHeader(RtHeader.SET_COOKIE.content, it.toCookieString(protocolPackage.getRequestURI()))
        }


        if (length != 0) {
            byteResponseWriter.writeBody(body)
        }
        byteResponseWriter.endWrite()

        reset()
    }

    fun reset(){
        header.clear()
        cookies.clear()
        setHeader(RtHeader.DATE.content, RtUtils.dateToFormat(Date(),"EEE, dd MMM yyyy HH:mm:ss 'GMT'"))
        setHeader("Server",rtContext.getRtConfig().serverVersionDetails)
        setResponseStatusCode(200)
        val session = protocolPackage.getSession()
        if (session.isNew()){
            addCookie(Cookie(getContext().getRtConfig().rtSessionConfig.sessionKey,session.getId()))
        }
    }


    @Throws(IOException::class)
    fun write(body: String, contentType: String, length: Int = body.length) {
        write(body.toByteArray(), contentType, length)
    }

    @Throws(IOException::class)
    fun write(body: String, contentType: String) {
        write(body.toByteArray(), contentType, body.length)
    }

    @Throws(IOException::class)
    fun write(body: String) {
        val contentType =
            header[RtHeader.CONTENT_TYPE.content] ?: throw IllegalStateException("Please provider Content-Type")
        write(body.toByteArray(), contentType, body.length)
    }

    @Throws(IOException::class)
    open fun writeFile(file: File, contentType: String? = null) {
        val fileSize = file.length()
        if (fileSize > 2147483647L) {
            throw IllegalArgumentException("File size is too bigger than 2147483647")
        } else {
            val rcontentType = contentType ?: RtMimeTypeMatcher.matchContentType(file.absolutePath)
            if (!rcontentType.startsWith("text/")) {
                setHeader(
                    RtHeader.CONTENT_DISPOSITION.content,
                    "attachment;filename=${URLEncodeUtil.encode(file.name, charset)}"
                )
            }

            val fileInputStream = FileInputStream(file)
            fileInputStream.use {
                write(it.readBytes(), rcontentType, fileSize.toInt())
            }
        }
    }

    @Throws(IOException::class)
    fun writeFile(path: String, contentType: String? = null) {
        writeFile(File(path), contentType)
    }

    @Throws(IOException::class)
    fun writeInputStream(inputStream: InputStream, contentType: String, length: Int) {
        val byteArray = ByteArray(length)
        inputStream.read(byteArray)
        write(byteArray, contentType, length)
    }

    fun getPrintWriter() = PrintWriter(output)
}