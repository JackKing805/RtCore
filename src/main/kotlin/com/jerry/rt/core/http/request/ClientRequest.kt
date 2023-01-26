package com.jerry.rt.core.http.request

import com.jerry.rt.core.Context
import com.jerry.rt.core.http.Client
import com.jerry.rt.core.http.interfaces.ClientListener
import com.jerry.rt.core.http.other.SessionManager
import com.jerry.rt.core.http.pojo.ProtocolPackage
import com.jerry.rt.core.http.pojo.Request
import com.jerry.rt.core.http.pojo.Response
import com.jerry.rt.core.http.pojo.RtResponse
import com.jerry.rt.core.http.protocol.Header
import com.jerry.rt.core.http.protocol.RtContentType
import com.jerry.rt.core.http.protocol.RtProtocol
import com.jerry.rt.core.http.protocol.RtVersion
import com.jerry.rt.core.thread.Looper
import com.jerry.rt.extensions.connectIsRtConnect
import com.jerry.rt.extensions.createStandCoroutineScope
import com.jerry.rt.extensions.readLength
import com.jerry.rt.extensions.rtContentTypeIsHeartbeat
import com.jerry.rt.utils.RtUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.DataInputStream
import java.io.InputStream
import java.net.Socket
import java.net.SocketException
import kotlin.jvm.Throws

/**
 * @className: ClientRequest
 * @description: 客户端请求处理
 * @author: Jerry
 * @date: 2023/1/2:13:27
 **/
internal class ClientRequest(private val context: Context,private val client: Client) {
    private var isAlive = false
    private var isInit = false
    private lateinit var socket: Socket
    private val scope = createStandCoroutineScope{
        clientListener?.onException(it)
    }
    private var clientListener: ClientListener? = null

    private var rtResponse:RtResponse?=null
    private var isRtIn =false

    private var localMessageListener: MessageListener = object : MessageListener {
        override suspend fun ifRtConnectHeartbeat(rtProtocol: MessageListener.MessageRtProtocol) {
            try{
                clientListener?.onRtHeartbeat(client)
            }catch (e:Exception){
                clientListener?.onException(e)
            }
        }


        override suspend  fun onMessage(rtProtocol: MessageListener.MessageRtProtocol, data: MutableList<ByteArray>) {
            val protocolPackage = ProtocolPackage(context,rtProtocol.method,rtProtocol.url,rtProtocol.protocolString,rtProtocol.header)

            val request = Request(context,protocolPackage,data)
            if(rtProtocol.isRtConnect()){
                checkHeartbeat()
                if (rtResponse==null){
                    rtResponse = RtResponse(context,protocolPackage,socket.getOutputStream())
                }

                if (rtProtocol.getContentType().rtContentTypeIsHeartbeat()) {
                    //心跳包
                    receiverHeartbeatTime = System.currentTimeMillis()
                    rtResponse!!.setContentType(RtContentType.RT_HEARTBEAT.content)
                    rtResponse!!.sendHeader()
                    ifRtConnectHeartbeat(rtProtocol)
                    return
                }
                //普通rt 信道
                if (!isRtIn){
                    isRtIn = true
                    try {
                        clientListener?.onRtClientIn(client,rtResponse!!)
                    }catch (e:Exception){
                        clientListener?.onException(e)
                    }
                }
                try {
                    clientListener?.onRtMessage(request,rtResponse!!)
                }catch (e:Exception){
                    clientListener?.onException(e)
                }
            }else{
                if (isRtIn){
                    tryClose()
                    return
                }
                val response = Response(context,protocolPackage,socket.getOutputStream())


                //其他类型协议
                dealProtocol(request,response)
                tryClose()
            }
        }

        override suspend  fun ifCustomInputStream(inputStream: InputStream) {
            try {
                clientListener?.onInputStreamIn(client,inputStream)
            }catch (e:Exception){
                clientListener?.onException(e)
            }
            tryClose()
        }

        private suspend fun dealProtocol(request: Request,response: Response){
            try {
                clientListener?.onMessage(client, request, response)
            }catch (e:Exception){
                clientListener?.onException(e)
            }
        }

    }

