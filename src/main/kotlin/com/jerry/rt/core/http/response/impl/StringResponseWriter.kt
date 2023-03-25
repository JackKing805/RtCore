package com.jerry.rt.core.http.response.impl

import com.jerry.rt.core.http.response.ResponseWriter
import java.io.OutputStream
import java.io.PrintWriter
import java.lang.Exception
import kotlin.jvm.Throws

/**
 * @className: StringResponseWriter
 * @description: string 结果
 * @author: Jerry
 * @date: 2023/1/1:16:21
 **/
class StringResponseWriter(outputStream: OutputStream): ResponseWriter<String>(outputStream) {
    private val printWriter = PrintWriter(outputStream)

    @Throws(Exception::class)
    override fun writeFirstLine(protocol: String, code: Int, msg: String) {
        super.writeFirstLine(protocol, code, msg)
        writeLine("$protocol $code $msg")
    }

    @Throws(Exception::class)
    override fun writeLine(line: String) {
        super.writeLine(line)
        if (line.endsWith("\r\n")){
            printWriter.write(line)
        }else{
            printWriter.write(line+"\r\n")
        }
        printWriter.flush()
    }

    @Throws(Exception::class)
    override fun writeHeader(key: String, value: Any) {
        super.writeHeader(key, value)
        writeLine("$key: $value")
    }

    @Throws(Exception::class)
    override fun writeBody(content: String) {
        super.writeBody(content)
        if (isFirstBody()){
            writeLine("\r\n")
        }
        printWriter.write(content)
        printWriter.flush()
    }

    @Throws(Exception::class)
    override fun writeBody(content: String, offset: Int, size: Int) {
        super.writeBody(content, offset, size)
        printWriter.write(content,offset,size)
        printWriter.flush()
    }

    @Throws(Exception::class)
    override fun endWrite() {
        super.endWrite()
        printWriter.flush()
        reset()
    }

    override fun writeDividingLine() {
        if (isFirstBody()){
            writeDividingLine()
        }
    }
}