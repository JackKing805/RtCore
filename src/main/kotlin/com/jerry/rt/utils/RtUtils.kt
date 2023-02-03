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
    fun getLocalHost(rtContext: RtContext):String{
        return try {
            InetAddress.getLocalHost().hostAddress + ":" + rtContext.getRtConfig().port
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
}