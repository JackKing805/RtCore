package com.jerry.rt.core.http.request.model

import com.jerry.rt.core.RtContext
import com.jerry.rt.core.http.pojo.ProtocolPackage
import com.jerry.rt.jva.utils.MultipartRequestInputStream
import java.nio.charset.Charset

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
//todo 当前读取中文会乱码
class MultipartFormData(
    context: RtContext,
    private val protocolPackage: ProtocolPackage,
    socketBody: SocketBody,
    charset: Charset
) {
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
    private val parameters = mutableMapOf<String,String>()
    private val files = mutableMapOf<String,MultipartFile>()

    init {
        val rtFileConfig = context.getRtConfig().rtFileConfig
        val input = MultipartRequestInputStream(socketBody.getInputStream())
        input.readBoundary()

        //todo 读取完所有文件之后会卡在这里
        while (true) {
            val header: MultipartFileHeader = input.readDataHeader(charset) ?: break
            if (header.isFile()) {
                // 文件类型的表单项
                val fileName = header.getFileName()!!
                if (fileName.isNotEmpty() && header.getContentType()!!.contains("application/x-macbinary")) {
                    input.skipBytes(128)
                }
                val newFile = MultipartFile(rtFileConfig,header)
                if (newFile.processStream(input)) {
                    files[header.getFormFieldName()!!] = newFile
                }
            } else {
                // 标准表单项
                parameters[header.getFormFieldName()!!] = input.readString(charset)
            }
            input.skipBytes(1)
            input.mark(1)

            // read byte, but may be end of stream
            val nextByte: Int = input.read()
            if (nextByte == -1 || nextByte == '-'.code) {
                input.reset()
                break
            }
            input.reset()
        }
    }

    fun getParameters() = parameters

    fun getFiles() = files
}

