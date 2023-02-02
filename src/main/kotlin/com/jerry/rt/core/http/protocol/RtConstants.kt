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
    RT_1_0("RT/1.0"),
    HTTP_1_0("HTTP/1.0"),
    HTTP_1_1("HTTP/1.1"),
    HTTP_2_0("HTTP/2.0");

    companion object{
        fun toRtVersion(version:String):RtVersion{
            return when(version.uppercase()){
                RT_1_0.content->RT_1_0
                HTTP_1_0.content->HTTP_1_0
                HTTP_1_1.content->HTTP_1_1
                HTTP_2_0.content->HTTP_2_0
                else->{
                    throw IllegalArgumentException("not support protocol")
                }
            }
        }

        fun getPrefix(version: RtVersion):String{
            return when(version){
                RT_1_0 -> "rt"
                HTTP_1_0 ,
                HTTP_1_1 ,
                HTTP_2_0 -> "http"
            }
        }
    }
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




enum class RtMimeType( val extension: String,  val explain: String,  val mimeType: String) {
    AAC("acc", "AAC音频", "audio/aac"),
    ABW("abw", "AbiWord文件", "application/x-abiword"),
    ARC("arc", "存档文件", "application/x-freearc"),
    AVI("avi", "音频视频交错格式", "video/x-msvideo"),
    AZW("azw", "亚马逊Kindle电子书格式", "application/vnd.amazon.ebook"),
    BIN("bin", "任何类型的二进制数据", "application/octet-stream"),
    BMP("bmp", "Windows OS / 2位图图形", "image/bmp"),
    BZ("bz", "BZip存档", "application/x-bzip"),
    BZ2("bz2", "BZip2存档", "application/x-bzip2"),
    CSH("csh", "C-Shell脚本", "application/x-csh"),
    CSS("css", "级联样式表（CSS）", "text/css"),
    CSV("csv", "逗号分隔值（CSV）", "text/csv"),
    DOC("doc", "微软Word文件", "application/msword"),
    DOCX("docx", "Microsoft Word（OpenXML）", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    EOT("eot", "MS Embedded OpenType字体", "application/vnd.ms-fontobject"),
    EPUB("epub", "电子出版物（EPUB）", "application/epub+zip"),
    GZ("gz", "GZip压缩档案", "application/gzip"),
    GIF("gif", "图形交换格式（GIF）", "image/gif"),
    HTM(
        "htm", "超文本标记语言（HTML）", "text/html"
    ),
    HTML("html", "超文本标记语言（HTML）", "text/html"),
    ICO("ico", "图标格式", "image/vnd.microsoft.icon"),
    ICS(
        "ics", "iCalendar格式", "text/calendar"
    ),
    JAR("jar", "Java存档", "application/java-archive"),
    JPEG(
        "jpeg", "JPEG图像", "image/jpeg"
    ),
    JPG("jpg", "JPEG图像", "image/jpeg"),
    JS("js", "JavaScript", "text/javascript"),
    JSON(
        "json", "JSON格式", "application/json"
    ),
    JSONLD("jsonld", "JSON-LD格式", "application/ld+json"),
    MID(
        "mid", "乐器数字接口（MIDI）", "audio/midi"
    ),
    MIDI("midi", "乐器数字接口（MIDI）", "audio/midi"),
    MJS(
        "mjs", "JavaScript模块", "text/javascript"
    ),
    MP3("mp3", "MP3音频", "audio/mpeg"),
    MPEG("mpeg", "MPEG视频", "video/mpeg"),
    MPKG(
        "mpkg", "苹果安装程序包", "application/vnd.apple.installer+xml"
    ),
    ODP("odp", "OpenDocument演示文稿文档", "application/vnd.oasis.opendocument.presentation"),
    ODS(
        "ods", "OpenDocument电子表格文档", "application/vnd.oasis.opendocument.spreadsheet"
    ),
    ODT("odt", "OpenDocument文字文件", "application/vnd.oasis.opendocument.text"),
    OGA(
        "oga", "OGG音讯", "audio/ogg"
    ),
    OGV("ogv", "OGG视频", "video/ogg"),
    OGX("ogx", "OGG", "application/ogg"),
    OPUS(
        "opus", "OPUS音频", "audio/opus"
    ),
    OTF("otf", "otf字体", "font/otf"),
    PNG("png", "便携式网络图形", "image/png"),
    PDF(
        "pdf", "Adobe 可移植文档格式（PDF）", "application/pdf"
    ),
    PHP("php", "php", "application/x-httpd-php"),
    PPT(
        "ppt", "Microsoft PowerPoint", "application/vnd.ms-powerpoint"
    ),
    PPTX(
        "pptx",
        "Microsoft PowerPoint（OpenXML）",
        "application/vnd.openxmlformats-officedocument.presentationml.presentation"
    ),
    RAR("rar", "RAR档案", "application/vnd.rar"),
    RTF("rtf", "富文本格式", "application/rtf"),
    SH(
        "sh", "Bourne Shell脚本", "application/x-sh"
    ),
    SVG("svg", "可缩放矢量图形（SVG）", "image/svg+xml"),
    SWF(
        "swf", "小型Web格式（SWF）或Adobe Flash文档", "application/x-shockwave-flash"
    ),
    TAR("tar", "磁带存档（TAR）", "application/x-tar"),
    TIF(
        "tif", "标记图像文件格式（TIFF）", "image/tiff"
    ),
    TIFF("tiff", "标记图像文件格式（TIFF）", "image/tiff"),
    TS("ts", "MPEG传输流", "video/mp2t"),
    TTF(
        "ttf", "ttf字体", "font/ttf"
    ),
    TXT("txt", "文本（通常为ASCII或ISO 8859- n", "text/plain"), VSD(
        "vsd", "微软Visio", "application/vnd.visio"
    ),
    WAV("wav", "波形音频格式", "audio/wav"),
    WEBA("weba", "WEBM音频", "audio/webm"),
    WEBM(
        "webm", "WEBM视频", "video/webm"
    ),
    WEBP("webp", "WEBP图像", "image/webp"),
    WOFF(
        "woff", "Web开放字体格式（WOFF）", "font/woff"
    ),
    WOFF2("woff2", "Web开放字体格式（WOFF）", "font/woff2"),
    XHTML(
        "xhtml", "XHTML", "application/xhtml+xml"
    ),
    XLS("xls", "微软Excel", "application/vnd.ms-excel"),
    XLSX(
        "xlsx",
        "微软Excel（OpenXML）",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    ),
    XML("xml", "XML", "application/xml"),
    XUL("xul", "XUL", "application/vnd.mozilla.xul+xml"),
    ZIP("zip", "ZIP", "application/zip"),
    MIME_3GP("3gp", "3GPP audio/video container", "video/3gpp"),
    MIME_3GP_WITHOUT_VIDEO(
        "3gp", "3GPP audio/video container doesn't contain video", "audio/3gpp2"
    ),
    MIME_3G2("3g2", "3GPP2 audio/video container", "video/3gpp2"),
    MIME_3G2_WITHOUT_VIDEO(
        "3g2", "3GPP2 audio/video container  doesn't contain video", "audio/3gpp2"
    ),
    MIME_7Z("7z", "7-zip存档", "application/x-7z-compressed"),

    STREAM("","流","application/octet-stream");

    companion object{
        fun matchContentType(str: String):RtMimeType{
            RtMimeType.values().forEach {
                if (it.extension.isNotEmpty() && str.endsWith(it.extension)){
                    return it
                }
            }
            return STREAM
        }
    }
}