package com.jerry.rt.core.http.pojo

import com.jerry.rt.core.RtContext
import com.jerry.rt.core.http.protocol.*
import com.jerry.rt.core.http.response.impl.ByteResponseWriter
import com.jerry.rt.core.http.converter.DataConverter
import com.jerry.rt.utils.RtUtils
import com.jerry.rt.utils.URLEncodeUtil
import java.io.*
import java.nio.charset.Charset
import java.util.*

/**
 * @className: Response
 * @description: 返回
 * @author: Jerry
 * @date: 2023/1/6:19:47
 **/
class Response(
    private val rtContext: RtContext,
    private val output: OutputStream,
    private val protocolPackage: ProtocolPackage
){
    private val byteResponseWriter = ByteResponseWriter(output)
    private var charset = protocolPackage.getCharset()

    private val header = mutableMapOf<String, String>()
    private var statusCode = 200

    private var cookies = mutableListOf<Cookie>()

    fun getPackage() = protocolPackage


    init {
        reset()
    }


    fun getContext() = rtContext

    fun setResponseCharset(charset: Charset) {
        this.charset = charset
    }

    fun getResponseCharset() = charset

    fun setHeader(key: String, value: String) {
        header[key] = value
    }

    fun getHeader(key: String,default:String?=null):String?{
        return header[key]?:default
    }

    fun getHeaders() = header

    fun addCookie(cookie: Cookie){
        cookies.add(cookie)
    }

    fun getCookies() = cookies

    fun setHeaders(headers: MutableMap<String, String>) {
        header.putAll(headers.filter {
            it.key != "Content-Type" &&
                    it.key != "Content-Length"
        })
    }

    fun setContentType(contentType: String) {
        val result = if (contentType.startsWith("text")) {
            if (contentType.contains(";")) {
                contentType
            } else {
                contentType + ";" + charset.name()
            }
        } else {
            contentType
        }
        header[RtHeader.CONTENT_TYPE.content] = result
    }

    fun setResponseStatusCode(code: Int) {
        statusCode = code
    }

    fun getResponseStatusCode() = statusCode

    fun setContentLength(length: Int) {
        header[RtHeader.CONTENT_LENGTH.content] = length.toString()
    }

    fun sendHeader() {
        write("")
    }

    fun removeHeader(name:String){
        header.remove(name)
    }

    fun getByteWriter() = byteResponseWriter

    fun reset(){
        header.clear()
        cookies.clear()
        setHeader(RtHeader.DATE.content, RtUtils.dateToFormat(Date(),"EEE, dd MMM yyyy HH:mm:ss 'GMT'"))
        setHeader("Server",rtContext.getRtConfig().serverVersionDetails)
        setResponseStatusCode(200)
        val session = protocolPackage.getSession()
        if (session.isNew()){
            addCookie(Cookie(getContext().getRtConfig().rtSessionConfig.sessionKey,session.getId()))
        }
    }

    @Throws(IOException::class)
    fun write(body: ByteArray, contentType: String) {
        val realData = DataConverter.converterToAcceptEncoding(rtContext,protocolPackage.getHeader().getAcceptEncodings(),body)

        send(start = {
            setContentType(contentType)
            setContentLength(realData.data.size)
            setHeader("Content-Encoding",realData.encoding)
        },{
            byteResponseWriter.writeBody(realData.data)
        }) {

        }
    }

    @Throws(IOException::class)
    fun write(body: ByteArray) {
        if (header[RtHeader.CONTENT_TYPE.content] ==null){
            throw NullPointerException("please provider contentType")
        }
        write(body,header[RtHeader.CONTENT_TYPE.content]!!)
    }

    @Throws(IOException::class)
    fun write(body: String, contentType: String) {
        write(body.toByteArray(), contentType)
    }

    @Throws(IOException::class)
    fun write(body: String) {
        val contentType = header[RtHeader.CONTENT_TYPE.content]?:throw NullPointerException("please set Content-Type")
        write(body.toByteArray(), contentType)
    }

    //https://blog.csdn.net/qq_26046771/article/details/103321223 大文件传输原理
    //https://blog.csdn.net/lantian_123/article/details/101517817?utm_medium=distribute.pc_relevant.none-task-blog-2~default~baidujs_baidulandingword~default-1-101517817-blog-110749214.pc_relevant_3mothn_strategy_and_data_recovery&spm=1001.2101.3001.4242.2&utm_relevant_index=4 分块传输原理
    @Throws(IOException::class)
    open fun writeFile(file: File, contentType: String? = null) {
        val content_type =contentType?:header[RtHeader.CONTENT_TYPE.content]?: RtMimeTypeMatcher.matchContentType(file.absolutePath)
        setHeader(RtHeader.CONTENT_TYPE.content,content_type)


        val acceptRanges = getPackage().getHeader().getAcceptRanges()
        if (acceptRanges==null){
            val fileLength = file.length().toInt()
            if (fileLength > 1024L) {
                if(getPackage().getProtocol()==RtVersion.HTTP_1_1){
                    //分块传输，只有http1.1支持
                    val inputStream = FileInputStream(file)
                    send({
                        removeHeader(RtHeader.CONTENT_LENGTH.content)
                        setHeader("Transfer-Encoding","chunked")
//                        setHeader("Content-Encoding","identity")//identity 表示未编码的数据
                        setHeader(RtHeader.CONNECTION.content,"keep-alive")
                    },{
                        val buffer = ByteArray(1024)
                        var len = 0
                        while (inputStream.read(buffer).also { len = it }!=-1 ){
                            it.writeDividingLine()
                            it.writeLine(Integer.toHexString(len))
                            it.writeBody(buffer,0,len)
                            it.writeLine("\r\n")
                        }
                        it.writeLine("0")
                        it.writeLine("\r\n")
                    }){
                        inputStream.close()
                    }
                    return
                }

            }
        }

        realWriteFile(file,acceptRanges,content_type)
    }

    private fun realWriteFile(file: File,acceptRanges:Array<Int>?,contentType: String){
        var rangeStart = 0
        var rangeEnd = 0


        val fileLength = file.length().toInt()
        if (acceptRanges == null) {
            rangeEnd = fileLength - 1
        } else {
            rangeStart = acceptRanges.first()
            rangeEnd = acceptRanges.last()
        }


        if (rangeEnd == 0) {
            rangeEnd = fileLength - 1
        }else if (rangeEnd<0){
            rangeStart = fileLength + rangeEnd-1
            rangeEnd = fileLength - 1
        }

        if (rangeStart>=fileLength || rangeEnd>=fileLength){
            setResponseStatusCode(RtCode._416.code)
            sendHeader()
            return
        }

        val contentLength = rangeEnd - rangeStart + 1
        val fileInput = FileInputStream(file)
        fileInput.skip(rangeStart.toLong())
        send({
            setResponseStatusCode(RtCode._206.code)
            if (!contentType.startsWith("text/")) {
                setHeader(
                    RtHeader.CONTENT_DISPOSITION.content,
                    "attachment;filename=${URLEncodeUtil.encode(file.name, charset)}"
                )
            }
            setHeader(RtHeader.CONTENT_LENGTH.content,contentLength.toString())
            setHeader("Accept-Ranges","bytes")
            setHeader("Content-Range","bytes $rangeStart-$rangeEnd/$fileLength")
            setHeader("Content-Encoding","identity")//identity 表示未编码的数据
        },{
            val buffer = ByteArray(1024)
            var len = 0
            var totalLen = 0
            while (fileInput.read(buffer).also { len = it }!=-1 && totalLen<contentLength){
                val wl = len.coerceAtMost(contentLength - totalLen)
                totalLen+=wl
                it.writeBody(buffer,0,wl)
            }
        }){
            fileInput.close()
        }
    }




    @Throws(IOException::class)
    fun writeFile(path: String, contentType: String? = null) {
        writeFile(File(path), contentType)
    }

    @Throws(IOException::class)
    fun writeInputStream(inputStream: InputStream, contentType: String, length: Int) {
        val rContentType = header[RtHeader.CONTENT_TYPE.content]?:contentType
        val l = header[RtHeader.CONTENT_LENGTH.content]
        val rContentLength =  if (!l.isNullOrEmpty()){
            l.toInt()
        }else{
            length
        }
        val byteArray = ByteArray(rContentLength)
        inputStream.read(byteArray)
        write(byteArray, rContentType)
    }

    fun getPrintWriter() = PrintWriter(output)

    /**
     * 发送，重要方法
     */
    @Synchronized
    private fun send(start:()->Unit,body:(ByteResponseWriter)->Unit,complete:()->Unit){
        try {
            start()
            byteResponseWriter.writeFirstLine(protocolPackage.getProtocol().content, statusCode, RtCode.match(statusCode).message)
            header.entries.forEach {
                byteResponseWriter.writeHeader(it.key, it.value)
            }
            cookies.forEach {
                byteResponseWriter.writeHeader(RtHeader.SET_COOKIE.content, it.toCookieString(protocolPackage.getRequestURI()))
            }
            body(byteResponseWriter)
            byteResponseWriter.endWrite()
            reset()
        }catch (e:Exception){
            e.printStackTrace()
        }finally {
            complete()
        }
    }
}
