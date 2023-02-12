package com.jerry.rt.core.http.request.exceptions

/**
 * @className: NoLengthReadException
 * @description: 本次的body已经被读取完毕
 * @author: Jerry
 * @date: 2023/2/12:13:20
 **/
class NoLengthReadException:IllegalStateException("ContentBody is already read all,no more read")