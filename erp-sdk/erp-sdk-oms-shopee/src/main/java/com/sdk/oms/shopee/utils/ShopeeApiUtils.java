package com.sdk.oms.shopee.utils;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.core.exception.ServiceException;
import com.common.core.utils.OkHttpUtils;
import com.sdk.oms.shopee.dto.base.ShopeeAuth;
import com.sdk.oms.shopee.dto.base.ShopeeResponse;
import com.sdk.oms.shopee.dto.base.ShopeeTokenAuth;
import com.sdk.oms.shopee.dto.merchant.response.MerchantResponse;
import com.sdk.oms.shopee.dto.shop.response.ShopResponse;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author zdy
 * @ClassName ShopeeBaseService
 * @description: TODO
 * @date 2023年10月20日
 * @version: 1.0
 */
@Slf4j
public class ShopeeApiUtils {

    // Shopee SDK请求/签名失败统一抛ServiceException，调用方不再按null响应兜底。
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION = "application/json";
    private static final String MASK = "***";
    private static final Set<String> SENSITIVE_KEYS = new HashSet<>(Arrays.asList(
            "access_token", "refresh_token", "sign", "partner_key", "tmp_partner_key",
            "secret", "secret_key", "token", "authorization"
    ));
    private static final List<MaskRule> SENSITIVE_MASK_RULES = buildSensitiveMaskRules();

    public static String getPublicSign(String path, long partner_id, String tmp_partner_key) {
        long timest = System.currentTimeMillis() / 1000L;
        String tmp_base_string = String.format("%s%s%s", partner_id, path, timest);
        byte[] partner_key;
        byte[] base_string;
        String sign = "";
        try {
            base_string = tmp_base_string.getBytes("UTF-8");
            partner_key = tmp_partner_key.getBytes("UTF-8");
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(partner_key, "HmacSHA256");
            mac.init(secret_key);
            sign = String.format("%064x", new BigInteger(1, mac.doFinal(base_string)));
        } catch (Exception e) {
            log.error("虾皮签名生成异常, path: {}, 错误: {}", path, e.getMessage(), e);
            throw new ServiceException("虾皮签名生成失败");
        }
        return sign;
    }

    public static String getOrderSign(String path, String access_token, long partner_id, String tmp_partner_key, long shop_id) {
        long timest = System.currentTimeMillis() / 1000L;
        return getOrderSign(path, access_token, partner_id, tmp_partner_key, shop_id, timest);
    }

    public static String getOrderSign(String path, String access_token, long partner_id, String tmp_partner_key, long shop_id, long timest) {
        String tmp_base_string = String.format("%s%s%s%s%s", partner_id, path, timest, access_token, shop_id);
        byte[] partner_key;
        byte[] base_string;
        String sign = null;
        try {
            base_string = tmp_base_string.getBytes("UTF-8");
            partner_key = tmp_partner_key.getBytes("UTF-8");
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(partner_key, "HmacSHA256");
            mac.init(secret_key);
            sign = String.format("%064x", new BigInteger(1, mac.doFinal(base_string)));
        } catch (Exception e) {
            log.error("虾皮签名生成异常, path: {}, 错误: {}", path, e.getMessage(), e);
            throw new ServiceException("虾皮签名生成失败");
        }
        return sign;
    }

    public static String getMerchantSign(String path, String access_token, long partner_id, String tmp_partner_key, long merchant_id) {
        long timest = System.currentTimeMillis() / 1000L;
        String tmp_base_string = String.format("%s%s%s%s%s", partner_id, path, timest, access_token, merchant_id);
        byte[] partner_key;
        byte[] base_string;
        String sign = null;
        try {
            base_string = tmp_base_string.getBytes("UTF-8");
            partner_key = tmp_partner_key.getBytes("UTF-8");
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(partner_key, "HmacSHA256");
            mac.init(secret_key);
            sign = String.format("%064x", new BigInteger(1, mac.doFinal(base_string)));
        } catch (Exception e) {
            log.error("虾皮签名生成异常, path: {}, 错误: {}", path, e.getMessage(), e);
            throw new ServiceException("虾皮签名生成失败");
        }
        return sign;
    }

