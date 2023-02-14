package com.jerry.rt.core.http.request.model

import com.jerry.rt.core.http.protocol.RtVersion

/**
 * @className: MessageRtProtocol
 * @author: Jerry
 * @date: 2023/2/12:13:12
 **/
data class MessageRtProtocol(
    var method: String,
    var url: String,
    var protocolString: RtVersion,
    val header: MutableMap<String, String>
) {
    private fun getValue(key: String, default: String = ""): String {
        return header.entries.find { it.key.trim().lowercase() == key.lowercase() }?.value?.trim()?:default
    }

    //获取内容类型:
    fun getContentType() = getValue("Content-Type", "none")

    //获取内容长度
    fun getContentLength() = try {
        val value = getValue("Content-Length")
        if (value.isEmpty()) {
            0L
        } else {
            value.toLong()
        }
    } catch (e: Exception) {
        0L
    }

    override fun toString(): String {
        return "MessageRtProtocol(method='$method', url='$url', protocolString='$protocolString', header=$header)"
    }
}