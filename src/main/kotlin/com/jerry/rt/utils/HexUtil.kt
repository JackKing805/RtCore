package com.jerry.rt.utils

import java.awt.Color
import java.math.BigInteger

/**
 * @className: HexUtil
 * @description:
 * @author: Jerry
 * @date: 2023/1/8:12:26
 */
object HexUtil {
    private val DIGITS_LOWER =
        charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')
    private val DIGITS_UPPER =
        charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')

    fun isHexNumber(value: String): Boolean {
        val index = if (value.startsWith("-")) 1 else 0
        return if (!value.startsWith("0x", index) && !value.startsWith("0X", index) && !value.startsWith("#", index)) {
            false
        } else {
            try {
                java.lang.Long.decode(value)
                true
            } catch (var3: NumberFormatException) {
                false
            }
        }
    }

    @JvmOverloads
    fun encodeHex(data: ByteArray, toLowerCase: Boolean = true): CharArray {
        return encodeHex(data, if (toLowerCase) DIGITS_LOWER else DIGITS_UPPER)
    }

    @JvmOverloads
    fun encodeHexStr(data: ByteArray, toLowerCase: Boolean = true): String {
        return encodeHexStr(data, if (toLowerCase) DIGITS_LOWER else DIGITS_UPPER)
    }

    @JvmOverloads
    fun encodeColor(color: Color, prefix: String? = "#"): String {
        val builder = StringBuilder(prefix)
        var colorHex = Integer.toHexString(color.red)
        if (1 == colorHex.length) {
            builder.append('0')
        }
        builder.append(colorHex)
        colorHex = Integer.toHexString(color.green)
        if (1 == colorHex.length) {
            builder.append('0')
        }
        builder.append(colorHex)
        colorHex = Integer.toHexString(color.blue)
        if (1 == colorHex.length) {
            builder.append('0')
        }
        builder.append(colorHex)
        return builder.toString()
    }

    fun decodeColor(hexColor: String?): Color {
        return Color.decode(hexColor)
    }

    fun toUnicodeHex(value: Int): String {
        val builder = StringBuilder(6)
        builder.append("\\u")
        val hex = toHex(value)
        val len = hex.length
        if (len < 4) {
            builder.append("0000", 0, 4 - len)
        }
        builder.append(hex)
        return builder.toString()
    }

    fun toUnicodeHex(ch: Char): String {
        return "\\u" + DIGITS_LOWER[ch.code shr 12 and 15] + DIGITS_LOWER[ch.code shr 8 and 15] + DIGITS_LOWER[ch.code shr 4 and 15] + DIGITS_LOWER[ch.code and 15]
    }

    fun toHex(value: Int): String {
        return Integer.toHexString(value)
    }

    fun hexToInt(value: String): Int {
        return value.toInt(16)
    }

    fun toHex(value: Long): String {
        return java.lang.Long.toHexString(value)
    }

    fun hexToLong(value: String): Long {
        return value.toLong(16)
    }

    fun appendHex(builder: StringBuilder, b: Byte, toLowerCase: Boolean) {
        val toDigits = if (toLowerCase) DIGITS_LOWER else DIGITS_UPPER
        val high = b.toInt() and 240 ushr 4
        val low = b.toInt() and 15
        builder.append(toDigits[high])
        builder.append(toDigits[low])
    }

    fun toBigInteger(hexStr: String?): BigInteger? {
        return if (null == hexStr) null else BigInteger(hexStr, 16)
    }

    private fun encodeHexStr(data: ByteArray, toDigits: CharArray): String {
        return String(encodeHex(data, toDigits))
    }

    private fun encodeHex(data: ByteArray, toDigits: CharArray): CharArray {
        val len = data.size
        val out = CharArray(len shl 1)
        var i = 0
        var j = 0
        while (i < len) {
            out[j++] = toDigits[240 and data[i].toInt() ushr 4]
            out[j++] = toDigits[15 and data[i].toInt()]
            ++i
        }
        return out
    }
}