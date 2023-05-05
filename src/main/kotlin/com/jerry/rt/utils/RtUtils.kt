package com.jerry.rt.utils

import com.jerry.rt.core.RtContext
import com.jerry.rt.core.http.pojo.Request
import java.net.InetAddress
import java.text.SimpleDateFormat
import java.util.*

/**
 * @className: RtUtils
 * @author: Jerry
 * @date: 2023/1/26:15:14
 **/
object RtUtils {
    //获取局域网ip加端口
    fun getLocalHost(rtContext: RtContext):String{
        return try {
            InetAddress.getLocalHost().hostAddress + ":" + rtContext.getRtConfig().rtInitConfig.port
        }catch (e:Exception){
            e.printStackTrace()
            ""
        }
    }

    fun getPublishHost(request: Request):String{
        return request.getPackage().getHeader().getHeaderValue("Host","")
    }


    fun dateToFormat(date: Date,format:String):String{
        val simpleDateFormat = SimpleDateFormat(format, Locale.US)
        simpleDateFormat.timeZone = TimeZone.getTimeZone("GMT")
        return simpleDateFormat.format(date)
    }

    //获取局域网ip
    fun getLocalIpAddress(): String {
        val host = InetAddress.getLocalHost()
        val ip = host.hostAddress

        // 检查是否是局域网 IP 地址
        if (!ip.startsWith("192.168.")) {
            throw Exception("Not a LAN IP address")
        }

        return ip
    }
}