package com.jerry.rt.core.http.response

import com.jerry.rt.core.http.response.interfaces.Writer
import java.io.OutputStream

/**
 * @className: ResponseWriter
 * @description: 结果返回
 * @author: Jerry
 * @date: 2023/1/1:16:13
 **/
abstract class ResponseWriter<T>(protected val outputStream: OutputStream):Writer<T> {
    private var isFirstLineWrite =false
    private var isHeaderWrite = false
    private var isFirstBody = true


    override fun writeFirstLine(protocol: String, code: Int, msg: String) {
        isFirstLineWrite = true

    }

    override fun writeLine(line: String) {

    }

    override fun writeHeader(key: String, value: Any) {
        checkWriteFirstLine()
        isHeaderWrite = true
    }

    override fun writeBody(content: T) {
        checkWriteFirstLine()
        checkWriteHeaer()

    }

    open fun endWrite(){
        checkWriteFirstLine()
        checkWriteHeaer()
    }

    fun checkWriteHeaer(){
         if (!isHeaderWrite){
            throw IllegalStateException("please write header")
        }
    }

    fun checkWriteFirstLine(){
        if (!isFirstLineWrite){
            throw IllegalStateException("please write first line")
        }
    }

    fun isFirstBody() = isFirstBody.also{
        isFirstBody = false
    }

    fun reset(){
        isFirstBody = true
        isHeaderWrite = false
        isFirstLineWrite = false
    }
}