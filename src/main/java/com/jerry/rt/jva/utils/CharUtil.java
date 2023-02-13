package com.jerry.rt.jva.utils;


import static com.jerry.rt.jva.utils.CharPool.BACKSLASH;
import static com.jerry.rt.jva.utils.CharPool.SLASH;

public class CharUtil {
    public static boolean isFileSeparator(char c) {
        return SLASH == c || BACKSLASH == c;
    }

    public static boolean isBlankChar(char c) {
        return isBlankChar((int) c);
    }

    public static boolean isBlankChar(int c) {
        return Character.isWhitespace(c)
                || Character.isSpaceChar(c)
                || c == '\ufeff'
                || c == '\u202a'
                || c == '\u0000'
                // issue#I5UGSQ，Hangul Filler
                || c == '\u3164'
                // Braille Pattern Blank
                || c == '\u2800'
                // MONGOLIAN VOWEL SEPARATOR
                || c == '\u180e';
    }
}
