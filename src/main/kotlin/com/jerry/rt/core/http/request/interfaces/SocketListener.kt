package com.jerry.rt.core.http.request.interfaces

import com.jerry.rt.core.http.request.model.SocketData
import java.net.Socket

/**
 * @className: ScoketListener
 * @author: Jerry
 * @date: 2023/2/12:12:37
 **/
interface SocketListener {
    @Throws(Exception::class)
    suspend fun onSocketIn(socket: Socket,onSocketData:suspend (SocketData)->Unit)

    @Throws(Exception::class)
    suspend fun onSocketOut(socket: Socket)


}