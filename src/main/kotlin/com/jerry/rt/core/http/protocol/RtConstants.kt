package com.jerry.rt.core.http.protocol

/**
 * @className: RtConstants
 * @description: rt协议内容
 * @author: Jerry
 * @date: 2023/1/1:13:02
 **/
enum class RtContentType(val content:String) {
    RT_HEARTBEAT("rt/heartbeat")
}

enum class RtVersion(val content: String){
    RT_1_0("rt/1.0")
}

enum class RtMethod(val content: String){
    RT("rt")
}