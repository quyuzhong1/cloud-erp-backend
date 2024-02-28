package com.common.core.utils;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlUtils {

    /**
     * 校验SQL合法性，是否有注入风险
     * @param input
     * @return false 表示不通过
     */
    public static boolean verifySqlLegality(String input){
        String pattern = "(?i)(insert|delete|update|select|drop|union|truncate|grant|exec|alter|create|rename|replace|shutdown|restore|backup|attach|detach|declare|execute)\\s";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(input);
        return !matcher.find();
    }
}
