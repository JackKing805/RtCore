package com.jerry.rt.core.http.request.model

import com.jerry.rt.core.RtContext
import com.jerry.rt.core.http.pojo.ProtocolPackage
import com.jerry.rt.core.http.protocol.RtContentType
import com.jerry.rt.extensions.logError
import java.io.File
import java.io.FileOutputStream

/**
 * @className: MultipartFile
 * @description: 用户上传的文件
 * @author: Jerry
 * @date: 2023/2/12:14:46
 *
POST /upload HTTP/1.1
Host: www.example.com
Content-Type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Length: 350

------WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="file"; filename="test.txt"
Content-Type: text/plain

This is the file content.
------WebKitFormBoundary7MA4YWxkTrZu0gW--

 */
class MultipartFormData(
    private val context: RtContext,
    private val protocolPackage: ProtocolPackage,
    private val socketBody: SocketBody
) {
    private lateinit var multipartData: MultipartData

    /**
     *
    HTTP文件上传格式有两种：
    使用 "Content-Type: multipart/form-data" 的 multipart 请求格式：
    css
    Copy code
    POST /upload HTTP/1.1
    Host: example.com
    Content-Type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW
    Content-Length: 554

    ------WebKitFormBoundary7MA4YWxkTrZu0gW
    Content-Disposition: form-data; name="text_field"

    text_value
    ------WebKitFormBoundary7MA4YWxkTrZu0gW
    Content-Disposition: form-data; name="file"; filename="test.txt"
    Content-Type: text/plain

    This is some test text.
    ------WebKitFormBoundary7MA4YWxkTrZu0gW--
    使用 "Content-Type: application/x-www-form-urlencoded" 的请求格式：
    makefile
    Copy code
    POST /upload HTTP/1.1
    Host: example.com
    Content-Type: application/x-www-form-urlencoded
    Content-Length: 28

    text_field=text_value&file=test.txt


    注意：这两种请求格式的请求体里的数据格式不同，接收方需要对应的解析方式来读取请求的内容。
     */
    init {
        val contentType = protocolPackage.getHeader().getContentType().lowercase()
        if (contentType.startsWith(RtContentType.MULTIPART.content)) {
            parseFormData(contentType)
        } else if (contentType == RtContentType.FORM_URLENCODED.content) {
            parseXwwformUrlencoded(contentType)
        } else {
            throw IllegalArgumentException("ill contentType")
        }
    }


    private fun parseXwwformUrlencoded(contentType: String) {
        val data = String(socketBody.readAllData())
        multipartData = MultipartData.XWWForm(data)
    }

    private fun parseFormData(contentType: String) {
        val rtFileConfig = context.getRtConfig().rtFileConfig

        var boundary = ""
        var endBoundary = ""
        val contentLength = protocolPackage.getHeader().getContentLength().toLong()
        if (contentType.contains("boundary")) {
            val boundaryLine = contentType.substringAfter(RtContentType.MULTIPART.content + "; boundary=").trim()
            boundary = "--$boundaryLine\r\n"
            endBoundary = "--$boundaryLine--\r\n"
        }


        val formBodys = mutableListOf<FormBody>()

        "contentLength:$contentLength".logError()
        var line: String? = null
        var readLength = 0L


        var contentDisposition: String = ""
        var mContentType: String = ""
        var name: String = ""
        var fileName: String = ""

        var isNew = true
        var isFile = false

        var readContent = StringBuilder("")
        var fileOutPutStream: FileOutputStream? = null
        var file:File?=null
        while (readLength < contentLength && socketBody.readLine()
                .also { line = if (it == null) null else it + "\r\n" } != null
        ) {
            val tLine = line!!
            //解析头部
            if (isNew) {
                if (tLine == boundary) {
                    contentDisposition = ""
                    mContentType = ""
                    name = ""
                    fileName = ""
                    readContent = StringBuilder("")
                    isNew = true
                    isFile = false
                    file=null
                    fileOutPutStream = null
                } else if (tLine.lowercase().startsWith("content-disposition:")) {
                    val split = tLine.trim().split(";")
                    split.forEach {
                        if (it.lowercase().contains("content-disposition:")) {
                            contentDisposition = it.substringAfter(":").trim()
                        } else if (it.lowercase().trim().startsWith("name=")) {
                            name = it.substringAfter("=").trim('"')
                        } else if (it.lowercase().trim().startsWith("filename=")) {
                            fileName = it.substringAfter("=").trim('"')
                        }
                    }
                } else if (tLine.lowercase().startsWith("content-type:")) {
                    mContentType = tLine.substringAfter(":").trim()
                } else if (tLine == "\r\n") {
                    if (mContentType.isNotEmpty() || fileName.isNotEmpty()) {
                        isFile = true

                        file = File(rtFileConfig.tempFileDir, fileName)
                        file.delete()
                        if (!file.exists()) {
                            file.createNewFile()
                        }
                        fileOutPutStream = FileOutputStream(file)
                    }
                    isNew = false
                    continue
                }
            } else {
                if (tLine == endBoundary || tLine==boundary) {
                    //添加数据
                    if (isFile) {
                        try {
                            fileOutPutStream?.close()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        formBodys.add(FormBody.FileItem(
                            contentDisposition,
                            contentType,
                            name,
                            fileName,
                            file!!
                        ))
                    } else {
                        formBodys.add(
                            FormBody.StringItem(
                                contentDisposition,
                                name,
                                readContent.toString()
                            )
                        )
                    }

                    contentDisposition = ""
                    mContentType = ""
                    name = ""
                    fileName = ""
                    readContent = StringBuilder("")
                    isNew = true
                    isFile = false
                    file=null
                    fileOutPutStream = null
                }else{
                    val rTline = tLine.substring(0,tLine.length-2)
                    val wl = Math.min(rTline.length,contentLength.toInt()-readLength.toInt())
                    if (isFile) {
                        fileOutPutStream?.write(rTline.toByteArray(),0,wl)
                        fileOutPutStream?.flush()
                    } else {
                        readContent.append(rTline,0,wl)
                    }
                }
            }
            readLength += tLine.length

            "readLength:${readLength},tLine:$tLine".logError()
        }
        "contentLength:${contentLength},readLength:$readLength".logError()
        try {
            fileOutPutStream?.close()
        }catch (e:Exception){
            e.printStackTrace()
        }
        fileOutPutStream = null

        formBodys.forEach {
            "formBody:$it".logError()
        }
    }


    fun getMultipartData() = multipartData
}


sealed class MultipartData {
    data class XWWForm(val query: String) : MultipartData() {
        private val map = mutableMapOf<String, String>()

        init {
            query.split("&").forEach {
                if (it.contains("=")) {
                    map[it.substringBefore("=").trim()] = it.substringAfter("=").trim()
                }
            }
        }

        fun getQueryMap() = map

        fun getValue(key: String) = map[key]
    }

    data class DataForms(val fileItems: List<FormBody>) : MultipartData()

    data class DataForm(val fileItem: FormBody) : MultipartData()
}

sealed class FormBody {
    data class FileItem(
        private val contentDisposition: String,
        private val contentType: String,
        private val name: String,
        private val fileName: String,
        private val file: File
    ) : FormBody()

    data class StringItem(
        private val contentDisposition: String,
        private val name: String,
        private val content: String
    ) : FormBody()
}

