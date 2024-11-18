package com.sdk.third.lingxing.utils;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.utils.RedisUtil;
import com.common.core.exception.ServiceException;
import com.sdk.third.lingxing.core.Config;
import com.sdk.third.lingxing.core.HttpMethod;
import com.sdk.third.lingxing.core.HttpRequest;
import com.sdk.third.lingxing.core.HttpResponse;
import com.sdk.third.lingxing.dto.FbaReceiveReqDTO;
import com.sdk.third.lingxing.dto.FbaShipmentReceiveDTO;
import com.sdk.third.lingxing.dto.Result;
import com.sdk.third.lingxing.dto.ShopInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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


    // 查询亚马逊店铺列表
    public static final String SHOP_LIST_URI = "erp/sc/data/seller/lists";
    // 查询FBA货件
    public static final String FBA_SHIPMENT_LIST_RUI = "erp/sc/data/fba_report/shipmentList";
    // FBA货件签收明细列表
    public static final String FBA_SHIPMENT_DETAIL_RUI = "erp/sc/data/fba_report/receivedInventory";

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
    public void setRedisUtil(RedisUtil redisUtil) {
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
    public static Result postAndSign(String path, TreeMap<String, Object> requestBody) {
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
            Result result = execute.readEntity(Result.class);
            log.debug("Post请求领星接口:路径={}, 参数={}, 结果={}", path, JSONUtil.toJsonStr(requestBody), JSONUtil.toJsonStr(result));
            return result;
        } catch (Exception e) {
            String errorMsg = StrUtil.format("Post请求领星接口失败:path={}, error={}", path, ExceptionUtil.stacktraceToString(e, 2000));
            log.error(errorMsg);
            throw new ServiceException(errorMsg);
        }
    }

    /**
     * 添加通用参数并签名和post请求请求参数
     */
    public static Result postAndSign(String path, Map<String, Object> requestBody) {
        return postAndSign(path, new TreeMap<>(requestBody));
    }

    /**
     * 添加通用参数并签名和get请求请求参数
     */
    public static <T> Result<T> getAndSign(String path, TreeMap<String, Object> requestBody) {
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
     * 添加通用参数并签名和get请求请求参数
     */
    public static <T> Result<T> getAndSign(String path, Map<String, Object> requestBody) {
        return getAndSign(path, new TreeMap<>(requestBody));
    }

    /**
     * 组合请求参数并生成签名
     */
    public static TreeMap<String, Object> combineQueryParams(TreeMap<String, Object> requestBody) {
        // 获取访问token
        String accessToken = getAccessToken();
        // 组合请求参数
        TreeMap<String, Object> queryParam = new TreeMap<>();
        queryParam.put("timestamp", System.currentTimeMillis() / 1000 + "");
        queryParam.put("access_token", accessToken);
        queryParam.put("app_key", APP_ID);

        TreeMap<String, Object> signMap = new TreeMap<>();
        signMap.putAll(queryParam);
        if (!CollectionUtils.isEmpty(requestBody)) {
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
    public static String getAccessToken() {
        // 访问令牌key
        String tokenKey = StrUtil.format(RedisCacheConstants.REDIS_PLATFORM_TOKEN, "lingxing", APP_ID);
        // 缓存获取
        Object tokenObj = redisUtil.get(tokenKey);
        if (null != tokenObj) {
            return tokenObj.toString();
        }
        // 检查配置参数
        checkConfig();

        try {
            // 请求授权
            Result<?> sourceResult = AKRestClientBuild.builder().endpoint(ENDPOINT).getAccessToken(APP_ID, APP_SECRET);
            if (null == sourceResult) {
                throw new ServiceException("请求领星授权接口失败：响应未空");
            }
            if (!"200".equals(sourceResult.getCode())) {
                throw new ServiceException("请求领星授权接口失败：result=" + JSONUtil.toJsonStr(sourceResult));
            }
            JSONObject jsonObject = new JSONObject(sourceResult.getData());
            String expiresIn = jsonObject.getStr("expires_in");
            String accessToken = jsonObject.getStr("access_token");
            // 缓存到redis
            redisUtil.set(tokenKey, accessToken, Long.parseLong(expiresIn) - 1L);
            return accessToken;
        } catch (Exception e) {
            log.error("请求领星授权接口失败: error={}", ExceptionUtil.stacktraceToString(e, 2000));
            throw new ServiceException("请求领星授权接口失败：error=" + ExceptionUtil.stacktraceToString(e, 1000));
        }
    }

    private static void checkConfig() {
        if (StringUtils.isBlank(ENDPOINT)) {
            throw new ServiceException("领星配置参数为空");
        }
    }

    /**
     * 获取领星店铺列表
     */
    public static List<ShopInfoDTO> getAllShopList() {
        Result<Object> result = LingxingApiUtils.getAndSign(LingxingApiUtils.SHOP_LIST_URI, new TreeMap<>());
        if (!"0".equalsIgnoreCase(result.getCode())) {
            String errorMsg = StrUtil.format("请求领星商店列表失败:, result={}", JSONUtil.toJsonStr(result));
            log.error(errorMsg);
            throw new ServiceException(errorMsg);
        }
        return JSONUtil.toList(JSONUtil.toJsonStr(result.getData()), ShopInfoDTO.class);
    }


    /**
     * 根据领星店铺ID和签收日期获取所有货件签收明细列表
     *
     * @param sid          领星店铺ID
     * @param receivedDate 签收日期
     * @return 所有货件签收明细列表
     */
    public static List<FbaShipmentReceiveDTO> getAllReceivedInventory(Integer sid, LocalDate receivedDate) {
        FbaReceiveReqDTO receivedDTO = new FbaReceiveReqDTO(sid, receivedDate);
        Result<List<Object>> firstResult = getReceivedInventory(receivedDTO);
        if (CollectionUtils.isEmpty(firstResult.getData())) {
            return Collections.emptyList();
        }
        if (firstResult.getTotal() <= 1000) {
            return JSONUtil.toList(JSONUtil.toJsonStr(firstResult.getData()), FbaShipmentReceiveDTO.class);
        }
        List<Object> resultList = firstResult.getData();
        int count = firstResult.getTotal() / 1000;
        for (int offset = 1; offset < count; offset++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error("拉取领星货件签收明细数据睡眠异常:e={}", ExceptionUtil.stacktraceToString(e));
                Thread.currentThread().interrupt();
            }
            FbaReceiveReqDTO currentReceivedDTO = new FbaReceiveReqDTO(sid, receivedDate, offset);
            Result<List<Object>> currentResult = getReceivedInventory(currentReceivedDTO);
            if (!CollectionUtils.isEmpty(currentResult.getData())) {
                resultList.addAll(currentResult.getData());
            }
        }
        return JSONUtil.toList(JSONUtil.toJsonStr(resultList), FbaShipmentReceiveDTO.class);
    }

    /**
     * 根据领星店铺ID和签收日期，分页参数获取货件签收明细分页
     *
     * @param receivedDTO 请求参数
     * @return 当前分页结果
     */
    public static Result<List<Object>> getReceivedInventory(FbaReceiveReqDTO receivedDTO) {
        Map<String, Object> objectMap = BeanUtil.beanToMap(receivedDTO);
        Result<List<Object>> result = LingxingApiUtils.postAndSign(LingxingApiUtils.FBA_SHIPMENT_DETAIL_RUI, objectMap);
        if (!"0".equalsIgnoreCase(result.getCode())) {
            String errorMsg = StrUtil.format("请求领星FBA货件签收明细列表失败:,sid={}, result={}", receivedDTO.getSid(), JSONUtil.toJsonStr(result));
            log.error(errorMsg);
            throw new ServiceException(errorMsg);
        }
        return result;
    }
}
