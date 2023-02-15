package com.jerry.rt.bean

import com.jerry.rt.core.http.interfaces.ISessionManager
import com.jerry.rt.core.http.other.SessionManager
import com.jerry.rt.core.http.request.impl.SocketListenerImpl
import java.io.File
import java.time.Duration

/**
 * @className: RtConfig
 * @description: RtCore 启动配置类
 * @author: Jerry
 * @date: 2022/12/31:16:25
 **/
data class RtConfig(
    val port:Int = 8080,
    val heartbeatReceiverIntervalTime:Duration = Duration.ofSeconds(10),//收到心跳包最小间隔时间
    val rtSessionConfig: RtSessionConfig = RtSessionConfig(),
    val serverVersionDetails:String = "RtServer/1.0",
    val socketListener: Class<out SocketListenerImpl> = SocketListenerImpl::class.java,//socket进入时负责数据处理的类
    val rtSSLConfig: RtSSLConfig?=null,
    val rtFileConfig:RtFileConfig,
    val rtTimeOutConfig: RtTimeOutConfig = RtTimeOutConfig()
)


data class RtSessionConfig(
    val sessionValidTime:Duration = Duration.ofMinutes(15),//session有效时间
    val sessionClazz:Class<out ISessionManager> = SessionManager::class.java,
    val sessionKey:String = "JESSIONID"
)

data class RtSSLConfig(
    //只能配置地址为本地文件
    val keyStoreFile:File,
    val keyStorePassword:String,
    val keyPassword:String
)

data class RtFileConfig(
    val tempFileDir:String,//上传文件的临时存储文职
    val saveFileDir:String,//用户文件保存位置
    val uploadMaxSize:Long = -1,//最大文件大小，默认无限制
    val memoryThreshold:Int = 8192,//文件保存到内存的边界
    val tempFileMaxAge:Long = 1000L*60*60*24*10//临时文件最长存在时间，默认10天
)

data class RtTimeOutConfig(
    val soTimeout:Int = 10
)