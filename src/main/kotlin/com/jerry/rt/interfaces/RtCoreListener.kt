package com.jerry.rt.interfaces

import com.jerry.rt.core.RtContext
import com.jerry.rt.core.http.Client

/**
 * @className: RtCoreStatusListener
 * @description: 核心的运行状态监控
 * @author: Jerry
 * @date: 2022/12/31:15:28
 **/
interface RtCoreListener {
    enum class Status{
        INIT,
        RUNNING,
        STOPPING,
        STOPPED
    }

    fun onStatusChange(status: Status){}

    fun onCreateContext(rtContext: RtContext){}

    fun onDestroyContext(rtContext: RtContext){}

    fun onClientIn(client: Client){}

    fun onClientOut(client: Client){}

    fun onRtCoreException(exception: Exception){}
}