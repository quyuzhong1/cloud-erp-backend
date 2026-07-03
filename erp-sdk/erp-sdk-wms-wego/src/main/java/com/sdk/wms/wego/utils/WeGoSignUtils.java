package com.sdk.wms.wego.utils;

import com.alibaba.fastjson.JSON;
import com.common.core.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * WEGO 海外仓 API 签名工具。
 * <p>
 * 签名算法（sign_method=md5），严格按 WEGO 官方示例 {@code signRequest} 实现：
 * <ol>
 *     <li>取出全部请求参数（含公共参数与业务参数），剔除 {@code sign} 字段；</li>
 *     <li>外层按参数名 ASCII 升序排序（{@code TreeMap}）；</li>
 *     <li>按 key + value 顺序无分隔符拼接成长串：
 *         <ul>
 *             <li>简单类型直接 {@link Object#toString()}；
 *                 {@link BigDecimal} 使用 {@link BigDecimal#toPlainString()} 避免科学计数法；</li>
 *             <li>复杂结构体（{@link Map} / {@link Collection} / 数组）<b>直接序列化为紧凑 JSON 字符串，
 *                 保留传入对象的原始字段顺序，不做内层 ASCII 排序</b>。
 *                 服务端示例用 {@code Jackson.writeValueAsString(JsonNode)} 处理，
 *                 也仅保留请求 JSON 中字段的原始顺序；如果客户端强行对内层做字典序排序，
 *                 会导致两侧内层 JSON 字符串不同、MD5 不一致；</li>
 *             <li>空字符串字段也参与签名（{@code key + ""}），与服务端 {@code asText()} 行为一致。</li>
 *         </ul>
 *     </li>
 *     <li>在长串首尾各拼接一次 secret；</li>
 *     <li>对完整字符串以 UTF-8 字节做 MD5，转 32 位大写 16 进制即为 {@code sign}。</li>
 * </ol>
 *
 * <pre>
 * 用法：
 *   Map&lt;String, Object&gt; params = new HashMap&lt;&gt;();
 *   params.put("accessToken", "6241b26c9b46460fa5492cd545ce1ddd");
 *   params.put("interfaceType", "warehouse.get");
 *   String sign = WeGoSignUtils.sign(params, secret);
 *   params.put("sign", sign);
 * </pre>
 *
 * @author Cloud ERP
 * @since 2026-05-29
 */
@Slf4j
public final class WeGoSignUtils {

    /**
     * 签名字段名，参与签名时需要剔除
     */
    public static final String SIGN_FIELD = "sign";

    private WeGoSignUtils() {
    }

    /**
     * 生成 WEGO 请求签名。
     *
     * @param params 参与签名的请求参数；如包含 sign 字段会被自动忽略
     * @param secret WEGO【API 授权】模块下发的密钥
     * @return 32 位大写 MD5 sign
     */
    public static String sign(Map<String, Object> params, String secret) {
        if (params == null) {
            throw new ServiceException("WEGO 签名参数不能为空");
        }
        if (secret == null || secret.isEmpty()) {
            throw new ServiceException("WEGO 签名 secret 不能为空");
        }
        String content = buildSignContent(params);
        String raw = secret + content + secret;
        String md5 = DigestUtils.md5Hex(raw.getBytes(StandardCharsets.UTF_8)).toUpperCase(Locale.ROOT);
        if (log.isDebugEnabled()) {
            log.debug("WEGO 签名原文: {}, sign={}", raw, md5);
        }
        return md5;
    }

    /**
     * 通过 JSON 字符串生成签名。
     *
     * @param paramJson 请求参数 JSON 字符串
     * @param secret    密钥
     * @return 32 位大写 MD5 sign
     */
    public static String sign(String paramJson, String secret) {
        if (paramJson == null || paramJson.isEmpty()) {
            throw new ServiceException("WEGO 签名 JSON 不能为空");
        }
        return sign(JSON.parseObject(paramJson), secret);
    }

    /**
     * 校验请求中携带的 sign 是否合法。
     *
     * @param params 完整请求参数（必须包含 sign 字段）
     * @param secret 密钥
     * @return 签名一致返回 true
     */
    public static boolean verify(Map<String, Object> params, String secret) {
        if (params == null) {
            return false;
        }
        Object origin = params.get(SIGN_FIELD);
        if (origin == null) {
            return false;
        }
        String expected = sign(params, secret);
        return expected.equalsIgnoreCase(origin.toString());
    }

    /**
     * 拼接 MD5 加密前的待签名长串（不含首尾 secret）。
     * <p>
     * 跳过规则（与 WEGO 服务端验签保持一致）：
     * <ul>
     *     <li>{@code sign} 字段不参与签名；</li>
     *     <li>value 为 {@code null} 的字段不参与签名（fastjson 默认也不会把 null 字段序列化到请求体，
     *         与服务端收到的 JSON 字段集保持一致）；</li>
     *     <li><b>空字符串字段参与签名</b>（拼接 {@code key + ""}），
     *         与服务端 {@code JsonNode.asText()} 对空串的处理一致。</li>
     * </ul>
     * 主要用于联调时排查签名失败问题。
     *
     * @param params 请求参数
     * @return 待签名长串
     */
    public static String buildSignContent(Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        TreeMap<String, Object> sorted = new TreeMap<>();
        params.forEach((k, v) -> sorted.put(String.valueOf(k), v));
        sorted.forEach((key, value) -> {
            if (SIGN_FIELD.equals(key) || value == null) {
                return;
            }
            sb.append(key).append(stringifyValue(value));
        });
        return sb.toString();
    }

    /**
     * 将 value 转换为参与签名拼接的字符串。
     * <p>
     * 处理优先级：
     * <ol>
     *     <li>{@link BigDecimal}：使用 {@link BigDecimal#toPlainString()} 避免科学计数法；</li>
     *     <li>复杂结构（{@link Map} / {@link Collection} / 数组）：使用 fastjson 直接序列化为紧凑 JSON 字符串，
     *         <b>保留原始字段顺序，不做内层 ASCII 排序</b>，与 WEGO 服务端 Jackson
     *         {@code writeValueAsString(JsonNode)} 输出对齐；</li>
     *     <li>其他类型：使用 {@link Object#toString()}。</li>
     * </ol>
     */
    private static String stringifyValue(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).toPlainString();
        }
        if (isComplex(value)) {
            return JSON.toJSONString(value);
        }
        return value.toString();
    }

    /**
     * 判断 value 是否为复杂结构（Map / Collection / 数组）。
     * 复杂结构在签名时整体序列化为紧凑 JSON 字符串。
     */
    private static boolean isComplex(Object value) {
        return value instanceof Map
                || value instanceof Collection
                || (value != null && value.getClass().isArray());
    }
}
