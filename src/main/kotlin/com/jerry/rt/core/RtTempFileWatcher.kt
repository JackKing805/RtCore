package com.jerry.rt.core

import com.jerry.rt.extensions.createStandCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

/**
 * @className: RtTempFileWatcher
 * @description 零时文件过期删除
 * @author: Jerry
 * @date: 2023/2/15:22:24
 **/
class RtTempFileWatcher(private val context: RtContext) {
    private val scope = createStandCoroutineScope()
    private var job:Job?=null

    fun start(){
        val rtFileConfig = context.getRtConfig().rtFileConfig
        job = scope.launch(Dispatchers.IO) {
            while (true){
                val file = File(rtFileConfig.tempFileDir)
                if (file.exists()){
                    file.listFiles()?.forEach {
                        val dis = System.currentTimeMillis() - it.lastModified()
                        if (dis>rtFileConfig.tempFileMaxAge){
                            try {
                                it.delete()
                            }catch (e:Exception){
                            }
                        }
                    }
                }

                delay(1000L*60*60)
            }
        }
    }

    fun stop(){
        job?.cancel()
        job = null
    }
}