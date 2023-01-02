package com.jerry.rt.core.http.pojo

/**
 * @className: ClientMessage
 * @description: 客户端暴露给外面的信息
 * @author: Jerry
 * @date: 2023/1/2:13:20
 **/
data class ClientMessage(
    val protocol: Protocol,
    val header: MutableMap<String,Any>,
    val isRtConnect:Boolean
    ){
    fun getHeaderValue(key: String,default:String=""): String {
        return ((header[key] as? String) ?: default).trim()
    }

    //获取内容类型:
    fun getContentType() = getHeaderValue("Content-Type","none")

    //获取内容长度
    fun getContentLength() = try {
        val value = getHeaderValue("Content-Length")
        if (value.isEmpty()){
            0L
        }else{
            value.toLong()
        }
    }catch (e:Exception){
        0L
    }

    //获取访问日期
    fun getDate() = getHeaderValue("Date","")

    //获取浏览器UA
    fun getUserAgent() = getHeaderValue("User-Agent","")

    //获取cookie
    fun getCookie() = getHeaderValue("Cookie","")

    override fun toString(): String {
        return "ClientMessage(protocol=$protocol, header=$header, isRtConnect=$isRtConnect)"
    }
}

data class Protocol(
    val method:String,
    val url:String,
    val protocol: String
)