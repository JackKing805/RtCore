package com.jerry.rt.extensions

import java.text.SimpleDateFormat

private var standardTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

private fun String.log(stackTraceElement: StackTraceElement,format:String){
    val runLocation = "<" + stackTraceElement.lineNumber + "> " + stackTraceElement.className + "." + stackTraceElement.methodName
    System.out.format(
        format,
        standardTimeFormat.format(
            System.currentTimeMillis()
        ),
        Thread.currentThread().name,
        runLocation,
        this
    )
}

internal fun String.logError(){
    log(Exception().stackTrace[1],"%s \u001b[31;1mERROR \u001b[31;0m--- [          %s] \u001b[32;1m%s \u001b[31;0m: %s %n")
}

internal fun String.logInfo(){
    log(Exception().stackTrace[1],"%s \u001b[33;1mINFO  \u001b[31;0m--- [          %s] \u001b[32;1m%s \u001b[31;0m: %s  %n")
}