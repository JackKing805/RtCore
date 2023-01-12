package com.jerry.rt.core.http.interfaces

import com.jerry.rt.core.http.Client
import com.jerry.rt.core.http.pojo.Request
import com.jerry.rt.core.http.pojo.Response
import java.io.InputStream
import java.lang.Exception

/**
 * @className: ClientListener
 * @description: 客户端监听
 * @author: Jerry
 * @date: 2023/1/2:13:19
 **/
interface ClientListener {
    suspend fun onRtHeartbeatIn(client: Client)

    suspend fun onMessage(client: Client,request: Request,response: Response)

    suspend fun onInputStreamIn(client: Client,inputStream: InputStream)

    fun onException(exception: Exception)
}