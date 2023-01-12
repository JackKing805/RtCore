package com.jerry.rt.core

import com.jerry.rt.bean.RtConfig
import com.jerry.rt.core.http.Client
import com.jerry.rt.core.http.interfaces.ClientListener
import com.jerry.rt.core.http.pojo.Request
import com.jerry.rt.core.http.pojo.Response
import com.jerry.rt.core.http.protocol.RtContentType
import com.jerry.rt.core.thread.Looper
import com.jerry.rt.extensions.createExceptionCoroutineScope
import com.jerry.rt.extensions.createStandCoroutineScope
import com.jerry.rt.extensions.logInfo
import com.jerry.rt.interfaces.RtCoreListener
import com.jerry.rt.utils.PlatformUtils
import kotlinx.coroutines.*
import java.io.File
import java.io.InputStream
import java.time.Duration
import java.util.concurrent.atomic.AtomicBoolean

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
        }

        onException = {
            rxLifeListener.onRtCoreException(it)
        }

        val rtLife = RtLife(rtConfig, rxLifeListener)
        statusListener?.onStatusChange(RtCoreListener.Status.INIT)
        rtLife.onInit()
        statusListener?.onStatusChange(RtCoreListener.Status.RUNNING)
        val runningJob = withContext(Dispatchers.Default){
            launch {
                rtLife.onRunning()
            }
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



//fun main(){
//    val isAndroid = PlatformUtils.isAndroid()
//    println("isAndroid:$isAndroid")
//    RtCore.instance.run(RtConfig(),object :RtCoreListener{
//        override fun onStatusChange(status: RtCoreListener.Status) {
//            println("status:$status")
//        }
//
//        override fun onClientIn(client: Client) {
//            println("onClientIn:")
//            client.listen(object :ClientListener{
//                override suspend fun onRtHeartbeatIn(client: Client) {
//                    println("onRtHeartbeatIn:")
//                }
//
//                override suspend fun onMessage(client: Client, request: Request, response: Response) {
//                    println("onMessage:")
//                    response.write("ssssss",RtContentType.TEXT_HTML.content)
//                }
//
//                override suspend fun onInputStreamIn(client: Client, inputStream: InputStream) {
//
//                }
//
//                override fun onException(exception: java.lang.Exception) {
//
//                }
//            })
//        }
//
//        override fun onClientOut(client: Client) {
//            println("onClientOut:")
//
//        }
//
//        override fun onRtCoreException(exception: Exception) {
//            println("onRtCoreException:$exception")
//
//        }
//
//    })
//}