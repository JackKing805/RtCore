package com.jerry.rt.core.http.request

import com.jerry.rt.core.Context
import com.jerry.rt.core.http.Client
import com.jerry.rt.core.http.interfaces.ClientListener
import com.jerry.rt.core.http.pojo.ProtocolPackage
import com.jerry.rt.core.http.pojo.Request
import com.jerry.rt.core.http.pojo.Response
import com.jerry.rt.core.http.protocol.Header
import com.jerry.rt.core.http.protocol.RtContentType
import com.jerry.rt.core.http.protocol.RtProtocol
import com.jerry.rt.extensions.connectIsRtConnect
import com.jerry.rt.extensions.createStandCoroutineScope
import com.jerry.rt.extensions.readLength
import com.jerry.rt.extensions.rtContentTypeIsHeartbeat
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
    private var localMessageListener: MessageListener = object : MessageListener {
        override fun ifRtConnectHeartbeat(rtProtocol: MessageListener.MessageRtProtocol) {
            clientListener?.onRtHeartbeatIn(client)
        }


        override fun onMessage(rtProtocol: MessageListener.MessageRtProtocol, data: MutableList<ByteArray>) {
            val protocolPackage = ProtocolPackage(client.getClientId(),rtProtocol.method,rtProtocol.url,rtProtocol.protocolString,rtProtocol.header)
            val response = Response(context,protocolPackage,socket.getOutputStream())
            if(rtProtocol.isRtConnect()){
                checkHeartbeat()
                if (rtProtocol.getContentType().rtContentTypeIsHeartbeat()) {
                    //心跳包
                    receiverHeartbeatTime = System.currentTimeMillis()
                    response.setContentType(RtContentType.RT_HEARTBEAT.content)
                    response.sendHeader()
                    ifRtConnectHeartbeat(rtProtocol)
                    return
                }
                //普通rt 信道
                dealProtocol(rtProtocol,protocolPackage,data,response)
            }else{
                //其他类型协议
                dealProtocol(rtProtocol,protocolPackage,data,response)
                tryClose()
            }
        }

        override fun ifCustomInputStream(inputStream: InputStream) {
            clientListener?.onInputStreamIn(client,inputStream)
            tryClose()
        }

        private fun dealProtocol(rtProtocol: MessageListener.MessageRtProtocol,protocolPackage: ProtocolPackage,data: MutableList<ByteArray>,response: Response){
            clientListener?.onMessage(client, Request(context,protocolPackage,data), response)
        }

    }

    fun init(s:Socket){
        if (isInit){
            return
        }
        isInit = true
        isAlive = true
        try {
            this.socket = s
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
                        e.printStackTrace()
                        break
                    }
                }
            }
        }catch (e:Exception){
            clientListener?.onException(e)
        }finally {
            tryClose()
        }
    }

    fun listen(clientListener: ClientListener) {
        if (!isAlive && isInit){
            return
        }
        this.clientListener = clientListener
    }

    @Throws(SocketException::class)
    private fun onPre(inputStream: DataInputStream) {
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
                val split = readLine.split(":")
                if (split.size < 2) {
                    return
                }
                val head = Header("","")
                for (i in split.indices) {
                    val content = split[i]
                    if (i == 0) {
                        //method
                        head.key = content
                    } else if (i == 1) {
                        //link
                        head.value = content
                    }
                }
                rtProtocol.header.add(head)
            } else if (readLine.isEmpty()) {
                break
            }
        }

        val protocolMessage = MessageListener.MessageRtProtocol(
            method = rtProtocol.protocol.method,
            url = rtProtocol.protocol.url,
            protocolString = rtProtocol.protocol.version,
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
        fun ifRtConnectHeartbeat(rtProtocol: MessageRtProtocol)

        fun onMessage(rtProtocol: MessageRtProtocol, data:MutableList<ByteArray>)

        fun ifCustomInputStream(inputStream: InputStream)

        data class MessageRtProtocol(
            var method:String,
            var url:String,
            var protocolString:String,
            val header: MutableMap<String,Any>,
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