package com.jerry.rt.core.http.pojo

/**
 * @className: Request
 * @description: 请求
 * @author: Jerry
 * @date: 2023/1/6:19:47
 **/
data class Request(
    private val protocol: Protocol,
    private val header: MutableMap<String, Any>,
    val isRtConnect: Boolean,
    private val data: MutableList<ByteArray>
) {
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

    data class Protocol(
        val method:String,
        val url:String,
        val protocol: String
    )
}