package com.jerry.rt.core

import com.jerry.rt.bean.RtConfig
import com.jerry.rt.core.thread.Looper

/**
 * @className: Context
 * @description: RtCore 上下文
 * @author: Jerry
 * @date: 2022/12/31:15:10
 **/
class Context(private val rtConfig: RtConfig) {
    private val mainLooper: Looper = Looper.getMainLooper()




    fun getRtConfig() = rtConfig
}