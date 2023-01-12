package com.jerry.rt.core.http.pojo

import java.net.URI

/**
 * @className: ComInPackage
 * @description: 请求包体
 * @author: Jerry
 * @date: 2023/1/7:18:53
 **/
class ProtocolPackage(
    val sessionId:String,
    val method: String,
    val url: String,
    val protocol: String,
    private val header: MutableMap<String, String>,
) {
    private val requestURI = URI.create(url)

    fun getHeaders() = header

    fun addHeader(key: String,value:String){
        header[key] = value
    }

    fun getHeaderValue(key: String, default: String = ""): String {
        return (header[key] ?: default).trim()
    }

    fun getContentType() = getHeaderValue("Content-Type", "text/plain")

    fun getContentLength() = try {
        val values = getHeaderValue("Content-Length")
        if (values.isEmpty()) {
            0L
        }else {
            values.toLong()
        }
    } catch (e: Exception) {
        0L
    }

    fun getDate() = getHeaderValue("Date", "")

    fun getUserAgent() = getHeaderValue("User-Agent", "")

    fun getCookie() = getHeaderValue("Cookie", "")


    fun getRequestURI() = requestURI
}