package com.jerry.rt.core.http.request.model

import com.jerry.rt.core.http.pojo.ProtocolPackage
import com.jerry.rt.core.http.protocol.RtContentType
import com.jerry.rt.extensions.logError
import java.io.File
import java.io.FileOutputStream
import java.util.*

/**
 * @className: MultipartFile
 * @description: 用户上传的文件
 * @author: Jerry
 * @date: 2023/2/12:14:46
 **/
class MultipartFile(private val protocolPackage: ProtocolPackage,private val socketBody: SocketBody) {
    private var boundary = ""
    private var endBoundary = ""
    private var contentLength = protocolPackage.getHeader().getContentLength().toLong()

    private var fileNameKey = ""
    private var fileName = ""
    private var fileContentType = ""

    private var readLength = 0L



    //todo 文件的读取有问题，一直无法读取到正确的字节数
    init {
        val contentType = protocolPackage.getHeader().getContentType()
        if (contentType.contains("boundary")){
            val boundaryLine =contentType.substringAfter(RtContentType.MULTIPART.content+"; boundary=").trim()
            boundary = "--$boundaryLine\r\n"
            endBoundary = "--$boundaryLine--\r\n"
        }
        "boundary:$boundary".logError()
        "endBoundary:$endBoundary".logError()


         val lines = mutableListOf<String>()
        while (true){
            val readLine = socketBody.readLine()
            readLength+=(readLine?.length?:0)+2
            lines.add(readLine?:"")
            if (readLine==""){
                break
            }
        }

        lines.forEach {
            if (it.startsWith("Content-Disposition:")){
                it.split(";").forEach {iner->
                    if (iner.startsWith(" name")){
                        fileNameKey = iner.substringAfter(" name=").trim('"')
                    }else if(iner.startsWith(" filename")){
                        fileName = iner.substringAfter(" filename=").trim('"')
                    }
                }
            }else if(it.startsWith("Content-Type")){
                fileContentType = it.substringAfter("Content-Type:").trim()
            }
        }

        if (fileName.isEmpty()){
            fileName = UUID.randomUUID().toString()
        }
    }

    fun getFileName() = fileName

    fun getFileNameKey() =fileNameKey

    fun save(file:File){
        if (!file.exists()){
            throw NullPointerException("$file not exists")
        }
        val fileOutputStream = FileOutputStream(file)

        "readLength:$readLength,contentLength:$contentLength".logError()
        while (readLength<contentLength){
            val readLine = socketBody.readLine()
            if (readLine!=null){
                if (readLine!=endBoundary){
                    fileOutputStream.write(readLine.toByteArray())
                }
                readLength += readLine.length+2
                "readLength:$readLength,contentLength:$contentLength".logError()
            }
        }

        "readLength:$readLength,contentLength:$contentLength".logError()

        fileOutputStream.close()
    }
}