    /**
     * GET 请求
     *
     * @param baseUrl
     * @param paramMap
     * @return
     */
    public static ShopResponse sendShopGet(String baseUrl, HashMap<String, Object> paramMap) {
        ShopResponse resultMap = null;
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(CONTENT_TYPE, APPLICATION);
        headers.put("Connection", "keep-alive");
        String safeUrl = buildSafeUrl(baseUrl, paramMap);
        log.info("虾皮接口请求, method: GET, url: {}", safeUrl);
        try {
            String bodyStr = OkHttpUtils.doGet(baseUrl, paramMap, headers);
            log.info("虾皮接口响应, method: GET, url: {}, response: {}", safeUrl, maskSensitiveContent(bodyStr));
            resultMap = JSONUtil.toBean(bodyStr, ShopResponse.class);
        } catch (Exception e) {
            log.error("虾皮接口请求异常, method: GET, url: {}, 错误: {}", safeUrl, e.getMessage(), e);
            throw new ServiceException("虾皮店铺接口请求失败");
        }

        return requireResponse(resultMap, "虾皮店铺接口响应为空");
    }
    /**
     * GET 请求
     *
     * @param baseUrl
     * @param paramMap
     * @return
     */
    public static MerchantResponse sendMerchantGet(String baseUrl, HashMap<String, Object> paramMap) {
        MerchantResponse resultMap = null;
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(CONTENT_TYPE, APPLICATION);
        headers.put("Connection", "keep-alive");
        String safeUrl = buildSafeUrl(baseUrl, paramMap);
        log.info("虾皮接口请求, method: GET, url: {}", safeUrl);
        try {
            String bodyStr = OkHttpUtils.doGet(baseUrl, paramMap, headers);
            log.info("虾皮接口响应, method: GET, url: {}, response: {}", safeUrl, maskSensitiveContent(bodyStr));
            resultMap = JSONUtil.toBean(bodyStr, MerchantResponse.class);
        } catch (Exception e) {
            log.error("虾皮接口请求异常, method: GET, url: {}, 错误: {}", safeUrl, e.getMessage(), e);
            throw new ServiceException("虾皮商户接口请求失败");
        }

        return requireResponse(resultMap, "虾皮商户接口响应为空");
    }
    /**
     * GET 请求
     *
     * @param baseUrl
     * @param paramMap
     * @return
     */
    public static ShopeeResponse sendGet(String baseUrl, HashMap<String, Object> paramMap) {
        ShopeeResponse resultMap = null;
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(CONTENT_TYPE, APPLICATION);
        headers.put("Connection", "keep-alive");
        String safeUrl = buildSafeUrl(baseUrl, paramMap);
        log.info("虾皮接口请求, method: GET, url: {}", safeUrl);
        try {
            String bodyStr = OkHttpUtils.doGet(baseUrl, paramMap, headers);
            log.info("虾皮接口响应, method: GET, url: {}, response: {}", safeUrl, maskSensitiveContent(bodyStr));
            resultMap = JSONUtil.toBean(bodyStr, ShopeeResponse.class);
        } catch (Exception e) {
            log.error("虾皮接口请求异常, method: GET, url: {}, 错误: {}", safeUrl, e.getMessage(), e);
            throw new ServiceException("虾皮接口请求失败");
        }

        return requireResponse(resultMap, "虾皮接口响应为空");
    }
    /**
     * 发送请求到沃尔玛获取令牌token
     *
     * @param baseUrl 接口地址
     * @param params
     * @return java.lang.String
     */
    public static ShopeeAuth sendAuthPost(String baseUrl, Map<String, Object> urlParams, Map<String, Object> params) {
        Map<String, String> headers = new HashMap();
        headers.put(CONTENT_TYPE, APPLICATION);
        headers.put("Accept", APPLICATION);
        String url = buildUrl(baseUrl, urlParams);
        String safeUrl = buildSafeUrl(baseUrl, urlParams);
        log.info("虾皮接口请求, method: POST, url: {}, body: {}", safeUrl, maskLogBody(params));
        ShopeeAuth resultMap = null;
        try {
            String bodyStr = OkHttpUtils.doPostJson(url, params, headers);
            log.info("虾皮接口响应, method: POST, url: {}, response: {}", safeUrl, maskSensitiveContent(bodyStr));
            resultMap = JSONUtil.toBean(bodyStr, ShopeeAuth.class);
        } catch (Exception e) {
            log.error("虾皮接口请求异常, method: POST, url: {}, 错误: {}", safeUrl, e.getMessage(), e);
            throw new ServiceException("虾皮授权接口请求失败");
        }
        return requireResponse(resultMap, "虾皮授权接口响应为空");
    }

