package com.jerry.rt.core.http.request

import com.jerry.rt.core.RtContext
import com.jerry.rt.core.http.Client
import com.jerry.rt.core.http.interfaces.ClientListener
import com.jerry.rt.core.http.pojo.ProtocolPackage
import com.jerry.rt.core.http.pojo.Request
import com.jerry.rt.core.http.pojo.Response
import com.jerry.rt.core.http.protocol.RtContentType
import com.jerry.rt.core.http.request.model.SocketData
import com.jerry.rt.core.thread.Looper
import com.jerry.rt.extensions.*
import com.jerry.rt.extensions.createStandCoroutineScope
import com.jerry.rt.extensions.isRtConnect
import com.jerry.rt.extensions.logError
import com.jerry.rt.extensions.rtContentTypeIsHeartbeat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

/**
 * @className: ClientRequest
 * @description: 客户端请求处理
 * @author: Jerry
 * @date: 2023/1/2:13:27
 **/
internal class ClientRequest(private val rtContext: RtContext, private val client: Client) {
    private var isAlive = AtomicBoolean(false)
    private var isInit = false
    private lateinit var socket: Socket
    private val scope = createStandCoroutineScope {
        clientListener?.onException(it)
    }
    private var clientListener: ClientListener? = null

    private var isRtIn = false

    private var localMessageListener: MessageListener = object : MessageListener {
        override suspend fun ifRtConnectHeartbeat(protocolPackage: ProtocolPackage) {
            try {
                clientListener?.onRtHeartbeat(client)
            } catch (e: Exception) {
                clientListener?.onException(e)
            }
        }


        override suspend fun onMessage(socketData: SocketData) {
            val request = Request(rtContext, socketData)
            if (request.getPackage().isRtConnect()) {
                receiverHeartbeatTime = System.currentTimeMillis()
                val rtResponse = Response(rtContext, socketData.getSocketBody().getOutputStream(),request.getPackage())
                //普通rt 信道
                if (!isRtIn) {
                    isRtIn = true
                    checkHeartbeat()
                    try {
                        clientListener?.onRtClientIn(client,request, rtResponse)
                    } catch (e: Exception) {
                        clientListener?.onException(e)
                    }
                }
                if (request.getPackage().getHeader().getContentType().rtContentTypeIsHeartbeat()) {
                    //心跳包
                    ifRtConnectHeartbeat(request.getPackage())
                    return
                }
                try {
                    clientListener?.onRtMessage(request, rtResponse)
                } catch (e: Exception) {
                    clientListener?.onException(e)
                }
            } else {
                if (isRtIn) {
                    tryClose()
                    return
                }
                val response = Response(rtContext, socketData.getSocketBody().getOutputStream(),request.getPackage())


                //其他类型协议
                dealProtocol(request, response)
                tryClose()
            }
        }

        private suspend fun dealProtocol(request: Request, response: Response) {
            try {
                clientListener?.onMessage(client, request, response)
            } catch (e: Exception) {
                clientListener?.onException(e)
            }
        }

    }

    fun init(s: Socket) {
        val looper = Looper()
        looper.prepare()
        scope.launch(Dispatchers.IO) {
            if (isInit) {
                return@launch
            }
            val timeOutConfig = rtContext.getRtConfig().rtTimeOutConfig
            this@ClientRequest.socket = s
            this@ClientRequest.socket.soTimeout = timeOutConfig.soTimeout
            isInit = true
            isAlive.set(true)
            val socketListener = rtContext.getRtConfig().socketListener.newInstance()
            try {
                socketListener.onSocketIn(s){
                    if (it.isRtConnect()){
                        this@ClientRequest.socket.soTimeout = 0
                    }else{
                        this@ClientRequest.socket.soTimeout = timeOutConfig.soTimeout
                    }
                    localMessageListener.onMessage(it)
                }
            }catch (e:Exception){
                clientListener?.onException(e)
            }finally {
                tryClose()
            }

            while (isAlive.get()){
                delay(600)
            }

            try {
                socketListener.onSocketOut(s)
            }catch (e:Exception){
                clientListener?.onException(e)
            }
            socketListener.stop()
            tryClose()
            looper.stop()
        }
        looper.loop()
    }

    fun listen(clientListener: ClientListener) {
        if (!isAlive.get() && isInit) {
            return
        }
        this.clientListener = clientListener
    }


    private var isCheckHeartbeatStart = false
    private var receiverHeartbeatTime = -1L

    //定时发送心跳
    private fun checkHeartbeat() {
        if (isCheckHeartbeatStart) {
            return
        }
        isCheckHeartbeatStart = true
        scope.launch(Dispatchers.IO) {
            val interval = rtContext.getRtConfig().heartbeatReceiverIntervalTime.toMillis()
            while (isAlive.get()) {
                delay(5000)
                val get = receiverHeartbeatTime
                if (get != -1L) {
                    val curr = System.currentTimeMillis()
                    val dis = curr - get
                    if (dis > interval) {
                        break
                    }
                }else{
                    break
                }
            }
            isCheckHeartbeatStart = false
            tryClose()
        }
    }

    fun tryClose() {
        if (!isAlive.get()) {
            return
        }
        if (isRtIn) {
            try {
                clientListener?.onRtClientOut(client)
            } catch (e: Exception) {
                clientListener?.onException(e)
            }
        }
        try {
            socket.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isAlive.set(false)
            clientListener = null
        }
    }


    internal fun isAlive(): Boolean = isAlive.get()

    private interface MessageListener {
        suspend fun ifRtConnectHeartbeat(protocolPackage: ProtocolPackage)

        suspend fun onMessage(socketData: SocketData)
    }

}
