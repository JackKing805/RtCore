package com.jerry.rt.core.http.interfaces

import java.util.*

/**
 * @className: ISession
 * @description: session
 * @author: Jerry
 * @date: 2023/1/26:14:30
 **/
interface ISession {
    fun getCreationTime(): Long

    fun getId(): String

    fun getLastAccessedTime(): Long

    fun setLastAccessedTime(time:Long)

    fun setMaxInactiveInterval(interval: Int)

    fun getMaxInactiveInterval(): Int

    fun getAttribute(name: String): Any?

    fun getAttributeNames(): Enumeration<String>?

    fun setAttribute(name: String, value: Any?)

    fun removeAttribute(name: String)

    fun invalidate()

    fun isNew(): Boolean

    fun setIsNew(isNew:Boolean)
}