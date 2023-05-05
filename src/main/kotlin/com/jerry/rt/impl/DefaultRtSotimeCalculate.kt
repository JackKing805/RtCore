package com.jerry.rt.impl

import com.jerry.rt.core.RtContext
import com.jerry.rt.core.http.request.model.MessageRtProtocol
import com.jerry.rt.interfaces.IRtSotimeCalculate

class DefaultRtSotimeCalculate:IRtSotimeCalculate {
    override fun calculateSoTimeout(rtContext: RtContext,messageRtProtocol: MessageRtProtocol): Int {
        val fileSize = messageRtProtocol.getContentLength()
        val KB = 1024L
        val MB = 1024 * KB
        val GB = 1024 * MB

        return when {
            fileSize < 10 * MB -> 5
            fileSize < 100 * MB -> 10
            fileSize < 1 * GB -> 30
            else-> 60
        } * 1000 // 转换为毫秒
    }
}