package com.jerry.rt.core.http.request.exceptions

/**
 * @className: LimitLengthException
 * @description: 读取的时候已读取的长度大于了contentLength
 * @author: Jerry
 * @date: 2023/2/12:13:16
 **/
class LimitLengthException:IllegalArgumentException("len is over maxSize,please provider current len to read byteArray")