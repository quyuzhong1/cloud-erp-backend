package com.common.core.utils;

public class StrUtils {

    /**
     * 判断字符串是否只包含数字和字母
     * @Author Luo_WG
     * @Date 2022/10/24 11:09
     * @param str str
     * @return boolean
     **/
    public static boolean isLetterDigit(String str) {
        String regex = "^[a-z0-9A-Z]+$";
        return str.matches(regex);
    }
}
