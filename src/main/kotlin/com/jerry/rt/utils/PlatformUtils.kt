package com.jerry.rt.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @className: PlantformUtils
 * @description: 判断平台
 * @author: Jerry
 * @date: 2023/1/12:22:01
 **/
object PlatformUtils {
    fun isAndroid():Boolean{
       return try {
           CoroutineScope(Dispatchers.Main).launch {  }
           true
       }catch (e:Exception){
           false
       }
    }
}