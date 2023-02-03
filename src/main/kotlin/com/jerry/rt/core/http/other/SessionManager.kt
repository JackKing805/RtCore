package com.jerry.rt.core.http.other

import com.jerry.rt.bean.RtSessionConfig
import com.jerry.rt.core.RtContext
import com.jerry.rt.core.http.interfaces.ISession
import com.jerry.rt.core.http.interfaces.ISessionManager
import com.jerry.rt.core.http.pojo.ProtocolPackage
import com.jerry.rt.core.http.pojo.Session
import com.jerry.rt.extensions.createStandCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.net.URI
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @className: SessionManager
 * @description: 创建客户端session
 * @author: Jerry
 * @date: 2023/1/26:14:28
 **/
class SessionManager :ISessionManager{
    private val scope = createStandCoroutineScope()
    private var active = false
    private val sessions = CopyOnWriteArrayList<ISession>()

    private lateinit var rtSessionConfig: RtSessionConfig

    override fun active(rtSessionConfig: RtSessionConfig) {
        this.rtSessionConfig = rtSessionConfig
        active = true
    }

    override fun deactivate() {
        active = false
        sessions.forEach {
            it.invalidate()
        }
        sessions.clear()
        scope.cancel()
    }

    override fun getSessionKey(rtContext: RtContext, path: String, uri: URI, header: ProtocolPackage.Header): String? {
        return header.getCookie(rtContext.getRtConfig().rtSessionConfig.sessionKey)
    }

    override fun createSession(sessionId: String?): ISession {
        if (sessionId.isNullOrEmpty()) {
            return create()
        } else {
            val session = findSession(sessionId) ?: return create()
            session.setIsNew(false)
            session.setLastAccessedTime(System.currentTimeMillis())
            return session
        }
    }

    override fun removeSession(session: ISession) {
        sessions.remove(session)
    }

    override fun findSession(sessionId: String?): ISession? {
        return sessions.find { it.getId() == sessionId }
    }

    private fun create(): Session {
        val session = Session(generateSessionId())
        session.setMaxInactiveInterval(rtSessionConfig.sessionValidTime.toMillis().toInt())
        scope.launch(Dispatchers.IO) {
            session.listen()
            removeSession(session)
        }
        sessions.add(session)
        return session
    }

    override fun generateSessionId(): String {
        return UUID.randomUUID().toString() + UUID.randomUUID().toString() + System.currentTimeMillis().toString()
    }
}

