package com.jerry.rt.extensions


/**
 * @className: StringExtensions
 * @description: String 拓展类
 * @author: Jerry
 * @date: 2023/1/8:2:29
 **/

fun getElse(value: String? = null, defaultValue: String): String {
    return value ?: defaultValue
}
