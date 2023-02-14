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
        while (isAlive()){
            try {
                val onPre = onPre(inputStream)
                val socketData = SocketData(onPre,inputStream)
                onSocketData.invoke(socketData)
            }catch (e:Exception){
                break
            }
        }
    }


    @Throws(Exception::class)
    override suspend fun onSocketOut(socket: Socket) {
    }

//    @Throws(Exception::class)
//    private fun onPre(bufferedReader: BufferedReader): MessageRtProtocol? {
//        var isProtocolLine = true
//        val rtProtocol = RtProtocol()
//        while (isAlive()) {
//            val readLine = bufferedReader.readLine()
//            if (isProtocolLine) {
//                readLine ?: return null
//                val split = readLine.split(" ")
//                if (split.size < 3) {
//                    return null
//                }
//                for (i in split.indices) {
//                    val content = split[i]
//                    if (i == 0) {
//                        //method
//                        rtProtocol.protocol.method = content
//                    } else if (i == 1) {
//                        //link
//                        rtProtocol.protocol.url = content
//                    } else if (i == 2) {
//                        //version
//                        rtProtocol.protocol.version = content
//                    }
//                }
//                isProtocolLine = false
//            } else if (readLine.contains(":")) {
//                val dotIndex = readLine.indexOf(":")
//                val name = readLine.substring(0, dotIndex)
//                val value = readLine.substring(dotIndex + 1)
//                val head = Header(name, value)
//                rtProtocol.header.add(head)
//            } else if (readLine.isEmpty()) {
//                break
//            }
//        }
//
//        val rtVersion = RtVersion.toRtVersion(rtProtocol.protocol.version)
//        return MessageRtProtocol(
//            method = rtProtocol.protocol.method,
//            url = rtProtocol.protocol.url,
//            protocolString = rtVersion,
//            header = rtProtocol.getHeaderMap()
//        )
//    }


    @Throws(Exception::class)
    private fun onPre(inputStream: InputStream): MessageRtProtocol {
        val inputStreamHandler = InputStreamHandler(inputStream)
        val headers = inputStreamHandler.headers()
        val requestLine = inputStreamHandler.requestLine()


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