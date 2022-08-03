package com.common.core.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Classname 正则格式校验器，用于校验各种数据类型
 * @Description TODO
 * @Date 2022-08-02 17:19
 * @Created by yl
 */
public class ValidatorUtil {
    /**
     * 邮箱地址校验
     */
    private static Pattern emailPattern = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");


    /**
     * 判断是否Email
     *
     * @param email
     * @return
     */
    public static boolean isEmail(String email) {
        if (StringUtils.isEmpty(email)) {
            return false;
        }
        Matcher m = emailPattern.matcher(email);
        if (m.matches()) {
            return true;
        } else {
            return false;
        }
    }
}
