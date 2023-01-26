package com.jerry.rt.utils

import com.jerry.rt.core.Context
import com.jerry.rt.core.http.pojo.Request
import java.net.InetAddress
import java.text.SimpleDateFormat
import java.util.Date

/**
 * @className: RtUtils
 * @author: Jerry
 * @date: 2023/1/26:15:14
 **/
object RtUtils {
    fun getLocalHost(context: Context):String{
        return try {
            InetAddress.getLocalHost().hostAddress + ":" + context.getRtConfig().port
        }catch (e:Exception){
            e.printStackTrace()
            ""
        }
    }

    fun getPublishHost(request: Request):String{
        return request.getPackage().getHeaderValue("Host","")
    }


    fun dateToFormat(date: Date,format:String):String{
        val simpleDateFormat = SimpleDateFormat(format)
        return simpleDateFormat.format(date)
    }
}