package com.jerry.rt.core.http.interfaces

import com.jerry.rt.bean.RtSessionConfig


/**
 * @className: ISessionManager
 * @description: session控制器
 * @author: Jerry
 * @date: 2023/1/26:18:37
 **/
interface ISessionManager {
    fun active(rtSessionConfig: RtSessionConfig)

    fun deactivate()

    fun createSession(sessionId: String?): ISession

    fun removeSession(session: ISession)

    fun findSession(sessionId: String?): ISession?

    fun generateSessionId(): String
}