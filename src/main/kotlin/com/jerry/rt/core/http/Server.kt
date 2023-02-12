package com.jerry.rt.core.http

import com.jerry.rt.core.RtContext
import com.jerry.rt.extensions.createStandCoroutineScope
import com.jerry.rt.extensions.logInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.net.ServerSocket
import java.net.Socket
import java.security.KeyStore
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext

/**
 * @className: Server
 * @description: socket服务端
 * @author: Jerry
 * @date: 2022/12/31:16:21
 **/
internal class Server(private val rtContext: RtContext, private val onException: (Exception)->Unit) {
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

    private fun createServerSocket():ServerSocket{
        val rtConfig = rtContext.getRtConfig()
        val rtSSLConfig = rtConfig.rtSSLConfig
        return if (rtSSLConfig!=null){
            val keyStore = KeyStore.getInstance("JKS")
            val keyStoreFile = FileInputStream(rtSSLConfig.keyStoreFile)
            keyStore.load(keyStoreFile, rtSSLConfig.keyStorePassword.toCharArray())

            val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
            keyManagerFactory.init(keyStore, rtSSLConfig.keyPassword.toCharArray())

            val context = SSLContext.getInstance("TLS")
            context.init(keyManagerFactory.keyManagers, null, null)

            val sslSocketFactory = context.serverSocketFactory
            sslSocketFactory.createServerSocket(rtConfig.port) as ServerSocket
        }else{
            ServerSocket(rtConfig.port)
        }
    }

    fun run():Flow<Socket>{
        serverJob = scope.launch(Dispatchers.IO) {
            val serverSocket = createServerSocket()
            "start server at:${serverSocket.localPort}".logInfo()
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