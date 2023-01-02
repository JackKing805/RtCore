package com.jerry.rt.core

import com.jerry.rt.bean.RtConfig
import com.jerry.rt.core.http.Client
import com.jerry.rt.core.http.interfaces.ClientListener
import com.jerry.rt.core.http.pojo.ClientMessage
import com.jerry.rt.core.http.protocol.RtContentType
import com.jerry.rt.core.http.response.impl.ByteResponseWriter
import com.jerry.rt.core.http.response.impl.StringResponseWriter
import com.jerry.rt.core.thread.Looper
import com.jerry.rt.extensions.createStandCoroutineScope
import com.jerry.rt.extensions.logError
import com.jerry.rt.extensions.logInfo
import com.jerry.rt.interfaces.RtCoreListener
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.io.InputStream
import java.lang.Thread.sleep
import java.net.Socket
import java.time.Duration
import kotlin.concurrent.thread

class RtCore private constructor() {
    companion object {
        val instance by lazy { RtCore() }
    }

    private var scope = createStandCoroutineScope()

    private var active = true
    private var mainJob: Job? = null

    internal fun isActive() = mainJob?.isActive ?: false

    fun run(rtConfig: RtConfig, statusListener: RtCoreListener? = null) {
        "RtCore start".logInfo()

        val looper = Looper()
        looper.prepare()


        mainJob = scope.launch {
            val mainLooper = Looper()
            mainLooper.prepare()
            Looper.setMainLooper(mainLooper)

            val rtLife = RtLife(rtConfig, object : RtCoreListener {
                override fun onStatusChange(status: RtCoreListener.Status) {
                    statusListener?.onStatusChange(status)
                }

                override fun onClientIn(client: Client) {
                    statusListener?.onClientIn(client)
                }

                override fun onClientOut(client: Client) {
                    statusListener?.onClientOut(client)
                }
            })
            statusListener?.onStatusChange(RtCoreListener.Status.INIT)
            rtLife.onInit()
            statusListener?.onStatusChange(RtCoreListener.Status.RUNNING)
            val runningJob = launch {
                rtLife.onRunning()
            }
            while (active) {
                delay(500)
            }
            runningJob.cancel()
            statusListener?.onStatusChange(RtCoreListener.Status.STOPPING)
            rtLife.onStop()
            statusListener?.onStatusChange(RtCoreListener.Status.STOPPED)
            rtLife.onDestroy()
            "RtCore end".logInfo()
            looper.stop()
        }
        looper.loop()
    }

    fun stop() {
        active = false
    }

    fun stopAfter(duration: Duration) {
        scope.launch {
            delay(duration.toMillis())
            stop()
        }
    }
}


fun main() {
    RtCore.instance.run(RtConfig(), object : RtCoreListener {
            override fun onStatusChange(status: RtCoreListener.Status) {
            }

            override fun onClientIn(client: Client) {
                "onClientIn:$client".logInfo()
                client.listen(object :ClientListener{
                    override fun onRtHeartbeatIn(client: Client, message: ClientMessage) {
                        "onRtHeartbeatIn:$message".logInfo()
                    }

                    override fun onMessage(client: Client, message: ClientMessage, data: MutableList<ByteArray>) {
                        "onMessage:$message".logInfo()
                        if (message.protocol.url=="/favicon.ico"){
                            val responseWrite = client.getResponseWrite(ByteResponseWriter::class)
                            val file = FileInputStream("C:\\Users\\10720\\Downloads\\8a8vgd.jpg")

                            responseWrite?.writeFirstLine(message.protocol.protocol,200,"success")
                            responseWrite?.writeHeader("Content-Type","image/jpeg")
                            responseWrite?.writeHeader("Content-Length",file.available())
                            responseWrite?.writeBody(file.readAllBytes())
                            responseWrite?.endWrite()
                        }else{
                            val responseWrite = client.getResponseWrite(ByteResponseWriter::class)
                            val file = FileInputStream("C:\\Users\\10720\\Downloads\\2020030823074369.png")

                            responseWrite?.writeFirstLine(message.protocol.protocol,200,"success")
                            responseWrite?.writeHeader("Content-Type","image/png")
                            responseWrite?.writeHeader("Content-Length",file.available())
                            responseWrite?.writeBody(file.readAllBytes())
                            responseWrite?.endWrite()
                        }
                    }

                    override fun onInputStreamIn(client: Client, inputStream: InputStream) {
                        "onInputStreamIn:$client".logInfo()

                    }
                })
            }

            override fun onClientOut(client: Client) {
                "onClientOut:$client".logInfo()
            }
        })


    RtCore.instance.stopAfter(Duration.ofMinutes(5))
}