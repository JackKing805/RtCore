package com.jerry.rt.core.http.pojo

import com.jerry.rt.core.Context
import com.jerry.rt.core.http.protocol.*
import com.jerry.rt.core.http.response.impl.ByteResponseWriter
import com.jerry.rt.extensions.getElse
import com.jerry.rt.extensions.getMimeType
import com.jerry.rt.extensions.readLength
import com.jerry.rt.jva.StreamUtils
import com.jerry.rt.utils.URLEncodeUtil
import sun.net.util.URLUtil
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintWriter
import java.net.URLEncoder
import java.nio.charset.Charset
import kotlin.jvm.Throws

/**
 * @className: Response
 * @description: 返回
 * @author: Jerry
 * @date: 2023/1/6:19:47
 **/
class Response(
    private val context: Context,
    private val protocolPackage: ProtocolPackage,
    private val output: OutputStream
) {
    private val byteResponseWriter = ByteResponseWriter(output)
    private var charset = Charsets.UTF_8

    private val header = mutableMapOf<String, String>()
    private var isSendResponse = false
    private var statusCode = 200

    fun getPackage() = protocolPackage

    fun getContext() = context

    fun setCharset(charset: Charset) {
        this.charset = charset
    }

    fun getCharset() = charset

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

    fun setStatusCode(code: Int) {
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
        byteResponseWriter.writeFirstLine(protocolPackage.protocol, statusCode, RtCode.match(statusCode).message)
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
    fun writeFile(file: File, contentType: String? = null) {
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