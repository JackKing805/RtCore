package com.jerry.rt.core

import com.jerry.rt.bean.RtConfig
import com.jerry.rt.core.http.Client
import com.jerry.rt.core.thread.Looper
import com.jerry.rt.extensions.createStandCoroutineScope
import com.jerry.rt.extensions.logInfo
import com.jerry.rt.interfaces.RtCoreListener
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import java.util.concurrent.atomic.AtomicBoolean

class RtCore private constructor() {
    companion object {
        val instance by lazy { RtCore() }
    }

    private var scope = createStandCoroutineScope()

    private var active = AtomicBoolean(false)
    private var isStopping = AtomicBoolean(false)
    private var mainJob: Job? = null


    fun run(rtConfig: RtConfig, statusListener: RtCoreListener? = null) {
        if (active.get()){
            return
        }
        active.set(true)

        "RtCore start".logInfo()

        val looper = Looper()
        looper.prepare()


        mainJob?.cancel()
        mainJob = null
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
            while (active.get()) {
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