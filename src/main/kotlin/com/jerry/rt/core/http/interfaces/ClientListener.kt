package com.jerry.rt.core.http.interfaces

import com.jerry.rt.core.http.Client
import com.jerry.rt.core.http.pojo.Request
import com.jerry.rt.core.http.pojo.Response
import com.jerry.rt.core.http.pojo.RtResponse
import java.io.InputStream

/**
 * @className: ClientListener
 * @description: 客户端监听
 * @author: Jerry
 * @date: 2023/1/2:13:19
 **/
interface ClientListener {
    suspend fun onRtHeartbeat(client: Client)

    fun onRtClientIn(client: Client,response: RtResponse)
    suspend fun onRtMessage(request: Request,rtResponse: RtResponse)
    fun onRtClientOut(client: Client,rtResponse: RtResponse)

    suspend fun onMessage(client: Client,request: Request,response: Response)

    suspend fun onInputStreamIn(client: Client,inputStream: InputStream)

    fun onException(exception: Exception)
}