package com.sdk.third.lingxing.utils;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * 领星API 签名工具类
 *
 * @author Jim
 * @since 2024-02-05
 */
@Slf4j
public class LingxingApiSignUtils {


    public static String sign(Map<String, Object> params, String appSecret) {
        // 参数排序
        TreeMap<String, Object> treeMap = new TreeMap<>(params);
        // 以「key=value&key2=value2」的方式组合成字符串
        String paramValue = treeMap.entrySet().stream()
                .filter(e -> null != e.getValue() && StringUtils.isNotBlank(e.getValue().toString()))
                .map(e -> StrUtil.format("{}={}", e.getKey(), e.getValue()))
                .collect(Collectors.joining("&"));
        // md5加密
        String md5Hex = DigestUtils.md5Hex(paramValue.getBytes(StandardCharsets.UTF_8)).toUpperCase();
        log.info("params append: {},md5Hex:{}", paramValue, md5Hex);
        return AesUtil.encryptEcb(md5Hex, appSecret);
    }
}
