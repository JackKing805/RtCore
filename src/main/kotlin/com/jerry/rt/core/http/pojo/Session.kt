package com.jerry.rt.core.http.pojo

import com.jerry.rt.core.Context
import com.jerry.rt.core.http.interfaces.ISession
import com.jerry.rt.core.http.other.SessionManager
import com.jerry.rt.extensions.createExceptionCoroutineScope
import com.jerry.rt.extensions.logError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import kotlin.Exception

/**
 * @className: Session
 * @description: 每个链接的session状态
 * @author: Jerry
 * @date: 2023/1/26:14:24
 **/
class Session(private val id:String) :ISession{
    private val creationTime  = System.currentTimeMillis()
    private var isNew = true
    private var isValid = true
    private var maxInactiveInterval = -1//秒，表示session过期时间

    private val attributes: ConcurrentMap<String, Any?> = ConcurrentHashMap()
    private var thisAccessedTime: Long = creationTime


    suspend fun listen(){
        while (isValidInterval()){
            delay(5000)
        }
    }

    override fun getCreationTime(): Long {
        return creationTime
    }

    override fun getId(): String {
        return id
    }

    override fun getLastAccessedTime(): Long {
        return thisAccessedTime
    }

    override fun setLastAccessedTime(time: Long) {
        thisAccessedTime = time
    }

    override fun setMaxInactiveInterval(interval: Int) {
        maxInactiveInterval = interval
    }

    override fun getMaxInactiveInterval(): Int {
        return maxInactiveInterval
    }

    override fun getAttribute(name: String): Any? {
        if (!isValidInterval()){
            return null
        }
        return attributes[name]
    }

    override fun getAttributeNames(): Enumeration<String>? {
        if (!isValidInterval()){
            return Collections.enumeration(setOf())
        }
        val names: Set<String> = HashSet(attributes.keys)
        return Collections.enumeration(names)
    }

    override fun setAttribute(name: String, value: Any?) {
        if (!isValidInterval()){
            return
        }
        attributes[name] = value
    }

    override fun removeAttribute(name: String) {
        if (!isValidInterval()){
            return
        }
        attributes.remove(name)
    }

    override fun invalidate() {
        isValid = false
        maxInactiveInterval = -1
    }

    override fun isNew(): Boolean {
        return isNew
    }

    override fun setIsNew(isNew: Boolean) {
        this.isNew = isNew
    }


    fun isValidInterval():Boolean{
        if (!isValid){
            return false
        }
        if (maxInactiveInterval==-1){
            return false
        }
        return System.currentTimeMillis() - thisAccessedTime<=maxInactiveInterval
    }
}