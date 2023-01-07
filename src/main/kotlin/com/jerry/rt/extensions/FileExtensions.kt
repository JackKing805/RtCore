package com.jerry.rt.extensions

import java.io.IOException
import java.net.URLConnection
import java.nio.file.Files
import java.nio.file.Paths

/**
 * @className: FileExtensions
 * @description: 文件拓展工具类
 * @author: Jerry
 * @date: 2023/1/8:2:16
 **/

internal fun String.getMimeType():String?{
    var contentType = URLConnection.getFileNameMap().getContentTypeFor(this)
    if (null == contentType) {
        if (this.endsWith(".css")) {
            contentType = "text/css"
        } else if (this.endsWith(".js")) {
            contentType = "application/x-javascript"
        }
    }

    if (null == contentType) {
        contentType = return try {
            Files.probeContentType(Paths.get(this))
        } catch (e: IOException) {
            throw e
        }
    }

    return contentType
}