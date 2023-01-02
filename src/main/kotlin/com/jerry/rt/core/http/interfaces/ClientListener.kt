package com.jerry.rt.core.http.interfaces

import com.jerry.rt.core.http.Client
import com.jerry.rt.core.http.pojo.ClientMessage
import java.io.InputStream

/**
 * @className: ClientListener
 * @description: 客户端监听
 * @author: Jerry
 * @date: 2023/1/2:13:19
 **/
interface ClientListener {
    fun onRtHeartbeatIn(client: Client,message: ClientMessage)

    fun onMessage(client: Client,message: ClientMessage, data:MutableList<ByteArray>)

    fun onInputStreamIn(client: Client,inputStream: InputStream)
}