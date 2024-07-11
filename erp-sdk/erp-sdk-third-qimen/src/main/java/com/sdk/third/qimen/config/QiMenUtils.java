package com.sdk.third.qimen.config;

import com.alibaba.fastjson.JSON;
import com.taobao.api.BaseTaobaoRequest;
import com.taobao.api.TaobaoResponse;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Slf4j
@NoArgsConstructor
public class QiMenUtils {
    private static final List<String> QIMEN_CRM_SIGNED_FIELDS = Arrays
            .asList("pageNo", "pageSize", "fields", "extendProps", "customerid", "method", "sd_code", "startModified",
                    "endModified");
    private static final List<String> QIMEN_EXCLUDE_SIGN_FIELDS = Arrays.asList("wdt3_customer_id", "wdt_sign");

    /**
     * 获取奇门自定义接口的签名
     *
     * @param request   请求
     * @param wdtSecret app_secret的前半部分
     * @return 签名值
     */
    public static <T extends TaobaoResponse> String getQimenCustomWdtSign(BaseTaobaoRequest<T> request, String wdtSecret) {
        Map<String, String> params = request.getTextParams();
        params.put("method", request.getApiMethodName());
        log.debug("{}: ", params);

        StringBuilder toBeSignedStringBuilder = new StringBuilder();
        getToBeSignedString(toBeSignedStringBuilder, params);
        toBeSignedStringBuilder.insert(0, wdtSecret).append(wdtSecret);
        log.debug("toBeSignedString: {}", toBeSignedStringBuilder.toString());
        log.debug("result: {}", DigestUtils.md5Hex(toBeSignedStringBuilder.toString()));

        return DigestUtils.md5Hex(toBeSignedStringBuilder.toString());
    }

    private String sign(Map<String, String> args, String secret) {
        StringBuilder sb = new StringBuilder();
        sb.append(secret);
        for (Map.Entry<String, String> item : args.entrySet()) {
            if (item.getKey().equals("sign"))
                continue;
            sb.append(item.getKey());
            sb.append(item.getValue());
        }
        sb.append(secret);

        return md5(sb.toString());
    }

    private static String md5(String info) {
        try {
            // 获取 MessageDigest 对象，参数为 MD5 字符串，表示这是一个 MD5 算法（其他还有 SHA1 算法等）：
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            // update(byte[])方法，输入原数据
            // 类似StringBuilder对象的append()方法，追加模式，属于一个累计更改的过程
            md5.update(info.getBytes(StandardCharsets.UTF_8));
            // digest()被调用后,MessageDigest对象就被重置，即不能连续再次调用该方法计算原数据的MD5值。可以手动调用reset()方法重置输入源。
            // digest()返回值16位长度的哈希值，由byte[]承接
            byte[] md5Array = md5.digest();
            // byte[]通常我们会转化为十六进制的32位长度的字符串来使用,本文会介绍三种常用的转换方法
            return bytesToHex(md5Array);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static String bytesToHex(byte[] md5Array) {
        StringBuilder strBuilder = new StringBuilder();
        for (int i = 0; i < md5Array.length; i++) {
            int temp = 0xff & md5Array[i];
            String hexString = Integer.toHexString(temp);
            if (hexString.length() == 1) {// 如果是十六进制的0f，默认只显示f，此时要补上0
                strBuilder.append("0").append(hexString);
            } else {
                strBuilder.append(hexString);
            }
        }

        return strBuilder.toString();
    }

    //	private static boolean isValidJson(String content)
    public static boolean isValidJson(String content) {
        if (null == content)
            return false;

        String trimmedContent = content.trim();
        if (!(trimmedContent.startsWith("{") && trimmedContent.endsWith("}")) && !(trimmedContent.startsWith("[") && trimmedContent.endsWith("]")))
            return false;

        return JSON.isValid(content);
    }

    @SuppressWarnings("unchecked")
    private static void getToBeSignedString(StringBuilder stringBuilder, Object object) {
        if (object instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) object;
            map.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey)).forEachOrdered(p -> {
                if (QIMEN_EXCLUDE_SIGN_FIELDS.contains(p.getKey()) || null == p.getValue()){
                    return;
                }

                stringBuilder.append(p.getKey());

                Object value = p.getValue();

                if (value instanceof Integer) {
                    stringBuilder.append(value);
                } else if (value instanceof String) {
                    if (JSON.isValidObject(value.toString())) {
                        getToBeSignedString(stringBuilder, JSON.parseObject(p.getValue().toString(), Map.class));
                    } else if (JSON.isValidArray(value.toString())) {
                        for (Object obj : JSON.parseArray(value.toString(), Object.class)) {
                            getToBeSignedString(stringBuilder, obj);
                        }
                    } else {
                        stringBuilder.append((String) value);
                    }
                } else if (value instanceof BigDecimal) {
                    stringBuilder.append(((BigDecimal) value).toPlainString());
                } else if (value instanceof Long) {
                    stringBuilder.append(value);
                } else if (value instanceof Boolean) {
                    stringBuilder.append(((Boolean) value).booleanValue());
                } else if (value instanceof Float) {
                    stringBuilder.append(value);
                } else if (value instanceof Double) {
                    stringBuilder.append(value);
                } else {
                    getToBeSignedString(stringBuilder, value);
                }

            });
        } else if (object instanceof List) {
            for (Map map : (List<Map>) object) {
                getToBeSignedString(stringBuilder, map);
            }
        } else {
            stringBuilder.append(object.toString());
        }
    }

    static <T extends TaobaoResponse> String getQimenOfficialWdtSign(BaseTaobaoRequest<T> request, String secret) {
        Map<String, String> params = request.getTextParams();
        params.put("method", request.getApiMethodName());
        params.entrySet().removeIf(e -> !QIMEN_CRM_SIGNED_FIELDS.contains(e.getKey()));
        StringBuilder toBeSignedStringBuilder = new StringBuilder();
        getToBeSignedString(toBeSignedStringBuilder, params);
        toBeSignedStringBuilder.insert(0, secret).append(secret);

        return DigestUtils.md5Hex(toBeSignedStringBuilder.toString());
    }
}
