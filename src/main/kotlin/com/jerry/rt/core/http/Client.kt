package com.jerry.rt.core.http

import com.jerry.rt.core.Context
import com.jerry.rt.core.http.interfaces.ClientListener
import com.jerry.rt.core.http.pojo.ClientMessage
import com.jerry.rt.core.http.pojo.Protocol
import com.jerry.rt.core.http.response.ResponseWriter
import com.jerry.rt.core.http.protocol.Header
import com.jerry.rt.core.http.protocol.RtContentType
import com.jerry.rt.core.http.protocol.RtMethod
import com.jerry.rt.core.http.protocol.RtProtocol
import com.jerry.rt.core.http.request.ClientRequest
import com.jerry.rt.core.http.response.impl.StringResponseWriter
import com.jerry.rt.core.thread.Looper
import com.jerry.rt.extensions.*
import com.jerry.rt.extensions.logInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.io.InputStream
import java.io.OutputStream
import java.lang.Thread.sleep
import java.net.Socket
import java.net.SocketException
import kotlin.concurrent.thread
import kotlin.jvm.Throws
import kotlin.reflect.KClass

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

    fun <T:ResponseWriter<*>> getResponseWrite(clazz:KClass<T>) = clientRequest.getResponseWrite(clazz)

    fun getClientId() = clientId

    override fun toString(): String {
        return "Client(clientId='$clientId')"
    }
}
