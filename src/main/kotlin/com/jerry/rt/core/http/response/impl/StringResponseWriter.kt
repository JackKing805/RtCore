package com.jerry.rt.core.http.response.impl

import com.jerry.rt.core.http.response.ResponseWriter
import java.io.OutputStream
import java.io.PrintWriter

/**
 * @className: StringResponseWriter
 * @description: string 结果
 * @author: Jerry
 * @date: 2023/1/1:16:21
 **/
class StringResponseWriter(outputStream: OutputStream): ResponseWriter<String>(outputStream) {
    private val printWriter = PrintWriter(outputStream)

    override fun writeFirstLine(protocol: String, code: Int, msg: String) {
        super.writeFirstLine(protocol, code, msg)
        writeLine("$protocol $code $msg")
    }

    override fun writeLine(line: String) {
        super.writeLine(line)
        if (line.endsWith("\r\n")){
            printWriter.write(line)
        }else{
            printWriter.write(line+"\r\n")
        }
    }

    override fun writeHeader(key: String, value: Any) {
        super.writeHeader(key, value)
        writeLine("$key: $value")
    }

    private var isFirstBody = true

    override fun writeBody(content: String) {
        super.writeBody(content)
        if (isFirstBody){
            isFirstBody = false
            writeLine("\r\n")
        }
        printWriter.write(content)
    }

    override fun endWrite() {
        super.endWrite()
        if (isFirstBody){
            isFirstBody = false
            writeLine("\r\n")
        }
        printWriter.flush()
    }
}