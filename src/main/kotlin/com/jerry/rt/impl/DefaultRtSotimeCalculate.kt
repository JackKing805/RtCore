package com.jerry.rt.impl

import com.jerry.rt.core.RtContext
import com.jerry.rt.core.http.request.model.MessageRtProtocol
import com.jerry.rt.interfaces.IRtSotimeCalculate

class DefaultRtSotimeCalculate:IRtSotimeCalculate {
    override fun calculateSoTimeout(rtContext: RtContext,messageRtProtocol: MessageRtProtocol, networkSpeed: Int): Int {
        val fileSize = messageRtProtocol.getContentLength()
        val baseTimeout = when {
            fileSize <= 1_000_000L -> 10_000 // 10 seconds
            fileSize <= 10_000_000L -> 30_000 // 30 seconds
            fileSize <= 100_000_000L -> 60_000 // 1 minute
            else -> 300_000 // 5 minutes
        }
        val speedFactor = when {
            networkSpeed >= 10_000_000 -> 1 // fast network
            networkSpeed >= 1_000_000 -> 2 // medium network
            else -> 3 // slow network
        }
        return baseTimeout * speedFactor
    }
}