package com.erp.server.oms.service.address.parser;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Text cleaning helpers for parse flow.
 */
public final class TextCleaner {

    private static final Pattern MULTI_SPACE = Pattern.compile("\\s+");
    private static final Pattern LABEL_PATTERN = Pattern.compile(
            "(收件人|收货人|联系人|姓名|电话|电话号码|联系电话|手机号码|手机|邮编|所在地区|详细地址)\\s*[:：]?");
    private static final Pattern DETAIL_LABEL_PATTERN = Pattern.compile(
            "(?:详细地址|地址)\\s*[:：]?\\s*(.+)$");
    private static final Set<String> DETAIL_NOISE_TOKENS = new HashSet<String>(Arrays.asList(
            "号码", "手机", "电话", "地区", "所在地区", "详细", "详细地址", "地址", "省", "市", "区", "县", "自治区", "特别行政区"
    ));

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
        String cleaned = LABEL_PATTERN.matcher(text).replaceAll(" ");
        return normalize(cleaned);
    }

    public static String extractDetailAddress(String text) {
        if (StringUtils.isBlank(text)) {
            return "";
        }
        Matcher matcher = DETAIL_LABEL_PATTERN.matcher(text);
        if (matcher.find()) {
            return normalize(matcher.group(1));
        }
        return "";
    }

    public static String cleanupDetailAddress(String text) {
        String normalized = normalize(text);
        if (StringUtils.isBlank(normalized)) {
            return "";
        }
        String[] parts = normalized.split(" ");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (StringUtils.isBlank(part)) {
                continue;
            }
            if (DETAIL_NOISE_TOKENS.contains(part)) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(part);
        }
        return normalize(builder.toString());
    }
}
