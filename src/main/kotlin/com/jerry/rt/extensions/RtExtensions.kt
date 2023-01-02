package com.jerry.rt.extensions

import com.jerry.rt.core.http.protocol.RtContentType
import com.jerry.rt.core.http.protocol.RtMethod
import com.jerry.rt.core.http.protocol.RtProtocol
import com.jerry.rt.core.http.protocol.RtVersion

/**
 * @className: RtExtensions
 * @description: rt协议拓展工具类
 * @author: Jerry
 * @date: 2023/1/1:13:05
 **/

internal fun RtProtocol.connectIsRtConnect() = protocol.method == RtMethod.RT.content && protocol.version==RtVersion.RT_1_0.content

internal fun String.rtContentTypeIsHeartbeat() = this == RtContentType.RT_HEARTBEAT.content

