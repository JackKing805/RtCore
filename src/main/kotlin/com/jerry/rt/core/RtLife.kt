package com.jerry.rt.core

import com.jerry.rt.bean.RtConfig
import com.jerry.rt.core.http.ClientDispatchers
import com.jerry.rt.core.http.Server
import com.jerry.rt.core.http.interfaces.ISessionManager
import com.jerry.rt.extensions.logInfo
import com.jerry.rt.interfaces.RtCoreListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext

/**
 * @className: RtLife
 * @description: RtCore 生命周期
 * @author: Jerry
 * @date: 2022/12/31:16:17
 **/
internal class RtLife(private val rtConfig: RtConfig,private val rtCoreListener: RtCoreListener) {
    private lateinit var rtContext: RtContext
    private lateinit var server: Server
    private lateinit var clientDispatchers: ClientDispatchers
    private lateinit var sessionManager: ISessionManager

    suspend fun onInit() {
        "RtLife>>onInit".logInfo()
        sessionManager = rtConfig.rtSessionConfig.sessionClazz.newInstance() as ISessionManager
        rtContext = RtContext(rtConfig,sessionManager)
        sessionManager.active(rtConfig.rtSessionConfig)
        server = Server(rtContext){
            rtCoreListener.onRtCoreException(it)
        }
        clientDispatchers = ClientDispatchers(rtContext,rtCoreListener)

        rtCoreListener.onCreateContext(rtContext)
    }

    suspend fun onRunning() {
        "RtLife>>onRunning".logInfo()
        withContext(Dispatchers.IO){
            val run = server.run()
            run.onEach {
                clientDispatchers.onClientIn(it)
            }.collect()
        }
    }

    suspend fun onStop() {
        "RtLife>>onStopping".logInfo()
        server.stop()
        clientDispatchers.clear()
        sessionManager.deactivate()
    }

    suspend fun onDestroy() {
        "RtLife>>onDestroy".logInfo()
        rtContext.destroy()
        rtCoreListener.onDestroyContext(rtContext)
    }
}