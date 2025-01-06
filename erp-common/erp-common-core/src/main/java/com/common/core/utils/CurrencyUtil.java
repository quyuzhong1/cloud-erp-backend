package com.common.core.utils;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 币种工具类
 *
 * @author Jim
 * @since 2024-12-18
 */
@Slf4j
public class CurrencyUtil {

    /**
     * 解析带币种符号的金额
     * @param currencyStr 带币种符号的金额
     * @return 金额
     */
    public static BigDecimal parseAmount(String currencyStr) {
        // 定义正则，移除非数字、小数点、逗号字符
        Pattern pattern = Pattern.compile("[^\\d.,]");
        Matcher matcher = pattern.matcher(currencyStr);
        // 替换匹配到的非金额字符为空
        String cleanStr = matcher.replaceAll("");
        // 去掉逗号，保留纯金额部分
        cleanStr = cleanStr.replace(",", "");
        if (!cleanStr.isEmpty()) {
            return new BigDecimal(cleanStr);
        } else {
            throw new IllegalArgumentException("无法解析金额: " + currencyStr);
        }
    }
}
