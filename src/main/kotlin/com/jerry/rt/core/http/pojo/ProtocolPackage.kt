package com.jerry.rt.core.http.pojo

import com.jerry.rt.core.RtContext
import com.jerry.rt.core.http.interfaces.ISession
import com.jerry.rt.core.http.protocol.RtHeader
import com.jerry.rt.core.http.protocol.RtMethod
import com.jerry.rt.core.http.protocol.RtVersion
import java.net.URI

/**
 * @className: ComInPackage
 * @description: 请求包体
 * @author: Jerry
 * @date: 2023/1/7:18:53
 **/
class ProtocolPackage(
    private val rtContext: RtContext,
    val method: String,
    val path: String,
    val protocol: RtVersion,
    private val header: Header
) {
    private val rootPath = "${RtVersion.getPrefix(protocol)}://"+header.getHeaderValue(RtHeader.HOST.content,"")
    private val realUrl = rootPath + if (path.startsWith("/")) path else "/$path"
    private val requestURI = URI.create(realUrl)


    fun isRtConnect() = method.equals(RtMethod.RT.content,true) && protocol == RtVersion.RT_1_0


    fun getHeader() = header


    //获取项目根路径
    fun getRootAbsolutePath() = if(rootPath.endsWith("/")) rootPath else "$rootPath/"

    //获取访问绝对地址
    fun getRequestAbsolutePath() = realUrl

    //获取访问地址
    fun getRequestPath() = requestURI.path


    fun getRequestURI() = requestURI


    private var session:ISession?=null

    fun getSession():ISession{
        if (session==null){
            val s = getSessionKey()
            //创建session和刷新session
            val createSession = rtContext.getSessionManager().createSession(s)
            session = createSession
        }
        return session!!
    }

    private fun getSessionKey() = rtContext.getSessionManager().getSessionKey(rtContext,path,requestURI,header)


    data class Header(
        private val header: MutableMap<String, String>){

        private val cookies = mutableMapOf<String,String>()

        init {
            getHeaderValue(RtHeader.COOKIE.content,"").split(";").forEach {
                if (it.contains("=")){
                    val index = it.indexOf("=")
                    val name = it.substring(0,index).trim()
                    val value = it.substring(index+1).trim()
                    cookies[name] = value
                }
            }
        }

        fun getHeaders() = header

        fun addHeader(key: String,value:String){
            header[key] = value
        }

        fun getHeaderValue(key: String, default: String = ""): String {
            return (header[key] ?: default).trim()
        }

        fun getHeaderValueOrNull(key: String): String? {
            return header[key]?.trim()
        }

        fun getContentType() = getHeaderValue(RtHeader.CONTENT_TYPE.content, "text/plain")

        fun getContentLength() = try {
            val values = getHeaderValue(RtHeader.CONTENT_LENGTH.content)
            if (values.isEmpty()) {
                0
            }else {
                values.toInt()
            }
        } catch (e: Exception) {
            0
        }

        fun getAcceptRanges():Array<Int>?{
            val ranges = getHeaderValue("Range")
            if (ranges.isEmpty()){
                return null
            }else{
                var x = 0
                var y = 0
                if (ranges.startsWith("bytes=")) {
                    val range = ranges.split("=")[1]//bytes=x-y
                    val split = range.split("-")
                    if(split.size==1){
                        if (range.startsWith("-")){
                            y = -(split[0].toIntOrNull()?:0)
                        }else{
                            x = split[0].toIntOrNull()?:0
                        }
                    }else{
                        x = split[0].toIntOrNull()?:0
                        if (split.size > 1) {
                            y = split[1].toIntOrNull()?:0
                        }
                    }
                }
                return arrayOf(x,y)
            }
        }

        fun getDate() = getHeaderValue(RtHeader.DATE.content, "")

        fun getUserAgent() = getHeaderValue(RtHeader.USER_AGENT.content, "")

        fun getCookies() = cookies

        fun getCookie(name:String) = getCookies()[name]
    }
}