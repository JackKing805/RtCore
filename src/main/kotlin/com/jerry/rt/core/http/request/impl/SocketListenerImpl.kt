package com.jerry.rt.core.http.request.impl

import com.jerry.rt.core.http.request.input.BasicInfoHandler
import com.jerry.rt.core.http.request.interfaces.SocketListener
import com.jerry.rt.core.http.request.model.SocketData
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @className: SocktListenerImpl
 * @author: Jerry
 * @date: 2023/2/12:12:46
 **/
open class SocketListenerImpl: SocketListener {
    private var isAlive = AtomicBoolean(true)

    internal fun stop(){
        isAlive.set(false)
    }

    @SuppressWarnings
    protected fun isAlive() = isAlive.get()


    @Throws(Exception::class)
    override suspend fun onSocketIn(
        socket: Socket,
        onSocketData: suspend (SocketData) -> Unit
    ) {
        val basicInfo = BasicInfoHandler(socket)
        while (isAlive()){
            try {
                val messageRtProtocol = basicInfo.getMessageRtProtocol()
                val socketData = SocketData(messageRtProtocol,basicInfo.inputStream(),basicInfo.outputStream())
                onSocketData.invoke(socketData)
                socketData.skipData()
            }catch (e:Exception){
                e.printStackTrace()
                break
            }
        }
    }


    @Throws(Exception::class)
    override suspend fun onSocketOut(socket: Socket) {
    }

}
