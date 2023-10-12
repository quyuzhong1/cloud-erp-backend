package com.erp.server.wms.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import cn.hutool.json.JSONUtil;
import com.common.core.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 金蝶扩展API签名工具类
 *
 * @author Jim
 * @since 2023-10-11
 */
@Slf4j
public class KingdeeExtensionSignUtil {

    public final static String SIGN_KEY = "sign";

    /**
     * 生成签名
     * 以「key=value&key2=value2」的方式组合成字符串然后进行加密，将加密后的十六进制字符串放入参数sign中一并传入
     */
    public static String sign(TreeMap<String, String> map, final String secret, String... ignoreKeys) {
        List<String> ignoreKeyList = Stream.of(ignoreKeys).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        String mapStr = convertString(map, ignoreKeyList);
        return SecureUtil.hmacSha256(secret).digestHex(mapStr);
    }

    /**
     * 以「key=value&key2=value2」的方式组合成字符串
     */
    private static String convertString(TreeMap<String, String> map, List<String> ignoreKeyList) {
        return map.entrySet().stream()
                .filter(e -> !ignoreKeyList.contains(e.getKey()))
                .map(e -> StrUtil.format("{}={}", e.getKey(), e.getValue()))
                .collect(Collectors.joining("&"));
    }


    /**
     * 验证签名(原签名在MAP中)
     */
    public static void verify(TreeMap<String, String> map, final String secret, String... ignoreKeys) {
        // 参数中的签名
        String paramSign = map.get(SIGN_KEY);
        if (StringUtils.isBlank(paramSign)) {
            // 签名字段不能为空
            throw new ServiceException("签名字段不能为空");
        }

        // 过滤的Key
        List<String> ignoreKeyList = Stream.of(ignoreKeys).collect(Collectors.toList());
        ignoreKeyList.add(SIGN_KEY);

        // 正确的签名
        String realSign = sign(map, secret, ignoreKeyList.toArray(new String[]{}));
        boolean check = realSign.equals(paramSign);
        if (!check) {
            log.error("签名错误: params={}", JSONUtil.toJsonStr(map));
            throw new ServiceException("签名错误");
        }
    }

    public static void main(String[] args) {
        TreeMap<String, String> map = new TreeMap<>();
        map.put("c", "c");
        map.put("a", "a");
        map.put("b", "b");
        System.out.println(sign(map, "123456"));
    }

}
