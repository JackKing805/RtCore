package com.jerry.rt.core

import com.jerry.rt.bean.RtConfig
import com.jerry.rt.core.http.Client
import com.jerry.rt.core.http.interfaces.ClientListener
import com.jerry.rt.core.http.pojo.Cookie
import com.jerry.rt.core.http.pojo.Request
import com.jerry.rt.core.http.pojo.Response
import com.jerry.rt.core.http.protocol.RtContentType
import com.jerry.rt.core.thread.Looper
import com.jerry.rt.extensions.createExceptionCoroutineScope
import com.jerry.rt.extensions.logInfo
import com.jerry.rt.interfaces.RtCoreListener
import com.jerry.rt.utils.PlatformUtils
import com.jerry.rt.utils.RtUtils
import kotlinx.coroutines.*
import java.io.File
import java.io.InputStream
import java.lang.Thread.sleep
import java.time.Duration
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

class RtCore private constructor() {
    companion object {
        val instance by lazy { RtCore() }
    }


    private var scope = createExceptionCoroutineScope{
        onException?.invoke(it)
    }
    private var onException:((Exception)->Unit)?=null

    private var active = AtomicBoolean(false)
    private var isStopping = AtomicBoolean(false)
    private var mainJob: Job? = null



    fun run(rtConfig: RtConfig, statusListener: RtCoreListener? = null) {
        if (active.get()){
            return
        }
        active.set(true)

        "RtCore start".logInfo()

        if (PlatformUtils.isAndroid()){
            runAndroid(rtConfig,statusListener)
        }else{
            runJava(rtConfig,statusListener)
        }
    }

    private fun runAndroid(rtConfig: RtConfig,statusListener: RtCoreListener?){
        onException = null
        mainJob?.cancel()
        mainJob = null
        mainJob = scope.launch {
            runCore(rtConfig,statusListener)
        }
    }

    private fun runJava(rtConfig: RtConfig,statusListener: RtCoreListener?){
        val looper = Looper()
        looper.prepare()

        onException = null

        mainJob?.cancel()
        mainJob = null
        mainJob = scope.launch {
            runCore(rtConfig,statusListener)
            looper.stop()
        }
        looper.loop()
    }

    private suspend fun runCore(rtConfig: RtConfig,statusListener: RtCoreListener?){
        withContext(Dispatchers.Default) {
            val mainLooper = Looper()
            mainLooper.prepare()
            Looper.setMainLooper(mainLooper)

            val rxLifeListener = object : RtCoreListener {
                override fun onStatusChange(status: RtCoreListener.Status) {
                    statusListener?.onStatusChange(status)
                }

                override fun onClientIn(client: Client) {
                    statusListener?.onClientIn(client)
                }

                override fun onClientOut(client: Client) {
                    statusListener?.onClientOut(client)
                }

                override fun onRtCoreException(exception: Exception) {
                    statusListener?.onRtCoreException(exception)
                }

                override fun onCreateContext(rtContext: RtContext) {
                    statusListener?.onCreateContext(rtContext)
                }

                override fun onDestroyContext(rtContext: RtContext) {
                    statusListener?.onDestroyContext(rtContext)
                }
            }

            onException = {
                rxLifeListener.onRtCoreException(it)
            }

            val rtLife = RtLife(rtConfig, rxLifeListener)
            statusListener?.onStatusChange(RtCoreListener.Status.INIT)
            rtLife.onInit()
            statusListener?.onStatusChange(RtCoreListener.Status.RUNNING)
            val runningJob = launch {
                rtLife.onRunning()
            }
            while (active.get()) {
                delay(500)
            }
            runningJob.cancel()
            statusListener?.onStatusChange(RtCoreListener.Status.STOPPING)
            rtLife.onStop()
            statusListener?.onStatusChange(RtCoreListener.Status.STOPPED)
            rtLife.onDestroy()
            "RtCore end".logInfo()
        }
    }

    fun stop() {
        onException = null
        isStopping.set(false)
        active.set(false)
    }

    fun stopAfter(duration: Duration) {
        if (isStopping.get()){
            return
        }
        isStopping.set(true)

        scope.launch {
            delay(duration.toMillis())
            if (isStopping.get()){
                stop()
            }
        }
    }
}



fun main(){
    thread {
        RtCore.instance.run(RtConfig(),object :RtCoreListener{
            override fun onStatusChange(status: RtCoreListener.Status) {
                println("status:$status")
            }

            override fun onClientIn(client: Client) {
                println("onClientIn:")
                client.listen(object :ClientListener{
                    override suspend fun onRtHeartbeat(client: Client) {
                        println("onRtHeartbeatIn:")
                    }

                    override fun onRtClientIn(client: Client, response: Response) {
                        println("onRtClientIn:")
                        thread {
                            var i = 1
                            while (true){
                                sleep(2000)
                                response.write("hello:$i")
                                i++
                            }
                        }
                    }

                    override suspend fun onRtMessage(request: Request,response: Response) {
                        println("onRtMessage:${request.getPackage().getRequestURI()}")
                    }

                    override fun onRtClientOut(client: Client,response: Response) {
                        println("onRtClientOut:")
                    }

                    override suspend fun onMessage(client: Client, request: Request, response: Response) {
                        println("onRtMessage:${RtUtils.getPublishHost(request)},${RtUtils.getLocalHost(request.getContext())},url:${request.getPackage().path},${request.getPackage().getRequestURI().toString()},${request.getPackage().getSession().getId()}")
                        response.addCookie(Cookie("aa","dd", expires = Date(System.currentTimeMillis() + 1000000)))
                        response.write(request.getPackage().getSession().getId(),RtContentType.TEXT_HTML.content)
                    }

                    override suspend fun onInputStreamIn(client: Client, inputStream: InputStream) {

                    }

                    override fun onException(exception: Exception) {
                        exception.printStackTrace()
                        println("onRtCoreException:$exception")
                    }
                })
            }

            override fun onClientOut(client: Client) {
                println("onClientOut:")

            }

            override fun onRtCoreException(exception: Exception) {
                exception.printStackTrace()
                println("onRtCoreException:$exception")

            }

        })
    }
}