    fun init(s:Socket){
        val looper = Looper()
        looper.prepare()
        scope.launch(Dispatchers.IO) {
            if (isInit){
                return@launch
            }
            isInit = true
            isAlive = true
            try {
                this@ClientRequest.socket = s
                if(context.getRtConfig().customerParse){
                    socket.getInputStream().use {
                        localMessageListener.ifCustomInputStream(it)
                    }
                }else{
                    val dataInputStream = DataInputStream(socket.getInputStream())
                    while (isAlive){
                        try {
                            onPre(dataInputStream)
                        }catch (e:Exception){
                            clientListener?.onException(e)
                            break
                        }
                    }
                }
            }catch (e:Exception){
                clientListener?.onException(e)
            }finally {
                tryClose()
                looper.stop()
            }
        }
        looper.loop()
    }

    fun listen(clientListener: ClientListener) {
        if (!isAlive && isInit){
            return
        }
        this.clientListener = clientListener
    }

    @Throws(SocketException::class,IllegalArgumentException::class)
    private suspend fun onPre(inputStream: DataInputStream) {
        var isProtocolLine = true
        val rtProtocol = RtProtocol()
        while (isAlive) {
            val readLine = inputStream.readLine()
            if (isProtocolLine) {
                readLine ?: return
                val split = readLine.split(" ")
                if (split.size < 3) {
                    return
                }
                for (i in split.indices) {
                    val content = split[i]
                    if (i == 0) {
                        //method
                        rtProtocol.protocol.method = content
                    } else if (i == 1) {
                        //link
                        rtProtocol.protocol.url = content
                    } else if (i == 2) {
                        //version
                        rtProtocol.protocol.version = content
                    }
                }
                isProtocolLine = false
            } else if (readLine.contains(":")) {
                val dotIndex = readLine.indexOf(":")
                val name = readLine.substring(0,dotIndex)
                val value = readLine.substring(dotIndex+1)
                val head = Header(name,value)
                rtProtocol.header.add(head)
            } else if (readLine.isEmpty()) {
                break
            }
        }

        val rtVersion = RtVersion.toRtVersion(rtProtocol.protocol.version)
        val protocolMessage = MessageListener.MessageRtProtocol(
            method = rtProtocol.protocol.method,
            url = rtProtocol.protocol.url,
            protocolString = rtVersion,
            header = rtProtocol.getHeaderMap(),
            rtProtocol
        )
        val byteArray = inputStream.readLength(protocolMessage.getContentLength())
        localMessageListener.onMessage(protocolMessage, byteArray)
    }

    private var isCheckHeartbeatStart = false
    private var receiverHeartbeatTime = -1L
    //定时发送心跳
    private fun checkHeartbeat() {
        if (isCheckHeartbeatStart){
            return
        }
        isCheckHeartbeatStart = true
        scope.launch(Dispatchers.IO) {
            val interval = context.getRtConfig().heartbeatReceiverIntervalTime.toMillis()
            while (isAlive){
                delay(5000)
                if (receiverHeartbeatTime!=-1L){
                    val dis = System.currentTimeMillis() - receiverHeartbeatTime
                    if (dis>interval){
                        break
                    }
                }
            }
            tryClose()
        }
    }

    fun tryClose() {
        if (!isAlive){
            return
        }
        if (isRtIn){
            try {
                clientListener?.onRtClientOut(client,rtResponse!!)
            }catch (e:Exception){
                clientListener?.onException(e)
            }
        }
        try {
            socket.close()
        }catch (e:Exception){
            e.printStackTrace()
        }finally {
            isAlive = false
            clientListener = null
        }
    }


    internal fun isAlive(): Boolean = isAlive

    private interface MessageListener {
        suspend fun ifRtConnectHeartbeat(rtProtocol: MessageRtProtocol)

        suspend fun onMessage(rtProtocol: MessageRtProtocol, data:MutableList<ByteArray>)

        suspend fun ifCustomInputStream(inputStream: InputStream)

        data class MessageRtProtocol(
            var method:String,
            var url:String,
            var protocolString:RtVersion,
            val header: MutableMap<String,String>,
            private val protocol: RtProtocol
        ){
            private fun getValue(key: String,default:String=""): String {
                return ((header[key] as? String) ?: default).trim()
            }

            //获取内容类型:
            fun getContentType() = getValue("Content-Type","none")

            //获取内容长度
            fun getContentLength() = try {
                val value = getValue("Content-Length")
                if (value.isEmpty()){
                    0L
                }else{
                    value.toLong()
                }
            }catch (e:Exception){
                0L
            }

            fun isRtConnect() = protocol.connectIsRtConnect()

            override fun toString(): String {
                return "MessageRtProtocol(method='$method', url='$url', protocolString='$protocolString', header=$header)"
            }
        }
    }
}