package com.jerry.rt.extensions

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * @className: CoroutineScopeExtensions
 * @description: 协程帮助类
 * @author: Jerry
 * @date: 2022/12/31:16:39
 **/

internal fun createStandCoroutineScope() = CoroutineScope(Dispatchers.Default + SupervisorJob() + CoroutineExceptionHandler { coroutineContext, throwable ->
    throwable.printStackTrace()
})