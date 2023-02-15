package com.jerry.rt.core.http.request.model

import com.jerry.rt.jva.utils.FileUtil
import com.jerry.rt.jva.utils.StrUtil

/**
 * @className: MultipartFile
 * @author: Jerry
 * @date: 2023/2/13:21:47
 **/
class MultipartFileHeader(dataHeader:String) {
    private var formFieldName: String? = null

    private var formFileName: String? = null
    private var path: String? = null
    private var fileName: String? = null

    private var isFile = false
    private var contentType: String? = null
    private var mimeType: String? = null
    private var mimeSubtype: String? = null
    private var contentDisposition: String? = null


    init {
        isFile = dataHeader.indexOf("filename") > 0
        formFieldName = getDataFieldValue(dataHeader, "name")
        if (isFile) {
            formFileName = getDataFieldValue(dataHeader, "filename")
            if (formFileName != null) {
                if (formFileName!!.isEmpty()) {
                    path = StrUtil.EMPTY
                    fileName = StrUtil.EMPTY
                }
                val ls = FileUtil.lastIndexOfSeparator(formFileName)
                if (ls == -1) {
                    path = StrUtil.EMPTY
                    fileName = formFileName
                } else {
                    path = formFileName!!.substring(0, ls)
                    fileName = formFileName!!.substring(ls)
                }
                if (fileName!!.isNotEmpty()) {
                    contentType = getContentType(dataHeader)
                    mimeType = getMimeType(contentType)
                    mimeSubtype = getMimeSubtype(contentType)
                    contentDisposition = getContentDisposition(dataHeader)
                }
            }
        }
    }

    // ---------------------------------------------------------------- public interface

    // ---------------------------------------------------------------- public interface
    /**
     * Returns `true` if uploaded data are correctly marked as a file.<br></br>
     * This is true if header contains string 'filename'.
     *
     * @return 是否为文件
     */
    fun isFile(): Boolean {
        return isFile
    }

    /**
     * 返回表单字段名
     *
     * @return 表单字段名
     */
    fun getFormFieldName(): String {
        return formFieldName?:""
    }

    /**
     * 返回表单中的文件名，来自客户端传入
     *
     * @return 表单文件名
     */
    fun getFormFileName(): String {
        return formFileName?:""
    }

    /**
     * 获取文件名，不包括路径
     *
     * @return 文件名
     */
    fun getFileName(): String {
        return fileName?:""
    }

    /**
     * Returns uploaded content type. It is usually in the following form:<br></br>
     * mime_type/mime_subtype.
     *
     * @return content type
     * @see .getMimeType
     * @see .getMimeSubtype
     */
    fun getContentType(): String {
        return contentType?:""
    }

    /**
     * Returns file types MIME.
     *
     * @return types MIME
     */
    fun getMimeType(): String {
        return mimeType?:""
    }

    /**
     * Returns file sub type MIME.
     *
     * @return sub type MIME
     */
    fun getMimeSubtype(): String? {
        return mimeSubtype
    }

    /**
     * Returns content disposition. Usually it is 'form-data'.
     *
     * @return content disposition
     */
    fun getContentDisposition(): String? {
        return contentDisposition
    }


    /**
     * 获得头信息字符串字符串中指定的值
     *
     * @param dataHeader 头信息
     * @param fieldName  字段名
     * @return 字段值
     */
    private fun getDataFieldValue(dataHeader: String, fieldName: String): String? {
        var value: String? = null
        val token = StrUtil.format("{}=\"", fieldName)
        val pos = dataHeader.indexOf(token)
        if (pos > 0) {
            val start = pos + token.length
            val end = dataHeader.indexOf('"', start)
            if (start > 0 && end > 0) {
                value = dataHeader.substring(start, end)
            }
        }
        return value
    }

    /**
     * 头信息中获得content type
     *
     * @param dataHeader data header string
     * @return content type or an empty string if no content type defined
     */
    private fun getContentType(dataHeader: String): String {
        val token = "Content-Type:"
        var start = dataHeader.indexOf(token)
        if (start == -1) {
            return StrUtil.EMPTY
        }
        start += token.length
        return dataHeader.substring(start)
    }

    private fun getContentDisposition(dataHeader: String): String {
        val start = dataHeader.indexOf(':') + 1
        val end = dataHeader.indexOf(';')
        return dataHeader.substring(start, end)
    }

    private fun getMimeType(ContentType: String?): String {
        val pos = ContentType!!.indexOf('/')
        return if (pos == -1) {
            ContentType
        } else ContentType.substring(1, pos)
    }

    private fun getMimeSubtype(ContentType: String?): String {
        var start = ContentType!!.indexOf('/')
        if (start == -1) {
            return ContentType
        }
        start++
        return ContentType.substring(start)
    }
}