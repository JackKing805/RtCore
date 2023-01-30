package com.jerry.rt.core.http.pojo

import com.jerry.rt.utils.RtUtils
import java.net.URI
import java.util.Date

/**
 * @className: Session
 * @description: 每个链接的Cookie
 * 　　Set－Cookie: NAME=VALUE；Expires=DATE；Path=PATH；Domain=DOMAIN_NAME；SECURE -->格式
 * @author: Jerry
 * @date: 2023/1/26:14:24
 **/
data class Cookie(
    /**
     * Cookie name.
     */
    val name: String,
    /**
     * Cookie value.
     */
    val value: String,
    /**
     * `;Domain=VALUE ...` domain that sees cookie
     */
    val domain: String? = null,
    /**
     * DD－MM－YY HH:MM:SS GMT
     */
    val expires:Date?=null,
    /**
     * `;Max-Age=VALUE ...` cookies auto-expire
     */
    val maxAge: Int = -1,
    /**
     * `;Path=VALUE ...` URLs that see the cookie
     * Path属性定义了Web服务器上哪些路径下的页面可获取服务器设置的Cookie。
     * 一般如果用户输入的URL中的路径部分从第一个字符开始包含Path属性所定义的字符串，
     * 浏览器就认为通过检查。如果Path属性的值为“/”，
     * 则Web服务器上所有的WWW资源均可读取该Cookie。
     * 同样该项设置是可选的，如果缺省时，则Path的属性值为Web服务器传给浏览器的资源的路径名。
     */
    val path: String = "",
    /**
     * `;Secure ...` e.g. use SSL
     */
    val secure: Boolean = false,
    /**
     * Not in cookie specs, but supported by browsers.
     */
    val httpOnly: Boolean = false
) {
    fun toCookieString(requestUri:URI): String {
        val cookie = StringBuilder("")
        cookie.append("$name=$value;")
        expires?.let {
            cookie.append("Expires=${RtUtils.dateToFormat(it,"DD－MM－YY HH:MM:SS")};")
        }
        val llPath = if(path.isEmpty()){
            requestUri.path
        }else{
            path
        }
        llPath.let {
            cookie.append("Path=$it;")
        }
        domain?.let {
            cookie.append("Domain=$it;")
        }
        maxAge.let {
            cookie.append("Max-Age=$it;")
        }
        if (secure){
            cookie.append("Secure;")
        }
        if(httpOnly){
            cookie.append("HttpOnly;")
        }

        val str = cookie.toString()

        return if (str.endsWith(";")){
            str.substring(0,str.length-2)
        }else{
            str
        }
    }
}