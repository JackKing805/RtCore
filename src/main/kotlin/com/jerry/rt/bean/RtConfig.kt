package com.jerry.rt.bean

import com.jerry.rt.core.http.interfaces.ISessionManager
import com.jerry.rt.core.http.other.SessionManager
import java.time.Duration

/**
 * @className: RtConfig
 * @description: RtCore 启动配置类
 * @author: Jerry
 * @date: 2022/12/31:16:25
 **/
data class RtConfig(
    val port:Int = 8080,
    val customerParse:Boolean = false,//是否自主解析inputStream
    val heartbeatReceiverIntervalTime:Duration = Duration.ofSeconds(10),//收到心跳包最小间隔时间
    val rtSessionConfig: RtSessionConfig = RtSessionConfig()
)


data class RtSessionConfig(
    val sessionValidTime:Duration = Duration.ofMinutes(15),//session有效时间
    val sessionClazz:Class<out ISessionManager> = SessionManager::class.java,
    val sessionKey:String = "JESSIONID"
)