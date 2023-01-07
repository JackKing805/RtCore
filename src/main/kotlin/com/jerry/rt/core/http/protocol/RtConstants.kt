package com.jerry.rt.core.http.protocol

/**
 * @className: RtConstants
 * @description: rt协议内容
 * @author: Jerry
 * @date: 2023/1/1:13:02
 **/
enum class RtContentType(val content:String) {
    RT_HEARTBEAT("rt/heartbeat"),
    FORM_URLENCODED("application/x-www-form-urlencoded"),
    MULTIPART("multipart/form-data"),
    JSON("application/json"),
    XML("application/xml"),
    TEXT_PLAIN("text/plain"),
    TEXT_XML("text/xml"),
    TEXT_HTML("text/html");
}

enum class RtVersion(val content: String){
    RT_1_0("rt/1.0")
}

enum class RtMethod(val content: String){
    RT("rt")
}


/**
 * @className: RtCode
 * @description: http协议状态码
 * @author: Jerry
 * @date: 2023/1/8:1:52
 **/
enum class RtCode(val code:Int,val message:String){
    _100(100,"Continue"),
    _101(101,"Switching Protocols"),
    _200(200,"OK"),
    _201(201,"Created"),
    _202(202,"Accepted"),
    _203(203,"Non-Authoritative Information"),
    _204(204,"No Content"),
    _205(205,"Reset Content"),
    _206(206,"Partial Content"),
    _300(300,"Multiple Choices"),
    _301(301,"Moved Permanently"),
    _302(302,"Found"),
    _303(303,"See Other"),
    _304(304,"Not Modified"),
    _305(305,"Use Proxy"),
    _307(307,"Temporary Redirect"),
    _400(400,"Bad Request"),
    _401(401,"Unauthorized"),
    _403(403,"Forbidden"),
    _404(404,"Not Found"),
    _405(405,"Method Not Allowed"),
    _406(406,"Not Acceptable"),
    _407(407,"Proxy Authentication Required"),
    _408(408,"Request Timeout"),
    _409(409,"Conflict"),
    _410(410,"Gone"),
    _411(411,"Length Required"),
    _412(412,"Precondition Failed"),
    _413(413,"Request Entity Too Large"),
    _414(414,"Request URI Too Long"),
    _415(415,"Unsupported Media Type"),
    _416(416,"Requested Range Not Satisfiable"),
    _417(417,"Expectation Failed"),
    _500(500,"Internal Server Error"),
    _501(501,"Not Implemented"),
    _502(502,"Bad Gateway"),
    _503(503,"Service Unavailable"),
    _504(504,"Gateway Timeout"),
    _505(505,"HTTP Version Not Supported");

    companion object{
        fun match(code:Int):RtCode{
            return when(code){
                100->_100
                101->_101
                200->_200
                201->_201
                202->_202
                203->_203
                204->_204
                205->_205
                206->_206
                300->_300
                301->_301
                302->_302
                303->_303
                304->_304
                305->_305
                307->_307
                400->_400
                401->_401
                403->_403
                404->_404
                405->_405
                406->_406
                407->_407
                408->_408
                409->_409
                410->_410
                411->_411
                412->_412
                413->_413
                414->_414
                415->_415
                416->_416
                417->_417
                500->_500
                501->_501
                502->_502
                503->_503
                504->_504
                505->_505
                else->throw RuntimeException("not support response code")
            }
        }
    }
}

/**
 * @className: RtHeader
 * @description: 请求头响应头
 * @author: Jerry
 * @date: 2023/1/8:2:20
 **/
enum class RtHeader(val content:String) {
    AUTHORIZATION("Authorization"),
    PROXY_AUTHORIZATION("Proxy-Authorization"),
    DATE("Date"),
    CONNECTION("Connection"),
    MIME_VERSION("MIME-Version"),
    TRAILER("Trailer"),
    TRANSFER_ENCODING("Transfer-Encoding"),
    UPGRADE("Upgrade"),
    VIA("Via"),
    CACHE_CONTROL("Cache-Control"),
    PRAGMA("Pragma"),
    CONTENT_TYPE("Content-Type"),
    HOST("Host"),
    REFERER("Referer"),
    ORIGIN("Origin"),
    USER_AGENT("User-Agent"),
    ACCEPT("Accept"),
    ACCEPT_LANGUAGE("Accept-Language"),
    ACCEPT_ENCODING("Accept-Encoding"),
    ACCEPT_CHARSET("Accept-Charset"),
    COOKIE("Cookie"),
    CONTENT_LENGTH("Content-Length"),
    WWW_AUTHENTICATE("WWW-Authenticate"),
    SET_COOKIE("Set-Cookie"),
    CONTENT_ENCODING("Content-Encoding"),
    CONTENT_DISPOSITION("Content-Disposition"),
    ETAG("ETag"),
    LOCATION("Location");
}