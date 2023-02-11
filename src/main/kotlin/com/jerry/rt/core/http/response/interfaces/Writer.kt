package com.jerry.rt.core.http.response.interfaces

import java.lang.Exception
import kotlin.jvm.Throws

/**
 * @className: Writer
 * @description: 数据写人
 * @author: Jerry
 * @date: 2023/1/1:16:14
 **/
interface Writer<T> {
    @Throws(Exception::class)
    fun writeLine(line:String)
    @Throws(Exception::class)
    fun writeFirstLine(protocol:String,code:Int,msg:String)
    @Throws(Exception::class)
    fun writeHeader(key:String,value:Any)
    @Throws(Exception::class)
    fun writeBody(content: T)
    @Throws(Exception::class)
    fun writeBody(content: T,offset:Int,size:Int)
}