package com.jerry.rt.interfaces

import com.jerry.rt.core.RtContext
import com.jerry.rt.core.http.request.model.MessageRtProtocol

//动态计算soTimeOut //todo add
interface IRtSotimeCalculate {
    //返回值为毫秒
    fun calculateSoTimeout(rtContext: RtContext,messageRtProtocol: MessageRtProtocol, networkSpeed: Int = 0): Int
}