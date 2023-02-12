package com.jerry.rt.core.thread

import java.lang.Thread.sleep
import java.util.LinkedHashMap
import java.util.LinkedList
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

/**
 * @className: Looper
 * @description: 线程暂停
 * @author: Jerry
 * @date: 2022/12/31:15:46
 **/
class Looper {
    private var initThread:Thread?=null
    private var active = AtomicBoolean(true)

    fun prepare(){
        if (initThread!=null){
            throw IllegalStateException("looper can only be prepare once")
        }
        initThread = Thread.currentThread()
    }

    fun loop(){
        initThread?:throw NullPointerException("please prepare first")

        val currentThread = Thread.currentThread()

        if (initThread!!.id != currentThread.id){
            throw IllegalStateException("please loop at same thread")
        }

        while (active.get()){
            sleep(500)
        }
    }


    fun stop(){
        active.set(false)
    }

    fun getThread() = initThread?:throw NullPointerException("please prepare first")

    companion object{
        private lateinit var mainLooper: Looper

        internal fun setMainLooper(looper: Looper){
            mainLooper = looper
        }

        fun getMainLooper(): Looper = mainLooper
    }
}