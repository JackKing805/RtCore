package com.jerry.rt.core.http.pojo

import com.jerry.rt.core.http.response.ResponseWriter
import java.io.OutputStream
import kotlin.reflect.KClass

/**
 * @className: Response
 * @description: 返回
 * @author: Jerry
 * @date: 2023/1/6:19:47
 **/
class Response(private val output:OutputStream) {
    fun <T: ResponseWriter<*>> getResponseWrite(clazz: KClass<T>):T{
        val constructor = clazz.java.getConstructor(OutputStream::class.java)
        return constructor.newInstance(output)
    }
}