package com.jerry.rt.core.http.interfaces

import com.jerry.rt.core.http.Client
import com.jerry.rt.core.http.converter.DataConverter
import com.jerry.rt.core.http.pojo.Request
import com.jerry.rt.core.http.pojo.Response
import com.jerry.rt.core.http.pojo.RtClient

/**
 * @className: ClientListener
 * @description: 客户端监听
 * @author: Jerry
 * @date: 2023/1/2:13:19
 **/
interface ClientListener {
    suspend fun onRtHeartbeat(client: RtClient)

    fun onRtClientIn(client: RtClient,request: Request,response: Response)
    suspend fun onRtMessage(request: Request,response: Response)
    fun onRtClientOut(client: RtClient)

    suspend fun onMessage(client: RtClient,request: Request,response: Response)

    fun onException(exception: Exception)
}