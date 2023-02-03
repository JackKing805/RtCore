package com.jerry.rt.core

import com.jerry.rt.bean.RtConfig
import com.jerry.rt.core.exceptions.DestroyContext
import com.jerry.rt.core.http.interfaces.ISessionManager
import com.jerry.rt.core.thread.Looper
import com.jerry.rt.extensions.logInfo

/**
 * @className: Context
 * @description: RtCore 上下文
 * @author: Jerry
 * @date: 2022/12/31:15:10
 **/
class RtContext(private val rtConfig: RtConfig, private val sessionManager: ISessionManager) {
    private val mainLooper: Looper = Looper.getMainLooper()
    private var isLive = true

    private fun checkContext(){
        if (!isLive){
            throw DestroyContext()
        }
    }

    fun getMainLooper() = mainLooper

    fun getRtConfig():RtConfig{
        checkContext()
        return rtConfig
    }

    fun getSessionManager():ISessionManager{
        checkContext()
        return sessionManager
    }

    fun destroy(){
        isLive = false
    }

    fun log(msg:String){
        msg.logInfo()
    }


}