package com.jerry.rt.core.http

import com.jerry.rt.core.Context
import com.jerry.rt.core.http.interfaces.ClientListener
import com.jerry.rt.core.http.request.ClientRequest
import java.lang.Exception
import java.net.Socket

/**
 * @className: Client
 * @description: socket 客户端
 * @author: Jerry
 * @date: 2022/12/31:16:30
 **/
//心跳包
//rt / rt/1.0/r/n
//header /r/n
///r/n

class Client(private val context: Context) {
    private var clientId = ""
    private val clientRequest = ClientRequest(context,this)

    internal fun initClient(clientId: String){
        this.clientId = clientId
    }

    internal fun init(socket: Socket) {
        clientRequest.init(socket)
    }

    fun listen(clientListener: ClientListener) {
        this.clientRequest.listen(clientListener)
    }

    fun isAlive(): Boolean = clientRequest.isAlive()

    fun close() {
        clientRequest.tryClose()
    }

    fun getClientId() = clientId
}
