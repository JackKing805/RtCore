package com.jerry.rt.utils

import java.nio.charset.Charset

/**
 * @className: URLEncodeUtil
 * @description:
 * @author: Jerry
 * @date: 2023/1/8:12:24
 */
object URLEncodeUtil {
    fun encode(url: String, charset: Charset): String {
        return URLEncoder.DEFAULT.encode(url, charset)
    }
}