    /**
     * 发送请求到沃尔玛获取令牌token
     *
     * @param baseUrl 接口地址
     * @param params
     * @return java.lang.String
     */
    public static ShopeeResponse sendPost(String baseUrl, Map<String, Object> urlParams, Map<String, Object> params) {
        ShopeeResponse resultMap = null;
        Map<String, String> headers = new HashMap();
        headers.put(CONTENT_TYPE, APPLICATION);
        headers.put("Accept", APPLICATION);
        String url = buildUrl(baseUrl, urlParams);
        String safeUrl = buildSafeUrl(baseUrl, urlParams);
        log.info("虾皮接口请求, method: POST, url: {}, body: {}", safeUrl, maskLogBody(params));
        try {
            String bodyStr = OkHttpUtils.doPostJson(url, params, headers);
            log.info("虾皮接口响应, method: POST, url: {}, response: {}", safeUrl, maskSensitiveContent(bodyStr));
            resultMap = JSONUtil.toBean(bodyStr, ShopeeResponse.class);
        } catch (Exception e) {
            log.error("虾皮接口请求异常, method: POST, url: {}, 错误: {}", safeUrl, e.getMessage(), e);
            throw new ServiceException("虾皮接口请求失败");
        }
        return requireResponse(resultMap, "虾皮接口响应为空");
    }

    /**
     * 发送请求到沃尔玛获取令牌token
     *
     * @param baseUrl 接口地址
     * @param params
     * @return java.lang.String
     */
    public static ShopeeTokenAuth sendRefreshPost(String baseUrl, Map<String, Object> urlParams, Map<String, Object> params) {
        ShopeeTokenAuth resultMap = null;
        Map<String, String> headers = new HashMap();
        headers.put(CONTENT_TYPE, APPLICATION);
        headers.put("Accept", APPLICATION);
        String url = buildUrl(baseUrl, urlParams);
        String safeUrl = buildSafeUrl(baseUrl, urlParams);
        log.info("虾皮接口请求, method: POST, url: {}, body: {}", safeUrl, maskLogBody(params));
        try {
            String bodyStr = OkHttpUtils.doPostJson(url, params, headers);
            log.info("虾皮接口响应, method: POST, url: {}, response: {}", safeUrl, maskSensitiveContent(bodyStr));
            resultMap = JSONUtil.toBean(bodyStr, ShopeeTokenAuth.class);
        } catch (Exception e) {
            log.error("虾皮接口请求异常, method: POST, url: {}, 错误: {}", safeUrl, e.getMessage(), e);
            throw new ServiceException("虾皮刷新授权接口请求失败");
        }

        return requireResponse(resultMap, "虾皮刷新授权接口响应为空");
    }
    /**
     * 虾皮标记发货 post请求
     *
     * @param baseUrl 接口地址
     * @param paramsJson
     * @return java.lang.String
     */
    public static ShopeeResponse sendPost(String baseUrl, Map<String, Object> urlParams, String paramsJson) {
        ShopeeResponse resultMap = null;
        Map<String, String> headers = new HashMap();
        headers.put(CONTENT_TYPE, APPLICATION);
        headers.put("Accept", APPLICATION);
        String url = buildUrl(baseUrl, urlParams);
        String safeUrl = buildSafeUrl(baseUrl, urlParams);
        log.info("虾皮接口请求, method: POST, url: {}, body: {}", safeUrl, maskLogBody(paramsJson));
        try {
            String bodyStr = OkHttpUtils.doPostJson(url, paramsJson, headers);
            log.info("虾皮接口响应, method: POST, url: {}, response: {}", safeUrl, maskSensitiveContent(bodyStr));
            resultMap = JSONUtil.toBean(bodyStr, ShopeeResponse.class);
        } catch (Exception e) {
            log.error("虾皮接口请求异常, method: POST, url: {}, 错误: {}", safeUrl, e.getMessage(), e);
            throw new ServiceException("虾皮接口请求失败");
        }
        return requireResponse(resultMap, "虾皮接口响应为空");
    }

