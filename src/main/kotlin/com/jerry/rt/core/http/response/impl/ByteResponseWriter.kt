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
class ByteResponseWriter(outputStream: OutputStream): ResponseWriter<ByteArray>(outputStream) {
    override fun writeFirstLine(protocol: String, code: Int, msg: String) {
        super.writeFirstLine(protocol, code, msg)
        writeLine("$protocol $code $msg")
    }

    override fun writeLine(line: String) {
        super.writeLine(line)
        if (line.endsWith("\r\n")){
            outputStream.write(line.toByteArray())
        }else{
            outputStream.write((line+"\r\n").toByteArray())
        }
    }

    override fun writeHeader(key: String, value: Any) {
        super.writeHeader(key, value)
        writeLine("$key: $value")
    }

    private var isFirstBody = true

    override fun writeBody(content: ByteArray) {
        super.writeBody(content)
        if (isFirstBody){
            isFirstBody = false
            writeLine("\r\n")
        }

        outputStream.write(content)
    }

    override fun endWrite() {
        super.endWrite()
        if (isFirstBody){
            isFirstBody = false
            writeLine("\r\n")
        }
        outputStream.flush()
    }
}