package com.jerry.rt.core.http.pojo.s

import com.jerry.rt.core.Context
import com.jerry.rt.core.http.protocol.RtCode
import com.jerry.rt.core.http.protocol.RtHeader
import com.jerry.rt.core.http.protocol.RtMimeType
import com.jerry.rt.core.http.protocol.RtVersion
import com.jerry.rt.core.http.response.impl.ByteResponseWriter
import com.jerry.rt.utils.URLEncodeUtil
import java.io.*
import java.nio.charset.Charset
import kotlin.jvm.Throws

/**
 * @className: IResponse
 * @description: response 实现
 * @author: Jerry
 * @date: 2023/1/19:12:17
 **/
open class IResponse(
    private val context: Context,
    private val output: OutputStream
) {
    protected val byteResponseWriter = ByteResponseWriter(output)
    protected var charset = Charsets.UTF_8

    protected val header = mutableMapOf<String, String>()
    protected var isSendResponse = false
    protected var statusCode = 200


    fun getContext() = context

    fun setResponseCharset(charset: Charset) {
        this.charset = charset
    }

    fun getResponseCharset() = charset

    fun setHeader(key: String, value: String) {
        header[key] = value
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
    open fun write(body: ByteArray, contentType: String, length: Int = body.size) {
        if (isSendResponse) {
            throw RuntimeException("response is send")
        }
        isSendResponse = true

        if (!header.contains(RtHeader.CONTENT_TYPE.content)) {
            setContentType(contentType)
        }
        if (!header.contains(RtHeader.CONTENT_LENGTH.content)) {
            setContentLength(length)
        }
        byteResponseWriter.writeFirstLine(RtVersion.RT_1_0.content, statusCode, RtCode.match(statusCode).message)
        header.entries.forEach {
            byteResponseWriter.writeHeader(it.key, it.value)
        }
        if (length != 0) {
            byteResponseWriter.writeBody(body)
        }
        byteResponseWriter.endWrite()
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
            val rcontentType = contentType ?: RtMimeType.matchContentType(file.absolutePath).mimeType
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