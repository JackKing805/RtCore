package com.jerry.rt.core.http.pojo

import com.jerry.rt.core.RtContext
import com.jerry.rt.extensions.toByteArray

/**
 * @className: Request
 * @description: 请求
 * @author: Jerry
 * @date: 2023/1/6:19:47
 **/
data class Request(
    private val rtContext: RtContext,
    private val protocolPackage: ProtocolPackage,
    private val data: MutableList<ByteArray>
) {
    fun getPackage() = protocolPackage

    fun getOriginBody() = data

    fun getBody() = data.toByteArray()

    fun getContext() = rtContext
}