    private static <T> T requireResponse(T response, String message) {
        if (response == null) {
            throw new ServiceException(message);
        }
        return response;
    }

    private static String buildSafeUrl(String url, Map<String, Object> urlParams) {
        return maskSensitiveContent(buildUrl(url, maskParams(urlParams)));
    }

    private static Object maskLogBody(Object body) {
        if (body == null) {
            return null;
        }
        if (body instanceof Map) {
            return maskParams((Map<?, ?>) body);
        }
        if (body instanceof Iterable) {
            return maskIterable((Iterable<?>) body);
        }
        return maskSensitiveContent(String.valueOf(body));
    }

    private static Map<String, Object> maskParams(Map<?, ?> params) {
        if (params == null || params.isEmpty()) {
            return null;
        }
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : params.entrySet()) {
            String key = String.valueOf(entry.getKey());
            result.put(key, maskValue(key, entry.getValue()));
        }
        return result;
    }

    private static List<Object> maskIterable(Iterable<?> iterable) {
        List<Object> result = new ArrayList<>();
        for (Object item : iterable) {
            result.add(maskValue(null, item));
        }
        return result;
    }

    private static Object maskValue(String key, Object value) {
        if (isSensitiveKey(key)) {
            return MASK;
        }
        if (value instanceof Map) {
            return maskParams((Map<?, ?>) value);
        }
        if (value instanceof Iterable) {
            return maskIterable((Iterable<?>) value);
        }
        if (value instanceof String) {
            return maskSensitiveContent((String) value);
        }
        return value;
    }

    private static boolean isSensitiveKey(String key) {
        return key != null && SENSITIVE_KEYS.contains(key.toLowerCase());
    }

    private static String maskSensitiveContent(String content) {
        if (content == null) {
            return null;
        }
        String result = content;
        for (MaskRule rule : SENSITIVE_MASK_RULES) {
            result = rule.pattern.matcher(result).replaceAll(rule.replacement);
        }
        return result;
    }

    private static List<MaskRule> buildSensitiveMaskRules() {
        List<MaskRule> rules = new ArrayList<>();
        for (String key : SENSITIVE_KEYS) {
            String quotedKey = Pattern.quote(key);
            rules.add(new MaskRule(Pattern.compile("(?i)(\"" + quotedKey + "\"\\s*:\\s*\")([^\"]*)(\")"), "$1" + MASK + "$3"));
            rules.add(new MaskRule(Pattern.compile("(?i)(\"" + quotedKey + "\"\\s*:\\s*)([^,}\\]]+)"), "$1\"" + MASK + "\""));
            rules.add(new MaskRule(Pattern.compile("(?i)([?&]" + quotedKey + "=)([^&\\s]+)"), "$1" + MASK));
            rules.add(new MaskRule(Pattern.compile("(?i)(^" + quotedKey + "=)([^&\\s]+)"), "$1" + MASK));
        }
        return rules;
    }

    private static class MaskRule {
        private final Pattern pattern;
        private final String replacement;

        private MaskRule(Pattern pattern, String replacement) {
            this.pattern = pattern;
            this.replacement = replacement;
        }
    }

    public static String buildUrl(String url, Map<String, Object> urlParams) {
        StringBuilder urlBuilder = new StringBuilder(url);
        if (urlParams != null && urlParams.size() > 0) {
            int i = 1;
            for (Map.Entry<String, Object> entry : urlParams.entrySet()) {
                if (1 == i) {
                    urlBuilder.append("?" + entry.getKey() + "=" + entry.getValue());
                    i += 1;
                } else {
                    urlBuilder.append("&" + entry.getKey() + "=" + entry.getValue());
                }

            }
        }
        return urlBuilder.toString();
    }

    public static void main(String[] args) {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        jsonArray.add("123");
        jsonArray.add("1234");
        jsonArray.add("1235");
        jsonArray.add("1236");
        jsonObject.put("merchant_id_list", jsonArray);

        ShopeeAuth resultMap = JSONUtil.toBean(jsonObject, ShopeeAuth.class);
        System.out.println(JSONUtil.toJsonStr(resultMap));
    }
}
