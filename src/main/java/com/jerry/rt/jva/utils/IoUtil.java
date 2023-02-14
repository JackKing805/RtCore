package com.jerry.rt.jva.utils;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.OutputStream;

/**
 * @className: IoUtil
 * @author: Jerry
 * @date: 2023/2/14:22:39
 **/
public class IoUtil {
    public static BufferedOutputStream toBuffered(OutputStream out) {
        return (out instanceof BufferedOutputStream) ? (BufferedOutputStream) out : new BufferedOutputStream(out);
    }

    public static void close(Closeable closeable) {
        if (null != closeable) {
            try {
                closeable.close();
            } catch (Exception e) {
                // 静默关闭
            }
        }
    }
}
