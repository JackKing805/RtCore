package com.jerry.rt.core.http.protocol

/**
 * @className: RtProtocol
 * @description: rt协议
 * @author: Jerry
 * @date: 2022/12/31:18:00
 **/
//rt协议:
//rt(协议名称) url(访问地址) version(协议版本)\r\n
//key(名字):value(内容)\r\n
//\r\n
//(协议内容)


/**
 * content-type:rt/connect 表示rt链接
 * content-type:rt/heartbeat 表示rt心跳
 */
 data class RtProtocol(
    val protocol: Protocol = Protocol("","",""),
    val header: MutableList<Header> = mutableListOf()
){
    fun getHeaderMap():MutableMap<String,Any>{
        val map = mutableMapOf<String,Any>()
        header.forEach {
            map[it.key] = it.value
        }
        return map
    }
}

 data class Protocol(
    var method:String,
    var url:String,
    var version:String
)

 data class Header(
    var key:String,
    var value:Any
)