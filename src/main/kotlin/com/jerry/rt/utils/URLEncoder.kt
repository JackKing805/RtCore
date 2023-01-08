package com.jerry.rt.utils

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.io.Serializable
import java.nio.charset.Charset
import java.util.*

/**
 * @className: URLEncoder
 * @description:
 * @author: Jerry
 * @date: 2023/1/8:12:25
 */
class URLEncoder private constructor(private val safeCharacters: BitSet) : Serializable {
    private var encodeSpaceAsPlus = false

    constructor() : this(BitSet(256)) {
        addAlpha()
        addDigit()
    }

    fun addSafeCharacter(c: Char) {
        safeCharacters.set(c.code)
    }

    fun removeSafeCharacter(c: Char) {
        safeCharacters.clear(c.code)
    }

    fun setEncodeSpaceAsPlus(encodeSpaceAsPlus: Boolean) {
        this.encodeSpaceAsPlus = encodeSpaceAsPlus
    }

    fun encode(path: String, charset: Charset?): String {
        return if (null != charset && !path.isEmpty()) {
            val rewrittenPath = StringBuilder(path.length)
            val buf = ByteArrayOutputStream()
            val writer = OutputStreamWriter(buf, charset)
            for (i in 0 until path.length) {
                val c = path[i].code
                if (safeCharacters[c]) {
                    rewrittenPath.append(c.toChar())
                } else if (encodeSpaceAsPlus && c == ' '.code) {
                    rewrittenPath.append('+')
                } else {
                    try {
                        writer.write(c.toChar().code)
                        writer.flush()
                    } catch (var13: IOException) {
                        buf.reset()
                        continue
                    }
                    val ba = buf.toByteArray()
                    val var10 = ba.size
                    for (var11 in 0 until var10) {
                        val toEncode = ba[var11]
                        rewrittenPath.append('%')
                        HexUtil.appendHex(rewrittenPath, toEncode, false)
                    }
                    buf.reset()
                }
            }
            rewrittenPath.toString()
        } else {
            path
        }
    }

    private fun addAlpha() {
        var i: Char
        i = 'a'
        while (i <= 'z') {
            addSafeCharacter(i)
            ++i
        }
        i = 'A'
        while (i <= 'Z') {
            addSafeCharacter(i)
            ++i
        }
    }

    private fun addDigit() {
        var i = '0'
        while (i <= '9') {
            addSafeCharacter(i)
            ++i
        }
    }

    companion object {
        private const val serialVersionUID = 1L
        val DEFAULT = createDefault()
        val PATH_SEGMENT = createPathSegment()
        val FRAGMENT = createFragment()
        val QUERY = createQuery()
        val ALL = createAll()
        fun createDefault(): URLEncoder {
            val encoder = URLEncoder()
            encoder.addSafeCharacter('-')
            encoder.addSafeCharacter('.')
            encoder.addSafeCharacter('_')
            encoder.addSafeCharacter('~')
            addSubDelims(encoder)
            encoder.addSafeCharacter(':')
            encoder.addSafeCharacter('@')
            encoder.addSafeCharacter('/')
            return encoder
        }

        fun createPathSegment(): URLEncoder {
            val encoder = URLEncoder()
            encoder.addSafeCharacter('-')
            encoder.addSafeCharacter('.')
            encoder.addSafeCharacter('_')
            encoder.addSafeCharacter('~')
            addSubDelims(encoder)
            encoder.addSafeCharacter('@')
            return encoder
        }

        fun createFragment(): URLEncoder {
            val encoder = URLEncoder()
            encoder.addSafeCharacter('-')
            encoder.addSafeCharacter('.')
            encoder.addSafeCharacter('_')
            encoder.addSafeCharacter('~')
            addSubDelims(encoder)
            encoder.addSafeCharacter(':')
            encoder.addSafeCharacter('@')
            encoder.addSafeCharacter('/')
            encoder.addSafeCharacter('?')
            return encoder
        }

        fun createQuery(): URLEncoder {
            val encoder = URLEncoder()
            encoder.setEncodeSpaceAsPlus(true)
            encoder.addSafeCharacter('*')
            encoder.addSafeCharacter('-')
            encoder.addSafeCharacter('.')
            encoder.addSafeCharacter('_')
            encoder.addSafeCharacter('=')
            encoder.addSafeCharacter('&')
            return encoder
        }

        fun createAll(): URLEncoder {
            val encoder = URLEncoder()
            encoder.addSafeCharacter('*')
            encoder.addSafeCharacter('-')
            encoder.addSafeCharacter('.')
            encoder.addSafeCharacter('_')
            return encoder
        }

        private fun addSubDelims(encoder: URLEncoder) {
            encoder.addSafeCharacter('!')
            encoder.addSafeCharacter('$')
            encoder.addSafeCharacter('&')
            encoder.addSafeCharacter('\'')
            encoder.addSafeCharacter('(')
            encoder.addSafeCharacter(')')
            encoder.addSafeCharacter('*')
            encoder.addSafeCharacter('+')
            encoder.addSafeCharacter(',')
            encoder.addSafeCharacter(';')
            encoder.addSafeCharacter('=')
        }
    }
}