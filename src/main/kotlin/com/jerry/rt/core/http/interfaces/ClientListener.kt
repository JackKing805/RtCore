package com.jerry.rt.core.http.interfaces

import com.jerry.rt.core.http.Client
import com.jerry.rt.core.http.pojo.Request
import com.jerry.rt.core.http.pojo.Response
import java.io.InputStream

/**
 * @className: ClientListener
 * @description: 客户端监听
 * @author: Jerry
 * @date: 2023/1/2:13:19
 **/
interface ClientListener {
    suspend fun onRtHeartbeat(client: Client)

    fun onRtClientIn(client: Client,response: Response)
    suspend fun onRtMessage(request: Request,response: Response)
    fun onRtClientOut(client: Client,response: Response)

    suspend fun onMessage(client: Client,request: Request,response: Response)

    suspend fun onInputStreamIn(client: Client,inputStream: InputStream)

    fun onException(exception: Exception)
}