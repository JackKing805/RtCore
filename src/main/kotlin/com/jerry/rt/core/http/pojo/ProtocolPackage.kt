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
    private val header: MutableMap<String, Any>,
) {
    private val requestURI = URI.create(url)

    fun getHeaderValue(key: String, default: String = ""): String {
        return ((header[key] as? String) ?: default).trim()
    }

    fun getContentType() = getHeaderValue("Content-Type", "none")

    fun getContentLength() = try {
        val value = getHeaderValue("Content-Length")
        if (value.isEmpty()) {
            0L
        } else {
            value.toLong()
        }
    } catch (e: Exception) {
        0L
    }

    fun getDate() = getHeaderValue("Date", "")

    fun getUserAgent() = getHeaderValue("User-Agent", "")

    fun getCookie() = getHeaderValue("Cookie", "")


    fun getRequestURI() = requestURI
}