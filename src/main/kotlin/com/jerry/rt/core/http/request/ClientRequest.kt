package com.jerry.rt.core.http.request

import com.jerry.rt.core.RtContext
import com.jerry.rt.core.http.Client
import com.jerry.rt.core.http.interfaces.ClientListener
import com.jerry.rt.core.http.pojo.ProtocolPackage
import com.jerry.rt.core.http.pojo.Request
import com.jerry.rt.core.http.pojo.Response
import com.jerry.rt.core.http.pojo.RtClient
import com.jerry.rt.core.http.protocol.RtContentType
import com.jerry.rt.core.http.request.model.MultipartFormData
import com.jerry.rt.core.http.request.model.SocketData
import com.jerry.rt.core.thread.Looper
import com.jerry.rt.extensions.*
import com.jerry.rt.extensions.createStandCoroutineScope
import com.jerry.rt.extensions.isRtConnect
import com.jerry.rt.extensions.logError
import com.jerry.rt.extensions.rtContentTypeIsHeartbeat
import com.jerry.rt.utils.RtUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.InetAddress
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
    private var rtClient:RtClient?=null//暴露给外面的client
    private var isAlive = AtomicBoolean(false)
    private var isInit = false
    private lateinit var socket: Socket
    private lateinit var inetAddress: InetAddress
    private val scope = createStandCoroutineScope {
        clientListener?.onException(it)
    }
    private var clientListener: ClientListener? = null

    private var isRtIn = false

    private var rtResponse:Response?=null

    private var localMessageListener: MessageListener = object : MessageListener {
        override suspend fun ifRtConnectHeartbeat(rtClient: RtClient,protocolPackage: ProtocolPackage) {
            try {
                clientListener?.onRtHeartbeat(rtClient)
            } catch (e: Exception) {
                clientListener?.onException(e)
            }
        }

        private fun createRtClient(isRt:Boolean,response: Response){
            if (rtClient!=null){
                if (rtClient!!.isRt()!=isRt){
                    rtClient = null
                }else{
                    return
                }
            }
            rtClient = RtClient(
                rtContext,
                client.getClientId(),
                isRt,
                response
            ) {
                isAlive()
            }
        }


        override suspend fun onMessage(socketData: SocketData) {
            val messageRtProtocol = socketData.getMessageRtProtocol()
            val protocolPackage = ProtocolPackage(
                rtContext, messageRtProtocol.method, messageRtProtocol.url, messageRtProtocol.protocolString,
                ProtocolPackage.Header(messageRtProtocol.header.toMutableMap(),inetAddress),
            )
            val request = Request(rtContext, socketData,protocolPackage)
            if (request.getPackage().isRtConnect()) {
                receiverHeartbeatTime = System.currentTimeMillis()
                if (rtResponse==null){//todo modifier
                    rtResponse = Response(rtContext, socketData.getSocketBody().getOutputStream(),request.getPackage())
                    createRtClient(true,rtResponse!!)
                }
                //普通rt 信道
                if (!isRtIn) {
                    isRtIn = true
                    checkHeartbeat()
                    try {
                        clientListener?.onRtClientIn(rtClient!!,request, rtResponse!!)
                    } catch (e: Exception) {
                        clientListener?.onException(e)
                    }
                }
                if (request.getPackage().getHeader().getContentType().rtContentTypeIsHeartbeat()) {
                    //心跳包
                    ifRtConnectHeartbeat(rtClient!!,request.getPackage())
                    return
                }
                try {
                    clientListener?.onRtMessage(request, rtResponse!!)
                } catch (e: Exception) {
                    clientListener?.onException(e)
                }
            } else {
                if (isRtIn) {
                    tryClose()
                    return
                }
                val response = Response(rtContext, socketData.getSocketBody().getOutputStream(),request.getPackage())
                createRtClient(false,response)

                //其他类型协议
                dealProtocol(rtClient!!,request, response)
                tryClose()
            }
        }

        private suspend fun dealProtocol(rtClient: RtClient,request: Request, response: Response) {
            try {
                clientListener?.onMessage(rtClient, request, response)
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
            this@ClientRequest.inetAddress = this@ClientRequest.socket.inetAddress
            this@ClientRequest.socket.soTimeout = timeOutConfig.defaultSoTimeout
            isInit = true
            isAlive.set(true)
            val socketListener = rtContext.getRtConfig().socketListener.newInstance()
            try {
                socketListener.onSocketIn(s){
                    if (it.isRtConnect()){
                        this@ClientRequest.socket.soTimeout = 0
                    }else{
                        this@ClientRequest.socket.soTimeout = timeOutConfig.defaultSoTimeout
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
            val interval = rtContext.getRtConfig().rtTimeOutConfig.heartbeatReceiverIntervalTime.toMillis()
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
                clientListener?.onRtClientOut(rtClient!!)
                rtClient = null
                rtResponse = null
            } catch (e: Exception) {
                clientListener?.onException(e)
            }
        }
        try {
            socket.shutdownInput()
        }catch (e:Exception){
            e.printStackTrace()
        }
        try {
            socket.shutdownOutput()
        }catch (e:Exception){
            e.printStackTrace()
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
        suspend fun ifRtConnectHeartbeat(rtClient: RtClient,protocolPackage: ProtocolPackage)

        suspend fun onMessage(socketData: SocketData)
    }

}
