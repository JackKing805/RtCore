package com.jerry.rt.core

import com.jerry.rt.bean.RtConfig
import com.jerry.rt.core.http.ClientDispatchers
import com.jerry.rt.core.http.Server
import com.jerry.rt.extensions.logInfo
import com.jerry.rt.interfaces.RtCoreListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @className: RtLife
 * @description: RtCore 生命周期
 * @author: Jerry
 * @date: 2022/12/31:16:17
 **/
internal class RtLife(private val rtConfig: RtConfig,private val rtCoreListener: RtCoreListener) {
    private lateinit var context: Context
    private lateinit var server: Server
    private lateinit var clientDispatchers: ClientDispatchers

    suspend fun onInit() {
        "RtLife>>onInit".logInfo()
        context = Context(rtConfig)
        server = Server(context)
        clientDispatchers = ClientDispatchers(context,rtCoreListener)
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
    }

    suspend fun onDestroy() {
        "RtLife>>onDestroy".logInfo()

    }
}