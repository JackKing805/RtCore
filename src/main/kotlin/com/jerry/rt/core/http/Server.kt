package com.jerry.rt.core.http

import com.jerry.rt.core.Context
import com.jerry.rt.extensions.createStandCoroutineScope
import com.jerry.rt.extensions.logInfo
import com.jerry.rt.interfaces.RtCoreListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.lang.Exception
import java.net.ServerSocket
import java.net.Socket

/**
 * @className: Server
 * @description: socket服务端
 * @author: Jerry
 * @date: 2022/12/31:16:21
 **/
internal class Server(private val context: Context,private val onException: (Exception)->Unit) {
    private val channel = Channel<Socket>(
        Int.MAX_VALUE,
        BufferOverflow.SUSPEND
    )

    private val scope = createStandCoroutineScope{
        onException.invoke(it)
        stop()
    }
    private var active = true
    private var serverJob:Job?=null

    fun run():Flow<Socket>{
        serverJob = scope.launch(Dispatchers.IO) {
            val port = context.getRtConfig().port
            val serverSocket = ServerSocket(port)
            "start server at:$port".logInfo()
            while (active){
                val accept = serverSocket.accept()
                channel.trySend(accept)
            }
        }
        return channel.receiveAsFlow()
    }

    fun stop(){
        active = false
        serverJob?.cancel()
        channel.close()
    }
}