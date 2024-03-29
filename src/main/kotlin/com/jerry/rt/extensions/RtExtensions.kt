package com.jerry.rt.extensions

import com.jerry.rt.core.http.protocol.RtContentType
import com.jerry.rt.core.http.protocol.RtMethod
import com.jerry.rt.core.http.protocol.RtProtocol
import com.jerry.rt.core.http.protocol.RtVersion
import com.jerry.rt.core.http.request.model.SocketData

/**
 * @className: RtExtensions
 * @description: rt协议拓展工具类
 * @author: Jerry
 * @date: 2023/1/1:13:05
 **/

internal fun RtProtocol.connectIsRtConnect() = protocol.method.equals(RtMethod.RT.content,true) && protocol.version.equals(RtVersion.RT_1_0.content,true)

internal fun String.rtContentTypeIsHeartbeat() = this.equals(RtContentType.RT_HEARTBEAT.content,true)

internal fun SocketData.isRtConnect() = getMessageRtProtocol().method.equals(RtMethod.RT.content,true) && getMessageRtProtocol().protocolString == RtVersion.RT_1_0
