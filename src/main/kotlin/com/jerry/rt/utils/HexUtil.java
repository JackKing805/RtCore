package com.jerry.rt.utils;

import java.awt.*;
import java.math.BigInteger;
import java.nio.charset.Charset;

/**
 * @className: HexUtil
 * @description:
 * @author: Jerry
 * @date: 2023/1/8:12:26
 **/

public class HexUtil {
    private static final char[] DIGITS_LOWER = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static final char[] DIGITS_UPPER = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public HexUtil() {
    }

    public static boolean isHexNumber(String value) {
        int index = value.startsWith("-") ? 1 : 0;
        if (!value.startsWith("0x", index) && !value.startsWith("0X", index) && !value.startsWith("#", index)) {
            return false;
        } else {
            try {
                Long.decode(value);
                return true;
            } catch (NumberFormatException var3) {
                return false;
            }
        }
    }

    public static char[] encodeHex(byte[] data) {
        return encodeHex(data, true);
    }

    public static char[] encodeHex(byte[] data, boolean toLowerCase) {
        return encodeHex(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
    }

    public static String encodeHexStr(byte[] data) {
        return encodeHexStr(data, true);
    }

    public static String encodeHexStr(byte[] data, boolean toLowerCase) {
        return encodeHexStr(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
    }



    public static String encodeColor(Color color) {
        return encodeColor(color, "#");
    }

    public static String encodeColor(Color color, String prefix) {
        StringBuilder builder = new StringBuilder(prefix);
        String colorHex = Integer.toHexString(color.getRed());
        if (1 == colorHex.length()) {
            builder.append('0');
        }

        builder.append(colorHex);
        colorHex = Integer.toHexString(color.getGreen());
        if (1 == colorHex.length()) {
            builder.append('0');
        }

        builder.append(colorHex);
        colorHex = Integer.toHexString(color.getBlue());
        if (1 == colorHex.length()) {
            builder.append('0');
        }

        builder.append(colorHex);
        return builder.toString();
    }

    public static Color decodeColor(String hexColor) {
        return Color.decode(hexColor);
    }

    public static String toUnicodeHex(int value) {
        StringBuilder builder = new StringBuilder(6);
        builder.append("\\u");
        String hex = toHex(value);
        int len = hex.length();
        if (len < 4) {
            builder.append("0000", 0, 4 - len);
        }

        builder.append(hex);
        return builder.toString();
    }

    public static String toUnicodeHex(char ch) {
        return "\\u" + DIGITS_LOWER[ch >> 12 & 15] + DIGITS_LOWER[ch >> 8 & 15] + DIGITS_LOWER[ch >> 4 & 15] + DIGITS_LOWER[ch & 15];
    }

    public static String toHex(int value) {
        return Integer.toHexString(value);
    }

    public static int hexToInt(String value) {
        return Integer.parseInt(value, 16);
    }

    public static String toHex(long value) {
        return Long.toHexString(value);
    }

    public static long hexToLong(String value) {
        return Long.parseLong(value, 16);
    }

    public static void appendHex(StringBuilder builder, byte b, boolean toLowerCase) {
        char[] toDigits = toLowerCase ? DIGITS_LOWER : DIGITS_UPPER;
        int high = (b & 240) >>> 4;
        int low = b & 15;
        builder.append(toDigits[high]);
        builder.append(toDigits[low]);
    }

    public static BigInteger toBigInteger(String hexStr) {
        return null == hexStr ? null : new BigInteger(hexStr, 16);
    }



    private static String encodeHexStr(byte[] data, char[] toDigits) {
        return new String(encodeHex(data, toDigits));
    }

    private static char[] encodeHex(byte[] data, char[] toDigits) {
        int len = data.length;
        char[] out = new char[len << 1];
        int i = 0;

        for(int j = 0; i < len; ++i) {
            out[j++] = toDigits[(240 & data[i]) >>> 4];
            out[j++] = toDigits[15 & data[i]];
        }

        return out;
    }

}
