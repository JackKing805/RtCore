package com.jerry.rt.core.http.request.input.model

import java.util.function.Consumer

/**
 * @className: BasicHeader
 * @author: Jerry
 * @date: 2023/2/15:19:40
 **/
class BasicHeader {
    private val map= mutableMapOf<String,MutableList<String>>()

    fun add(key:String,value:String){
        checkValue(value)
        val k = normalize(key)
        var l: MutableList<String>? = map[k]
        if (l == null) {
            l = mutableListOf()
            map[k] = l
        }
        l.add(value)
    }


    private fun normalize(key: String): String {
        val len = key.length
        return if (len == 0) {
            key
        } else {
            val b = key.toCharArray()
            if (b[0] in 'a'..'z') {
                b[0] = (b[0].code - 32).toChar()
            } else require(!(b[0] == '\r' || b[0] == '\n')) { "illegal character in key" }
            for (i in 1 until len) {
                if (b[i] in 'A'..'Z') {
                    b[i] = (b[i].code + 32).toChar()
                } else require(!(b[i] == '\r' || b[i] == '\n')) { "illegal character in key" }
            }
            String(b)
        }
    }

    private fun checkValue(value: String) {
        val len = value.length
        var i = 0
        while (i < len) {
            val c = value[i]
            if (c == '\r') {
                require(i < len - 2) { "Illegal CR found in header" }
                val c1 = value[i + 1]
                val c2 = value[i + 2]
                require(c1 == '\n') { "Illegal char found after CR in header" }
                require(!(c2 != ' ' && c2 != '\t')) { "No whitespace found after CRLF in header" }
                i += 2
            } else require(c != '\n') { "Illegal LF found in header" }
            ++i
        }
    }

    fun size() = map.size

    fun getFirst(key: String): String? {
        val l: MutableList<String>? = map[this.normalize(key)]
        return if (l == null) null else l[0]
    }

    fun getList(key: String): MutableList<String>? {
        return map[normalize(key)]
    }

    fun keySet(): Set<String> {
        return map.keys
    }

    fun getHeaders(): Map<String, String> {
        val map: MutableMap<String, String> = HashMap()
        val ketSet: Set<String> = keySet()
        ketSet.forEach(Consumer { s: String ->
            val first = getFirst(s)
            if (first != null) {
                map[s] = first
            }
        })
        return map
    }

}