package com.jerry.rt.core.http.response.interfaces

/**
 * @className: Writer
 * @description: 数据写人
 * @author: Jerry
 * @date: 2023/1/1:16:14
 **/
interface Writer<T> {
    fun writeLine(line:String)

    fun writeFirstLine(protocol:String,code:Int,msg:String)

    fun writeHeader(key:String,value:Any)

    fun writeBody(content: T)
}