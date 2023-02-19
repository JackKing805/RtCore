package com.jerry.rt.core.http.pojo.utils

import com.jerry.rt.core.RtContext
import com.jerry.rt.core.http.pojo.ProtocolPackage

/**
 * @className: PojoUtil
 * @description: PojoUtil
 * @author: Jerry
 * @date: 2023/2/19:19:38
 **/
internal object PojoUtil {
    private fun getSamePath(url1: String, url2: String): String {
        val split = url1.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val split1 = url2.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val s1 = StringBuilder()
        val min = split1.size.coerceAtMost(split.size)
        for (i in 0 until min) {
            if (split[i] != split1[i]) {
                break
            } else {
                s1.append(split[i]).append("/")
            }
        }
        return s1.toString()
    }

    fun getResourcesName(protocolPackage: ProtocolPackage):String{
        val referer = protocolPackage.getHeader().getReferer()
        val query = protocolPackage.getRequestURI().query
        val fullPath = protocolPackage.getRequestAbsolutePath()
        val path = protocolPackage.getRelativePath()
        val root = protocolPackage.getRootAbsolutePath()
        var resourcesPath = if (referer.isEmpty() || referer==root){
            path
        }else{
            val same = getSamePath(fullPath,referer)
            fullPath.replace(same,"")
        }
        if (resourcesPath.startsWith("/")){
            resourcesPath = resourcesPath.substring(1)
        }

        if (query!=null){
            resourcesPath = resourcesPath.replace("?$query","")
        }
        return resourcesPath
    }



    private val resourcesSuffix = listOf(
        //文本
        ".txt",
        ".md",
        ".doc",
        ".docx",
        ".pdf",
        ".xls",
        ".xlsx",
        ".ppt",
        ".pptx",

        //图像
        ".jpg",
        ".jpeg",
        ".png",
        ".bmp",
        ".gif",
        ".webp",
        ".ico",

        //音频
        ".mp3",
        ".flac",
        ".aac",
        ".ogg",
        ".m4a",

        //视频
        ".mp4",
        ".avi",
        ".mkv",
        ".mov",
        ".flv",
        ".rmvb",
        ".wmv",
        ".m4v",
        ".3gp",
        ".f4v",

        //压缩
        ".zip",
        ".rar",
        ".7z",
        ".gz",
        ".tar",
        ".iso",
        ".dmg",
        ".apk",
        ".apks",
        ".aab",

        //程序
        ".exe",
        ".dll",
        ".jar",
        ".class",
        ".py",
        ".java",
        ".kt",
        ".swift",
        ".c",
        ".cpp",
        ".h",
        ".hpp",

        //网页
        ".html",
        ".htm",
        ".css",
        ".js",
        ".json",
        ".xml",
        ".jsp",
        ".vue",


        //数据库
        ".db",
        ".sqlite",
        ".mysql",
        ".postgresql",
        ".redis",
        ".mongodb",
        ".csv",
        ".xls",
        ".xlsx",
    )

    //判断是不是资源请求
    fun isResources(context: RtContext,resourceName:String):Boolean{
        val lowercase = resourceName.lowercase()
        resourcesSuffix.forEach {
            if (lowercase.endsWith(it)){
                return true
            }
        }
        context.getRtConfig().rtResourcesConfig?.suffix?.forEach {
            if (lowercase.endsWith(it)){
                return true
            }
        }
        return false
    }
}