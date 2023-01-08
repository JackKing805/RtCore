package com.jerry.rt.utils;

import java.nio.charset.Charset;

/**
 * @className: URLEncodeUtil
 * @description:
 * @author: Jerry
 * @date: 2023/1/8:12:24
 **/

public class URLEncodeUtil {
    public static String encode(String url, Charset charset) {
        return URLEncoder.DEFAULT.encode(url, charset);
    }

}
