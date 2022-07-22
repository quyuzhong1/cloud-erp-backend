package com.cloud.erp.common.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Classname FileUtil
 * @Description TODO
 * @Date 2022-07-12 11:12
 * @Created by yl
 */
public class FileUtil {

    /**
     * 驼峰转换下划线
     *
     * @param str
     * @return
     */
    public static String humpToUnderLine(String str) {
        Matcher matcher = humpPattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "_" + matcher.group(0).toLowerCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }


    private static Pattern humpPattern = Pattern.compile("[A-Z]");
}
