package com.jerry.rt.core.http.pojo

import com.jerry.rt.core.Context
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
}