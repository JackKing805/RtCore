package com.jerry.rt.core.http.interfaces

import com.jerry.rt.bean.RtSessionConfig
import com.jerry.rt.core.RtContext
import com.jerry.rt.core.http.pojo.ProtocolPackage
import java.net.URI


/**
 * @className: ISessionManager
 * @description: session控制器
 * @author: Jerry
 * @date: 2023/1/26:18:37
 **/
interface ISessionManager {
    fun active(rtSessionConfig: RtSessionConfig)

    fun deactivate()

    /*
     * 自定义key的获取方式
     */
    fun getSessionKey(rtContext: RtContext, path:String, uri:URI, header: ProtocolPackage.Header):String?

    fun createSession(sessionId: String?): ISession

    fun removeSession(session: ISession)

    fun findSession(sessionId: String?): ISession?

    /**
     * 创建认证key
     */
    fun generateSessionId(): String
}