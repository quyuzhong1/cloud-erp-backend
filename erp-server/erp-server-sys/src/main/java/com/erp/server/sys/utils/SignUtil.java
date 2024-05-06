package com.erp.server.sys.utils;

import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.common.core.exception.ServiceException;


/**
 * 签名算法
 */
public class SignUtil {

    private static final Logger logger = LoggerFactory.getLogger(SignUtil.class);

    public static void buildPayParams(StringBuilder sb, Map<String, String> payParams, boolean encoding) {
        List<String> keys = new ArrayList<String>(payParams.keySet());
        Collections.sort(keys);
        for (String key : keys) {
            sb.append(key).append("=");

            if (encoding) {
                sb.append(payParams.get(key));
            } else {
                sb.append(payParams.get(key));
            }
            sb.append("&");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
    }

    public static Map<String, String> paraFilter(Map<String, String> sArray) {
        if (sArray == null || sArray.size() == 0) {
            return sArray;
        }
        Map<String, String> result = new HashMap<String, String>(sArray.size());
        if (sArray.size() <= 0) {
            return result;
        }
        for (Map.Entry<String, String> set : sArray.entrySet()) {
            String value = set.getValue();
            String key = set.getKey();
            if (value == null || value.equals("") || key.equalsIgnoreCase("sign")) {
                continue;
            }
            result.put(key, value);
        }
        return result;
    }


    /**
     * 验签
     *
     * @param signData  待签名数据
     * @param charset   字符集
     * @param sign_type 签名类型
     * @param sign      签名
     * @param key       秘钥
     * @return
     */
    public static boolean checkSign(Object signData, String charset, String sign_type, String sign,
                                    String key) {
        return StringUtils.equalsIgnoreCase(genSign(getSignStr(signData), charset, sign_type, key), sign);
    }

    public static boolean equalsAny(final CharSequence string, final CharSequence... searchStrings) {
        if (ArrayUtils.isNotEmpty(searchStrings)) {
            for (final CharSequence next : searchStrings) {
                if (equals(string, next)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public static boolean equals(final CharSequence cs1, final CharSequence cs2) {
        if (cs1 == cs2) {
            return true;
        }
        if (cs1 == null || cs2 == null) {
            return false;
        }
        if (cs1.length() != cs2.length()) {
            return false;
        }
        if (cs1 instanceof String && cs2 instanceof String) {
            return cs1.equals(cs2);
        }
        // Step-wise comparison
        final int length = cs1.length();
        for (int i = 0; i < length; i++) {
            if (cs1.charAt(i) != cs2.charAt(i)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 生成签名
     *
     * @param signData  待签名数据
     * @param charset   字符集
     * @param sign_type 签名类型
     * @param key       秘钥
     * @return
     */
    public static String genSign(String signData, String charset, String sign_type, String key) {
        String sign;
        if (SignType.AES.equalsIgnoreCase(sign_type)) {
            sign = AESUtil.encrypt(signData, key, charset);
        } else if (SignType.MD5.equalsIgnoreCase(sign_type)){
            sign = MD5.str2md5(signData+key);
        }else {
        	throw new ServiceException("不支持的加密方式");
        }

        return sign;
    }

    /**
     * 将对象转换为k=v&k=v形式字符串<br>
     * 注：对象字段按照ASCII码递增排序（字母升序排序）<br>
     * 注：去掉为空和为null的字段<br>
     *
     * @param input 待签名对象
     * @return
     */
    public static String getSignStr(Object input) {
        if (input == null) {
            return null;
        }

        Map<String, Object> objFields = new TreeMap<String, Object>();
        ObjUtils.obj2Map(input, false, objFields);
        objFields.remove("sign");
        objFields.remove("requestIp");
        objFields.remove("secretKey");

        return map2Params(objFields, true);
    }

    /**
     * 将Map对象转换为k=v&k=v形式字符串<br>
     * 注：去掉为空和为null的字段<br>
     *
     * @author chenck
     * @date 2017年3月1日 下午5:39:59
     */
    public static String map2Params(Map<String, Object> params, boolean removeBlankField) {
        StringBuilder sb = new StringBuilder(1024);
        boolean first = true;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            // 移除值为空或为null的字段，不拼接到k=v串中
            if (removeBlankField
                    && (entry.getValue() == null || "".equals(entry.getValue().toString()))) {
                continue;
            }
            if (first) {
                first = false;
            } else {
                sb.append("&");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue().toString());
        }
        return sb.toString();
    }

    public static String map2ParamsStr(Map<String, String> paramsO, boolean removeBlankField) {
        Map<String, String> params = new TreeMap<>(paramsO);

        StringBuilder sb = new StringBuilder(1024);
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            // 移除值为空或为null的字段，不拼接到k=v串中
            if (removeBlankField
                    && (entry.getValue() == null || "".equals(entry.getValue().toString()))) {
                continue;
            }
            if (first) {
                first = false;
            } else {
                sb.append("&");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue().toString());
        }
        return sb.toString();
    }

    public static String mapErpParams(Map<String, Object> params, boolean removeBlankField, String charSet) {
        StringBuilder sb = new StringBuilder();
        try {
            boolean first = true;
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                // 移除值为空或为null的字段，不拼接到k=v串中
                if (removeBlankField
                        && (entry.getValue() == null || "".equals(entry.getValue().toString()))) {
                    continue;
                }
                if (first) {
                    first = false;
                } else {
                    sb.append("&");
                }
                sb.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue().toString(), charSet));
            }
        } catch (Exception e) {
            logger.error("参数拼接错误", e);
        }

        return sb.toString();
    }

    public static boolean checkSignature(String token, String signature, String timestamp, String nonce) {
        String[] paramArr = new String[]{token, timestamp, nonce};
        Arrays.sort(paramArr);
        String content = paramArr[0].concat(paramArr[1]).concat(paramArr[2]);
        String ciphertext = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(content.toString().getBytes());
            ciphertext = byteToStr(digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return ciphertext != null && ciphertext.equals(signature.toUpperCase());
    }
    private static String byteToStr(byte[] byteArray) {
        String strDigest = "";
        for (int i = 0; i < byteArray.length; i++) {
            strDigest += byteToHexStr(byteArray[i]);
        }
        return strDigest;
    }
    private static String byteToHexStr(byte mByte) {
        char[] Digit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        char[] tempArr = new char[2];
        tempArr[0] = Digit[(mByte >>> 4) & 0X0F];
        tempArr[1] = Digit[mByte & 0X0F];
        String s = new String(tempArr);
        return s;
    }
    public static void main(String[] args) {
        System.out.println(new Date().getTime());
    }

}
