package com.sdk.wms.aiya.utils;

import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * AIYA（爱亚 / 百世海外仓）API 签名工具。
 * <p>
 * 签名规则（sign_method=md5）：
 * <ol>
 *     <li>取 API 请求参数中的 {@code bizData} 与 {@code partnerKey} 值，依次拼接为待签名串：
 *         {@code bizData + partnerKey}；</li>
 *     <li>将待签名串以 UTF-8 转成字节流，做 MD5 摘要；</li>
 *     <li>摘要结果用十六进制（<b>小写</b>）表示，固定 32 位，即为 {@code sign}。</li>
 * </ol>
 * <p>
 * 示例：{@code bizData} 为业务 XML/JSON 字符串，{@code partnerKey} 为 {@code 123456}，
 * 则待签名串为 {@code <?xml...><request>...</request>123456}，MD5 后得到 32 位小写十六进制签名。
 * <p>
 * 注意：{@code partnerKey} 仅用于本地签名计算，不随请求发送给第三方。
 */
@Slf4j
public final class AiyaSignUtils {

    /**
     * 签名字段名
     */
    public static final String SIGN_FIELD = "sign";

    /**
     * 签名字符集
     */
    private static final String CHARSET_UTF8 = "UTF-8";

    private AiyaSignUtils() {
    }

    /**
     * 生成 AIYA 请求签名。
     *
     * @param bizData    业务数据字符串（参与签名并随请求发送）
     * @param partnerKey AIYA 下发的合作方密钥（仅用于本地签名，不发送）
     * @return 32 位小写 MD5 sign
     */
    public static String sign(String bizData, String partnerKey) {
        if (bizData == null) {
            throw new ServiceException(ApiError.WH_AIYA_SDK_SIGN_PARAMS_EMPTY);
        }
        if (partnerKey == null || partnerKey.isEmpty()) {
            throw new ServiceException(ApiError.WH_AIYA_SDK_SIGN_SECRET_EMPTY);
        }
        String sign = makeSign(bizData + partnerKey, CHARSET_UTF8);
        if (log.isDebugEnabled()) {
            log.debug("AIYA 签名完成, bizDataLength={}, sign={}", bizData.length(), sign);
        }
        return sign;
    }

    /**
     * 校验请求中携带的 sign 是否合法。
     *
     * @param bizData    业务数据字符串
     * @param partnerKey 合作方密钥
     * @param sign       请求携带的签名
     * @return 签名一致返回 true
     */
    public static boolean verify(String bizData, String partnerKey, String sign) {
        if (sign == null) {
            return false;
        }
        return sign.equalsIgnoreCase(sign(bizData, partnerKey));
    }

    /**
     * 按 AIYA 规则对待签名串做 MD5 摘要，并转为 32 位小写十六进制字符串。
     *
     * @param data   待签名串（bizData + partnerKey）
     * @param encode 字符集
     * @return 32 位小写十六进制签名
     */
    private static String makeSign(String data, String encode) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(data.getBytes(encode));
            byte[] b = md.digest();
            StringBuilder output = new StringBuilder(32);
            for (byte value : b) {
                String temp = Integer.toHexString(value & 0xff);
                if (temp.length() < 2) {
                    output.append("0");
                }
                output.append(temp);
            }
            return output.toString();
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new ServiceException(e, ApiError.WH_AIYA_SDK_SIGN_FAILED, e.getMessage());
        }
    }
}
