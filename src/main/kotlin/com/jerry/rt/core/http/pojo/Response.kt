package com.jerry.rt.core.http.pojo

import com.jerry.rt.core.Context
import com.jerry.rt.core.http.other.SessionManager
import com.jerry.rt.core.http.pojo.s.IResponse
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
    context: Context,
    private val protocolPackage: ProtocolPackage,
    output: OutputStream
): IResponse(context,output) {
    fun getPackage() = protocolPackage

    override fun write(body: ByteArray, contentType: String, length: Int) {
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
        byteResponseWriter.writeFirstLine(protocolPackage.protocol.content, statusCode, RtCode.match(statusCode).message)
        header.entries.forEach {
            byteResponseWriter.writeHeader(it.key, it.value)
        }

        val session = protocolPackage.getSession()
        if (session.isNew()){
            addCookie(Cookie(getContext().getRtConfig().rtSessionConfig.sessionKey,session.getId()))
        }

        cookies.forEach {
            byteResponseWriter.writeHeader("Set-Cookie", it.toCookieString(protocolPackage.getRequestURI()))
        }


        if (length != 0) {
            byteResponseWriter.writeBody(body)
        }
        byteResponseWriter.endWrite()
    }
}