package com.sdk.third.lingxing.utils;


import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.utils.RedisUtil;
import com.common.core.exception.ServiceException;
import com.sdk.third.lingxing.core.Config;
import com.sdk.third.lingxing.core.HttpMethod;
import com.sdk.third.lingxing.core.HttpRequest;
import com.sdk.third.lingxing.core.HttpResponse;
import com.sdk.third.lingxing.dto.Result;
import com.sdk.third.lingxing.dto.Token;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.TreeMap;

/**
 * 领星API 工具类
 *
 * @author Jim
 * @since 2024-02-05
 */
@Slf4j
@Component
public class LingxingApiUtils {

    // 授权获取 access-token和refresh-token
    public static final String AUTH_URI = "/api/auth-server/oauth/access-token";
    // 查询亚马逊店铺列表
    public static final String SHOP_LIST_URI = "/erp/sc/data/seller/lists";
    // 查询FBA货件
    public static final String FBA_SHIPMENT_LIST_RUI = "/erp/sc/data/fba_report/shipmentList";

    // FBA货件签收明细列表
    public static final String FBA_SHIPMENT_DETAIL_RUI = "/erp/sc/data/fba_report/receivedInventory";

    /**
     * 接口域名
     */
    private static String ENDPOINT;
    /**
     * appId
     */
    private static String APP_ID;
    /**
     * app密钥
     */
    private static String APP_SECRET;
    /**
     * redis工具类
     */
    private static RedisUtil redisUtil;

    @Resource
    public void setRedisUtil(RedisUtil redisUtil){
        LingxingApiUtils.redisUtil = redisUtil;
    }

    @Value("${openApi.lingxing.endpoint:}")
    public void setEndpoint(String endpoint) {
        LingxingApiUtils.ENDPOINT = endpoint;
    }

    @Value("${openApi.lingxing.appId:}")
    public void setAppId(String appId) {
        LingxingApiUtils.APP_ID = appId;
    }

    @Value("${openApi.lingxing.appSecret:}")
    public void setAppSecret(String appSecret) {
        LingxingApiUtils.APP_SECRET = appSecret;
    }

    /**
     * 添加通用参数并签名和post请求请求参数
     */
    private static <T> Result<T> postAndSign(String path, TreeMap<String, Object> requestBody) {
        // 组合请求参数并生成签名
        TreeMap<String, Object> queryParam = combineQueryParams(requestBody);
        // 构建请求
        HttpRequest<Result> build = HttpRequest.builder(Result.class)
                .method(HttpMethod.POST)
                .endpoint(ENDPOINT)
                .path(path)
                .queryParams(queryParam)
                .json(JSONUtil.toJsonStr(requestBody))
                .config(Config.DEFAULT.withConnectionTimeout(30000).withReadTimeout(30000))
                .build();
        try (HttpResponse execute = HttpExecutor.create().execute(build)) {
            Result<T> result = execute.readEntity(Result.class);
            log.debug("Post请求领星接口:路径={}, 参数={}, 结果={}", path, JSONUtil.toJsonStr(requestBody), JSONUtil.toJsonStr(result));
            return result;
        } catch (Exception e) {
            String errorMsg = StrUtil.format("Post请求领星接口失败:path={}, error={}", path, ExceptionUtil.stacktraceToString(e, 2000));
            log.error(errorMsg);
            throw new ServiceException(errorMsg);
        }
    }

    /**
     * 添加通用参数并签名和get请求请求参数
     */
    private static <T> Result<T> getAndSign(String path, TreeMap<String, Object> requestBody) {
        // 组合请求参数并生成签名
        TreeMap<String, Object> queryParam = combineQueryParams(requestBody);
        // 构建请求
        HttpRequest<Result> build = HttpRequest.builder(Result.class)
                .method(HttpMethod.GET)
                .endpoint(ENDPOINT)
                .path(path)
                .queryParams(queryParam)
                .config(Config.DEFAULT.withConnectionTimeout(30000).withReadTimeout(30000))
                .build();
        try (HttpResponse execute = HttpExecutor.create().execute(build)) {
            Result<T> result = execute.readEntity(Result.class);
            log.debug("Get请求领星接口:路径={}, 参数={}, 结果={}", path, JSONUtil.toJsonStr(requestBody), JSONUtil.toJsonStr(result));
            return result;
        } catch (Exception e) {
            String errorMsg = StrUtil.format("Post请求领星接口失败:path={}, error={}", path, ExceptionUtil.stacktraceToString(e, 2000));
            log.error(errorMsg);
            throw new ServiceException(errorMsg);
        }
    }

    /**
     * 组合请求参数并生成签名
     */
    private static TreeMap<String, Object> combineQueryParams(TreeMap<String, Object> requestBody) {
        // 获取访问token
        String accessToken = getAccessToken();
        // 组合请求参数
        TreeMap<String, Object> queryParam = new TreeMap<>();
        queryParam.put("timestamp", System.currentTimeMillis() / 1000 + "");
        queryParam.put("access_token", accessToken);
        queryParam.put("app_key", APP_ID);

        TreeMap<String, Object> signMap = new TreeMap<>();
        signMap.putAll(queryParam);
        if (!CollectionUtils.isEmpty(requestBody)){
            signMap.putAll(requestBody);
        }
        // 生成签名
        String sign = LingxingApiSignUtils.sign(signMap, APP_ID);
        queryParam.put("sign", sign);
        log.debug("领星签名：sign={}", sign);
        return queryParam;
    }


    /**
     * 请求或缓存获取访问接口的AccessToken
     */
    private static String getAccessToken() {
        // 访问令牌key
        String tokenKey = StrUtil.format(RedisCacheConstants.REDIS_PLATFORM_TOKEN, "lingxing", APP_ID);
        // 缓存获取
        Object tokenObj = redisUtil.get(tokenKey);
        if (null != tokenObj){
            return tokenObj.toString();
        }
        Result<Token> result;
        try {
            result = AKRestClientBuild.builder().endpoint(ENDPOINT).getAccessToken(APP_ID, APP_SECRET);
            if (null == result){
                throw new ServiceException("请求领星授权接口失败：响应未空");
            }
            if (!"200".equals(result.getCode())){
                throw new ServiceException("请求领星授权接口失败：result="+ JSONUtil.toJsonStr(result));
            }
            String expiresIn = result.getData().getExpiresIn();
            String accessToken = result.getData().getAccessToken();
            // 缓存到redis
            redisUtil.set(tokenKey, accessToken, Integer.parseInt(expiresIn) - 1);
            return tokenKey;
        } catch (Exception e) {
            log.error("请求领星授权接口失败: error={}", ExceptionUtil.stacktraceToString(e, 2000));
            throw new ServiceException("请求领星授权接口失败：error=" + ExceptionUtil.stacktraceToString(e, 1000));
        }
    }
}
