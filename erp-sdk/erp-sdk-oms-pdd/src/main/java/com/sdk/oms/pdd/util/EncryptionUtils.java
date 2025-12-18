package com.sdk.oms.pdd.util;

import cn.hutool.json.JSONUtil;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class EncryptionUtils {

    /**
     * 生成API请求的签名
     *
     * @param params 请求参数Map，包含公共参数和业务参数
     * @param clientSecret 分配给客户端的密钥
     * @param signMethod 签名算法，当前仅支持"md5"
     * @return 生成的签名值（大写）
     * @throws NoSuchAlgorithmException 如果指定的签名算法不存在
     * @throws IllegalArgumentException 如果参数无效
     */
    public static String generateSign(Map<String, Object> params, String clientSecret, String signMethod)
            throws NoSuchAlgorithmException, IllegalArgumentException {

        // 参数校验
        if (params == null || params.isEmpty()) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if (clientSecret == null || clientSecret.trim().isEmpty()) {
            throw new IllegalArgumentException("client_secret不能为空");
        }
        if (!"md5".equalsIgnoreCase(signMethod)) {
            throw new IllegalArgumentException("当前仅支持md5签名算法");
        }

        // 步骤1：参数排序（按key的ASCII码升序排列）
        List<String> sortedKeys = new ArrayList<>(params.keySet());
        Collections.sort(sortedKeys, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                // 按ASCII码值比较
                int minLength = Math.min(s1.length(), s2.length());
                for (int i = 0; i < minLength; i++) {
                    char c1 = s1.charAt(i);
                    char c2 = s2.charAt(i);
                    if (c1 != c2) {
                        return c1 - c2;
                    }
                }
                // 如果前面部分完全相同，则长度短的排在前面
                return s1.length() - s2.length();
            }
        });

        // 步骤2：字符串拼接
        // 2.1 拼接基础字符串
        StringBuilder baseStringBuilder = new StringBuilder();
        for (String key : sortedKeys) {
            String value = params.get(key).toString();
            // 注意：即使value为空，也要拼接（除非API文档特别说明空值不参与签名）
            baseStringBuilder.append(key).append(value);
        }
        String baseString = baseStringBuilder.toString();

        // 2.2 生成最终签名字符串（client_secret + 基础字符串 + client_secret）
        String finalString = clientSecret + baseString + clientSecret;

        // 步骤3：生成sign值（MD5加密并转为大写）
        String sign = md5(finalString).toUpperCase();

        return sign;
    }

    /**
     * MD5加密方法
     *
     * @param input 需要加密的字符串
     * @return MD5加密后的十六进制字符串（小写）
     * @throws NoSuchAlgorithmException 如果MD5算法不存在
     */
    private static String md5(String input) throws NoSuchAlgorithmException {
        if (input == null) {
            return null;
        }

        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(input.getBytes());

        // 将字节数组转换为十六进制字符串
        StringBuilder hexString = new StringBuilder();
        for (byte b : digest) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString();
    }

    /**
     * 重载方法：默认使用MD5签名算法
     */
    public static String generateSign(Map<String, Object> params, String clientSecret)
            throws NoSuchAlgorithmException, IllegalArgumentException {
        return generateSign(params, clientSecret, "md5");
    }

    /**
     * 测试示例
     */
    public static void main(String[] args) {
        try {
            // 示例参数
            Map<String, Object> params = new HashMap<>();
            params.put("app_id", "20210001166857");
            params.put("method", "alipay.trade.create");
            params.put("timestamp", "2021-07-01 12:00:00");
            params.put("version", "1.0");
            params.put("biz_content", "{\"out_trade_no\":\"20210701120000\",\"total_amount\":88.88}");

            String clientSecret = "your_client_secret_here";

            // 生成签名
            String sign = generateSign(params, clientSecret);

            System.out.println("生成的签名: " + sign);
            System.out.println("签名长度: " + sign.length()); // MD5签名固定32位

            // 验证签名（可以再次生成并比较）
            String sign2 = generateSign(params, clientSecret);
            System.out.println("签名一致: " + sign.equals(sign2));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
