package com.erp.server.oms.service.address.parser;

import java.util.regex.Pattern;

/**
 * Text cleaning helpers for parse flow.
 */
public final class TextCleaner {

    private static final Pattern MULTI_SPACE = Pattern.compile("\\s+");

    private TextCleaner() {
    }

    public static String normalize(String input) {
        if (input == null) {
            return "";
        }
        String text = input
                .replace('，', ',')
                .replace('；', ';')
                .replace('：', ':')
                .replace('（', '(')
                .replace('）', ')');
        text = text.replaceAll("[\\r\\n\\t]+", " ");
        text = MULTI_SPACE.matcher(text).replaceAll(" ").trim();
        return text;
    }

    public static String removeToken(String text, String token) {
        if (text == null || token == null || token.trim().isEmpty()) {
            return text == null ? "" : text;
        }
        return text.replace(token, " ");
    }

    public static String removeLabelWords(String text) {
        if (text == null) {
            return "";
        }
        return text
                .replace("收件人", " ")
                .replace("收货人", " ")
                .replace("联系人", " ")
                .replace("电话", " ")
                .replace("手机", " ")
                .replace("邮编", " ")
                .replace("姓名", " ");
    }
}
