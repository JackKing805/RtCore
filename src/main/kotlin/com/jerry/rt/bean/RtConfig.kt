package com.jerry.rt.bean

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
    val heartbeatReceiverIntervalTime:Duration = Duration.ofSeconds(10)//收到心跳包最小间隔时间
)
