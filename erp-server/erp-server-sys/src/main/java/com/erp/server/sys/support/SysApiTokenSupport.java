package com.erp.server.sys.support;

import com.common.core.exception.ServiceException;
import com.erp.model.sys.constants.SysApiTokenConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.AntPathMatcher;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * API Token 生成、脱敏和白名单路径处理工具。
 */
public final class SysApiTokenSupport {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final int TOKEN_RANDOM_BYTES = 32;

    private static final int TOKEN_PREVIEW_PREFIX_LENGTH = 12;

    private static final int TOKEN_PREVIEW_SUFFIX_LENGTH = 6;

    private static final AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();

    private SysApiTokenSupport() {
    }

    public static String generateToken() {
        byte[] bytes = new byte[TOKEN_RANDOM_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        return SysApiTokenConstants.TOKEN_PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte item : bytes) {
                sb.append(String.format("%02x", item));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new ServiceException(e, "生成令牌哈希失败");
        }
    }

    public static String tokenPreviewPrefix(String token) {
        return token.substring(0, Math.min(TOKEN_PREVIEW_PREFIX_LENGTH, token.length()));
    }

    public static String tokenPreviewSuffix(String token) {
        int startIndex = Math.max(0, token.length() - TOKEN_PREVIEW_SUFFIX_LENGTH);
        return token.substring(startIndex);
    }

    public static String maskToken(String tokenPreviewPrefix, String tokenPreviewSuffix) {
        return StringUtils.defaultString(tokenPreviewPrefix) + "******" + StringUtils.defaultString(tokenPreviewSuffix);
    }

    public static String normalizePathPattern(String pathPattern) {
        String value = StringUtils.trimToEmpty(pathPattern);
        if (StringUtils.isBlank(value)) {
            throw new ServiceException("接口路径不能为空");
        }
        if (value.contains("\\")) {
            throw new ServiceException("接口路径不允许包含反斜杠");
        }

        value = removeQueryAndFragment(value);
        if (value.startsWith("http://") || value.startsWith("https://")) {
            try {
                value = StringUtils.defaultString(URI.create(value).getRawPath());
            } catch (Exception e) {
                throw new ServiceException("接口路径格式不合法");
            }
        }
        if (!value.startsWith("/")) {
            value = "/" + value;
        }
        while (value.contains("//")) {
            value = value.replace("//", "/");
        }
        if (value.contains("..")) {
            throw new ServiceException("接口路径不允许包含路径穿越");
        }
        if (value.length() > 1 && value.endsWith("/") && !value.endsWith("/**")) {
            value = value.substring(0, value.length() - 1);
        }
        if (StringUtils.isBlank(value) || "/".equals(value)) {
            throw new ServiceException("接口路径不能为空");
        }
        if (value.length() > 500) {
            throw new ServiceException("接口路径最大长度不能超过500位");
        }
        return value;
    }

    public static boolean matchPathPattern(String pattern, String requestPath) {
        String normalizedPattern = normalizePathPattern(pattern);
        String normalizedRequestPath = normalizePathPattern(requestPath);
        return ANT_PATH_MATCHER.match(normalizedPattern, normalizedRequestPath);
    }

    private static String removeQueryAndFragment(String value) {
        int queryIndex = value.indexOf('?');
        int fragmentIndex = value.indexOf('#');
        int endIndex = -1;
        if (queryIndex >= 0 && fragmentIndex >= 0) {
            endIndex = Math.min(queryIndex, fragmentIndex);
        } else if (queryIndex >= 0) {
            endIndex = queryIndex;
        } else if (fragmentIndex >= 0) {
            endIndex = fragmentIndex;
        }
        return endIndex >= 0 ? value.substring(0, endIndex) : value;
    }
}
