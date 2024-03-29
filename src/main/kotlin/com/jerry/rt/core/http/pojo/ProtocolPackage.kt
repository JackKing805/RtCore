package com.jerry.rt.core.http.pojo

import com.jerry.rt.core.RtContext
import com.jerry.rt.core.http.interfaces.ISession
import com.jerry.rt.core.http.protocol.RtHeader
import com.jerry.rt.core.http.protocol.RtMethod
import com.jerry.rt.core.http.protocol.RtVersion
import java.net.InetAddress
import java.net.URI
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern

/**
 * @className: ComInPackage
 * @description: 请求包体
 * @author: Jerry
 * @date: 2023/1/7:18:53
 **/
class ProtocolPackage(
    private val rtContext: RtContext,
    private val method: String,
    private val path: String,
    private val protocol: RtVersion,
    private val header: Header
) {
    private val rootPath = "${RtVersion.getPrefix(protocol)}://"+header.getHeaderValue(RtHeader.HOST.content,"") + "/"
    private val realUrl = rootPath + if (path.startsWith("/")) path.substring(1) else path
    private val requestURI = URI.create(realUrl)
    private val CHARSET_PATTERN = Pattern.compile("charset\\s*=\\s*([a-z0-9-]*)", Pattern.CASE_INSENSITIVE)

    private var charset: Charset?=null

    init {
        initCharset()
    }

    private fun initCharset(){
        val contentType = getHeader().getContentType()

        if (contentType.isNotEmpty()){
            val matcher = CHARSET_PATTERN.matcher(contentType)
            if (matcher.find()){
                charset = Charset.forName(matcher.group(1))
            }
        }

        if (charset ==null){
            charset = StandardCharsets.UTF_8
        }
    }

    fun getRequestMethod() = method

    fun getProtocol() = protocol

    fun getCharset():Charset = charset!!

    fun isRtConnect() = method.equals(RtMethod.RT.content,true) && protocol == RtVersion.RT_1_0


    fun getHeader() = header


    //获取项目根路径
    fun getRootAbsolutePath() = rootPath

    //获取访问绝对地址
    fun getRequestAbsolutePath() = realUrl

    //获取访问地址
    fun getRelativePath() = requestURI.path


    fun getRequestURI() = requestURI

    fun getRequestURIQuery() = requestURI.query

    fun isMultipart():Boolean{
        val contentType = getHeader().getContentType().lowercase()
        return contentType.startsWith("multipart/")
    }


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


    data class Header(private val header: MutableMap<String, String>,private val inetAddress: InetAddress){

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


        fun getHeaderValue(key: String, default: String = ""): String {
            return header.entries.find { it.key.trim().lowercase() == key.lowercase() }?.value?.trim()?:default
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

        fun getReferer() = getHeaderValue("Referer","")

        fun getAccept() = getHeaderValue("Accept", "")

        fun getAcceptEncoding() = getHeaderValue("Accept-Encoding","")

        fun getAcceptEncodings() = getHeaderValue("Accept-Encoding","").split(",")

        fun getAcceptLanguage() = getHeaderValue("Accept-Language","")

        fun getCacheControl() = getHeaderValue("Cache-Control","")

        fun getConnection() = getHeaderValue("Connection","")

        fun getHost() = getHeaderValue("Host","")

        /**
         * 获取用户真实IP地址，不使用request.getRemoteAddr();的原因是有可能用户使用了代理软件方式避免真实IP地址,
         * 参考文章： http://developer.51cto.com/art/201111/305181.htm
         *
         * 可是，如果通过了多级反向代理的话，X-Forwarded-For的值并不止一个，而是一串IP值，究竟哪个才是真正的用户端的真实IP呢？
         * 答案是取X-Forwarded-For中第一个非unknown的有效IP字符串。
         *
         * 如：X-Forwarded-For：192.168.1.110, 192.168.1.120, 192.168.1.130,
         * 192.168.1.100
         *
         * 用户真实IP为： 192.168.1.110
         *
         * @param request
         * @return
         */
        fun getIpAddress(): String {
            var ip: String? = getHeaderValueOrNull("x-forwarded-for")
            if (ip.isNullOrEmpty() || "unknown".equals(ip, ignoreCase = true)) {
                ip = getHeaderValueOrNull("Proxy-Client-IP")
            }
            if (ip.isNullOrEmpty() || "unknown".equals(ip, ignoreCase = true)) {
                ip = getHeaderValueOrNull("WL-Proxy-Client-IP")
            }
            if (ip.isNullOrEmpty() || "unknown".equals(ip, ignoreCase = true)) {
                ip = getHeaderValueOrNull("HTTP_CLIENT_IP")
            }
            if (ip.isNullOrEmpty() || "unknown".equals(ip, ignoreCase = true)) {
                ip = getHeaderValueOrNull("HTTP_X_FORWARDED_FOR")
            }
            if (ip.isNullOrEmpty() || "unknown".equals(ip, ignoreCase = true)) {
                ip = inetAddress.hostAddress
            }
            return ip?:""
        }
    }
}