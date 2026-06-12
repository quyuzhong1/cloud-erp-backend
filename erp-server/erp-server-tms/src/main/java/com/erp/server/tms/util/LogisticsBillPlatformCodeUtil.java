package com.erp.server.tms.util;

import cn.hutool.core.text.CharSequenceUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 物流单 platformCode 匹配工具：库侧可能以英文逗号存储多个平台订单号。
 */
public final class LogisticsBillPlatformCodeUtil {

    private LogisticsBillPlatformCodeUtil() {
    }

    public static List<String> splitPlatformCodes(String platformCodes) {
        if (CharSequenceUtil.isBlank(platformCodes)) {
            return Collections.emptyList();
        }
        return Arrays.stream(platformCodes.split(","))
                .map(CharSequenceUtil::trim)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Excel 侧单个平台订单号是否命中 VO 侧平台订单号（支持逗号拼接）。
     */
    public static boolean matches(String excelPlatformCode, String voPlatformCode) {
        String importCode = CharSequenceUtil.trim(excelPlatformCode);
        if (CharSequenceUtil.isBlank(importCode) || CharSequenceUtil.isBlank(voPlatformCode)) {
            return false;
        }
        return splitPlatformCodes(voPlatformCode).stream()
                .anyMatch(item -> CharSequenceUtil.equals(importCode, item));
    }
}
