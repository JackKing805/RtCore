package com.jerry.rt.extensions

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.nio.charset.Charset

/**
 * @className: StringExtensions
 * @description: String 拓展类
 * @author: Jerry
 * @date: 2023/1/8:2:29
 **/

fun getElse(value: String? = null, defaultValue: String): String {
    return value ?: defaultValue
}
