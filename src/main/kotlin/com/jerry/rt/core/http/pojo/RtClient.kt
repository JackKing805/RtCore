package com.jerry.rt.core.http.pojo

import com.jerry.rt.core.RtContext

class RtClient(
    private val rtContext: RtContext,
    private val clientId:String,
    private val isRtClient: Boolean,
    private val response: Response,
    private val isAlive:()->Boolean,
    private val callClose: ()->Unit
) {
    fun isRt() = isRtClient

    fun getRtContext() = rtContext

    fun getClientId() = clientId

    fun getResponse() = response

    fun isClientAlive() = isAlive()
    
    fun close() {
        callClose()
    }
}
