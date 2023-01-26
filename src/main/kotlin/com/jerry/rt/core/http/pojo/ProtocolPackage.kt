package com.jerry.rt.core.http.pojo

import com.jerry.rt.core.Context
import com.jerry.rt.core.http.interfaces.ISession
import com.jerry.rt.core.http.other.SessionManager
import com.jerry.rt.core.http.protocol.RtVersion
import java.net.URI

/**
 * @className: ComInPackage
 * @description: 请求包体
 * @author: Jerry
 * @date: 2023/1/7:18:53
 **/
class ProtocolPackage(
    private val context: Context,
    val method: String,
    val path: String,
    val protocol: RtVersion,
    private val header: Header
) {
    private val realUrl = "${RtVersion.getPrefix(protocol)}://"+header.getHeaderValue("Host","") + if (path.startsWith("/")) path else "/$path"
    private val requestURI = URI.create(realUrl)

    fun getHeader() = header


    fun getRequestURI() = requestURI


    private var session:ISession?=null

    fun getSession():ISession{
        if (session==null){
            val s = getSessionKey()
            //创建session和刷新session
            val createSession = context.getSessionManager().createSession(s)
            session = createSession
        }
        return session!!
    }

    private fun getSessionKey() = context.getSessionManager().getSessionKey(context,path,requestURI,header)


    data class Header(
        private val header: MutableMap<String, String>){

        private val cookies = mutableMapOf<String,String>()

        init {
            getHeaderValue("Cookie","").split(";").forEach {
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

        fun getContentType() = getHeaderValue("Content-Type", "text/plain")

        fun getContentLength() = try {
            val values = getHeaderValue("Content-Length")
            if (values.isEmpty()) {
                0L
            }else {
                values.toLong()
            }
        } catch (e: Exception) {
            0L
        }

        fun getDate() = getHeaderValue("Date", "")

        fun getUserAgent() = getHeaderValue("User-Agent", "")

        fun getCookies() = cookies

        fun getCookie(name:String) = getCookies()[name]
    }
}