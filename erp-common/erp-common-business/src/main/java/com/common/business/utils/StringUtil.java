package com.common.business.utils;

import org.apache.commons.lang3.StringUtils;

public class StringUtil {

    private StringUtil() {
    }

    /**
     * 定义下划线
     */
    private static final char UNDERLINE = '_';

    /**
     * 如果传入的字符串是null，返回空字符串，否则返回原来的字符串
     * @param input 需要检查的字符串
     * @return 返回处理后的字符串
     */
    public static String getOrDefault(String input) {
        return input == null ? "" : input;
    }

    // 将陀峰命名中的大写字母（首字母除外）转成“_小写字母”
    public static String camelToUnderline(String param) {
        if (StringUtils.isNotBlank(param)) {
            int len = param.length();
            StringBuilder sb = new StringBuilder(len);
            for (int i = 0; i < len; i++) {
                char c = param.charAt(i);
                if (Character.isUpperCase(c)) {
                    if (i != 0) {
                        sb.append(UNDERLINE);
                    }
                    sb.append(Character.toLowerCase(c));
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        } else {
            return "";
        }
    }
    
    /**
     * 下划线转驼峰
     *
     * @param underlineName
     * @return
     */
    public static String convertToCamel(String underlineName) {
        StringBuilder sb = new StringBuilder();
        boolean capitalizeNext = false;

        for (int i = 0; i < underlineName.length(); i++) {
            char currentChar = underlineName.charAt(i);

            if (currentChar == UNDERLINE) {
                capitalizeNext = true;
            } else {
                if (capitalizeNext) {
                    sb.append(Character.toUpperCase(currentChar));
                    capitalizeNext = false;
                } else {
                    sb.append(Character.toLowerCase(currentChar));
                }
            }
        }
        return sb.toString();
    }

    /**
     * 首字母大写
     *
     * @param name
     * @return
     */
    public static String upperCaseFirst(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1, name.length());
    }
}
