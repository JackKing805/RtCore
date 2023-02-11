package com.jerry.rt.core.http.pojo

import com.jerry.rt.core.RtContext
import com.jerry.rt.core.http.protocol.*
import com.jerry.rt.core.http.response.impl.ByteResponseWriter
import com.jerry.rt.extensions.logError
import com.jerry.rt.utils.RtUtils
import com.jerry.rt.utils.URLEncodeUtil
import java.io.*
import java.nio.charset.Charset
import java.util.*
import kotlin.jvm.Throws

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
    protected val byteResponseWriter = ByteResponseWriter(output)
    protected var charset = Charsets.UTF_8

    protected val header = mutableMapOf<String, String>()
    protected var statusCode = 200

    protected var cookies = mutableListOf<Cookie>()

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

    fun addCookie(cookie: Cookie){
        cookies.add(cookie)
    }

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

    fun setContentLength(length: Int) {
        header[RtHeader.CONTENT_LENGTH.content] = length.toString()
    }

    fun sendHeader() {
        write("")
    }

    fun removeHeader(name:String){
        header.remove(name)
    }

    @Throws(IOException::class)
    fun write(body: ByteArray, contentType: String, length: Int = body.size) {
        send(start = {
            setContentType(contentType)
            setContentLength(length)
        },{
            byteResponseWriter.writeBody(body)
        }) {

        }
    }

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
    fun write(body: String, contentType: String, length: Int = body.length) {
        write(body.toByteArray(), contentType, length)
    }

    @Throws(IOException::class)
    fun write(body: String, contentType: String) {
        write(body.toByteArray(), contentType, body.length)
    }

    @Throws(IOException::class)
    fun write(body: String) {
        val contentType = header[RtHeader.CONTENT_TYPE.content]?:throw NullPointerException("please set Content-Type")
        write(body.toByteArray(), contentType, body.length)
    }

    //https://blog.csdn.net/qq_26046771/article/details/103321223 大文件传输原理
    //https://blog.csdn.net/lantian_123/article/details/101517817?utm_medium=distribute.pc_relevant.none-task-blog-2~default~baidujs_baidulandingword~default-1-101517817-blog-110749214.pc_relevant_3mothn_strategy_and_data_recovery&spm=1001.2101.3001.4242.2&utm_relevant_index=4 分块传输原理
    @Throws(IOException::class)
    open fun writeFile(file: File, contentType: String? = null) {
        val content_type = contentType?: RtMimeTypeMatcher.matchContentType(file.absolutePath)
        setHeader(RtHeader.CONTENT_TYPE.content,content_type)


        val acceptRanges = getPackage().getHeader().getAcceptRanges()
        if (acceptRanges==null){
            val fileLength = file.length().toInt()
            if (fileLength > 1024L) {
                if(getPackage().protocol==RtVersion.HTTP_1_1){
                    //分块传输，只有http1.1支持
                    val inputStream = FileInputStream(file)
                    send({
                        removeHeader(RtHeader.CONTENT_LENGTH.content)
                        setHeader("Transfer-Encoding","chunked")
                    },{
                        val buffer = ByteArray(1024)
                        var len = 0
                        while (inputStream.read(buffer).also { len = it }!=-1 ){
                            it.writeBody(buffer,0,len)
                        }
                        it.writeLine("0")
                    }){
                        inputStream.close()
                    }
                    return
                }

            }
        }

        realWriteFile(file,acceptRanges,content_type)
    }

    private fun realWriteFile(file: File,acceptRanges:Array<Long>?,contentType: String){
        var rangeStart = 0L
        var rangeEnd = 0L


        val fileLength = file.length().toInt()
        if (acceptRanges == null) {
            rangeEnd = fileLength - 1L
        } else {
            rangeStart = acceptRanges.first()
            rangeEnd = acceptRanges.last()
        }


        if (rangeEnd == 0L) {
            rangeEnd = fileLength - 1L
        }

        if (rangeStart>=fileLength || rangeEnd>=fileLength){
            setResponseStatusCode(RtCode._416.code)
            sendHeader()
            return
        }

        val contentLength = rangeEnd - rangeStart + 1L
        val fileInput = FileInputStream(file)
        fileInput.skip(rangeStart)
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
        },{
            val buffer = ByteArray(1024)
            var len = 0
            var totalLen = 0
            while (fileInput.read(buffer).also { len = it }!=-1 && totalLen<contentLength){
                val wl = len.coerceAtMost(contentLength.toInt() - totalLen)
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
        val byteArray = ByteArray(length)
        inputStream.read(byteArray)
        write(byteArray, contentType, length)
    }

    fun getPrintWriter() = PrintWriter(output)




    /**
     * 发送，重要方法
     */
    private fun send(start:()->Unit,body:(ByteResponseWriter)->Unit,complete:()->Unit){
        try {
            start()
            byteResponseWriter.writeFirstLine(protocolPackage.protocol.content, statusCode, RtCode.match(statusCode).message)
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