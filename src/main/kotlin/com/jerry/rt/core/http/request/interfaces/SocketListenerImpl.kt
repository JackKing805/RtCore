package com.jerry.rt.core.http.request.interfaces

import com.jerry.rt.core.http.protocol.RtVersion
import com.jerry.rt.core.http.request.model.MessageRtProtocol
import com.jerry.rt.core.http.request.model.SocketData
import com.jerry.rt.jva.http.InputStreamHandler
import java.io.InputStream
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @className: SocktListenerImpl
 * @author: Jerry
 * @date: 2023/2/12:12:46
 **/
open class SocketListenerImpl:SocketListener {
    private var isAlive = AtomicBoolean(true)

    internal fun stop(){
        isAlive.set(false)
    }

    protected fun isAlive() = isAlive.get()


    @Throws(Exception::class)
    override suspend fun onSocketIn(
        socket: Socket,
        onSocketData: suspend (SocketData) -> Unit
    ) {
        val inputStream = socket.getInputStream()
        val outputStream = socket.getOutputStream()
        val inputStreamHandler = InputStreamHandler(inputStream,outputStream)
        while (isAlive()){
            try {
                val onPre = onPre(inputStreamHandler)
                val socketData = SocketData(onPre,inputStreamHandler.inputStream(),inputStreamHandler.outputStream())
                onSocketData.invoke(socketData)
            }catch (e:Exception){
                break
            }
        }
    }


    @Throws(Exception::class)
    override suspend fun onSocketOut(socket: Socket) {
    }




    @Throws(Exception::class)
    private fun onPre(inputStream: InputStreamHandler): MessageRtProtocol {
        val headers = inputStream.headers()
        val requestLine = inputStream.requestLine()


        val split = requestLine.split(" ")
        val method = split[0]
        val url = split[1]
        val version = split[2]

        val rtVersion = RtVersion.toRtVersion(version)
        return MessageRtProtocol(
            method = method,
            url = url,
            protocolString = rtVersion,
            header = headers.headers
        )
    }

}