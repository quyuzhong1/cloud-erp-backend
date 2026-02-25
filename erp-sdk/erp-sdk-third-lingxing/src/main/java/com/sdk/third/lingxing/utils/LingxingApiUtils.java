package com.sdk.third.lingxing.utils;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.utils.RedisUtil;
import com.common.core.exception.ServiceException;
import com.sdk.third.lingxing.core.Config;
import com.sdk.third.lingxing.core.HttpMethod;
import com.sdk.third.lingxing.core.HttpRequest;
import com.sdk.third.lingxing.core.HttpResponse;
import com.sdk.third.lingxing.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 领星API 工具类
 *
 * @author Jim
 * @since 2024-02-05
 */
@Slf4j
@Component
public class LingxingApiUtils {


    // 领星ERP相关接口
    // 查询亚马逊店铺列表
    public static final String SHOP_LIST_URI = "erp/sc/data/seller/lists";
    // 查询FBA货件
    public static final String FBA_SHIPMENT_LIST_RUI = "erp/sc/data/fba_report/shipmentList";
    // FBA货件签收明细列表
    public static final String FBA_SHIPMENT_DETAIL_RUI = "erp/sc/data/fba_report/receivedInventory";
    // 查询仓库列表
    public static final String WAREHOUSE_URI = "erp/sc/data/local_inventory/warehouse";


    // 领星多平台相关接口
    // 快速出库
    public static final String FAST_OUTBOUND_URI = "pb/mp/order/v2/fastOutbound";
    // 标记订单不发货
    public static final String CANCEL_ORDER_URI = "pb/mp/order/v2/cancelOrder";
    // 编辑/更新自发货订单
    public static final String UPDATE_ORDER_URI = "pb/mp/order/v2/updateOrder";

    // 添加/编辑本地产品
    public static final String PRODUCT_SET_URI = "erp/sc/routing/storage/product/set";
    // 查询本地产品列表
    public static final String PRODUCT_LIST_URI = "erp/sc/routing/data/local_inventory/productList";


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
    public static Result postAndSign(String path, TreeMap<String, Object> requestBody, boolean signParamListToStr) {
        // 组合请求参数并生成签名
        TreeMap<String, Object> queryParam = combineQueryParams(requestBody, signParamListToStr);

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
            log.warn("Post请求领星接口:路径={}, 参数={}, 结果={}", path, JSONUtil.toJsonStr(requestBody), JSONUtil.toJsonStr(result));
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
        return postAndSign(path, new TreeMap<>(requestBody), false);
    }

    /**
     * 添加通用参数并签名和get请求请求参数
     */
    public static <T> Result<T> getAndSign(String path, TreeMap<String, Object> requestBody) {
        // 组合请求参数并生成签名
        TreeMap<String, Object> queryParam = combineQueryParams(requestBody, false);
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
            log.warn("Get请求领星接口:路径={}, 参数={}, 结果={}", path, JSONUtil.toJsonStr(requestBody), JSONUtil.toJsonStr(result));
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
    public static TreeMap<String, Object> combineQueryParams(TreeMap<String, Object> requestBody, boolean signParamListToStr) {
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
            if (signParamListToStr) {
                TreeMap<String, String> resultRequestBody = convertToTreeMapString(requestBody);
                signMap.putAll(resultRequestBody);
            } else {
                signMap.putAll(requestBody);
            }
        }
        // 生成签名
        String sign = LingxingApiSignUtils.sign(signMap, APP_ID);
        queryParam.put("sign", sign);
        log.debug("领星签名：sign={}", sign);
        return queryParam;
    }

    /**
     * 转换TreeMap<String, Object>为TreeMap<String, String>
     */
    private static TreeMap<String, String> convertToTreeMapString(TreeMap<String, Object> requestBody) {
        TreeMap<String, String> result = new TreeMap<>();
        for (Map.Entry<String, Object> entry : requestBody.entrySet()) {
            // 将 Object 转为 String，处理 null 情况
            if (null == entry.getValue()) {
                continue;
            }
            Object valueObj = entry.getValue();
            String value;
            if (valueObj instanceof List) {
                // 数组转List
                value = JSONUtil.toJsonStr(entry.getValue());
            } else {
                value = entry.getValue().toString();
            }
            result.put(entry.getKey(), value);
        }
        return result;
    }

    /**
     * 更新-生成签名和post请求
     * <a href="https://apidoc.lingxing.com/#/docs/Guidance/QA?id=_9-%e5%8f%82%e6%95%b0%e4%b8%8d%e5%90%88%e6%b3%95">调整原因</a>
     * 原因：数组、List集合在生成sign过程中会将其数据类型转为string类型后传入body参数当中，而转义后的数据不符合接口文档传入参数类型的规范
     * 解决方法：在生成sign之后，添加一段代码将body参数中对应的值重新以本身数组或List集合类型进行覆盖即可
     */
    public static Result postAndSignCheckListConvert(String path, Map<String, Object> requestBody) {
        return postAndSign(path, new TreeMap<>(requestBody), true);
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

        return refreshToken();
    }

    /**
     * 刷新领星授权token
     */
    public static String refreshToken() {
        try {
            // 访问令牌key
            String tokenKey = StrUtil.format(RedisCacheConstants.REDIS_PLATFORM_TOKEN, "lingxing", APP_ID);
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
    @Deprecated
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

    /**
     * 请求领星接口
     */
    public static Result<Object> postRequestData(String apiType, TreeMap<String, Object> requestMap) {
        Result<Object> result = LingxingApiUtils.postAndSign(apiType, requestMap);
        if (!"0".equalsIgnoreCase(result.getCode()) && !"3001008".equalsIgnoreCase(result.getCode()) && !"2001005".equalsIgnoreCase(result.getCode())) {
            String errorMsg = StrUtil.format("请求领星{}接口:, result={}", apiType, JSONUtil.toJsonStr(result));
            log.error(errorMsg);
            throw new ServiceException(errorMsg);
        }
        return result;
    }

    /**
     * 请求领星接口
     */
    public static Result<Object> postRequestDataAndRetry(String apiType, TreeMap<String, Object> requestMap) {
        Result<Object> resultData = null;
        long sleepTime = 1000;
        // 限流最多请求10次
        for (int count = 1; count <= 10; count++) {
            resultData = LingxingApiUtils.postRequestData(apiType, requestMap);
            // 限流重试
            if ("3001008".equalsIgnoreCase(resultData.getCode())) {
                if (10 == count) {
                    throw new ServiceException("调用领星接口重试" + count + "失败：" + apiType);
                }
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    log.error("拉取调用领星接口重试睡眠异常:e={}", ExceptionUtil.stacktraceToString(e));
                    Thread.currentThread().interrupt();
                }
                sleepTime = sleepTime + 1000;
                count = count + 1;
            } else if ("2001005".equalsIgnoreCase(resultData.getCode())) {
                // 领星发版token失效刷新
                if (10 == count) {
                    throw new ServiceException("调用领星接口重试" + count + "失败：" + apiType);
                }
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    log.error("拉取调用领星接口授权失效重试睡眠异常:e={}", ExceptionUtil.stacktraceToString(e));
                    Thread.currentThread().interrupt();
                }
                // 刷新授权
                LingxingApiUtils.refreshToken();
                sleepTime = sleepTime + 1000;
                count = count + 1;
            } else {
                break;
            }
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            log.error("调用领星接口重试睡眠异常:e={}", ExceptionUtil.stacktraceToString(e));
            Thread.currentThread().interrupt();
        }
        return resultData;
    }


    /**
     * 标记订单不发货
     *
     * @param orderList 领星订单ID
     * @return 响应
     */
    public static Result<Object> cancelOrderByOrderList(List<String> orderList) {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("order_list", orderList);
        Result<Object> result = LingxingApiUtils.postAndSignCheckListConvert(LingxingApiUtils.CANCEL_ORDER_URI, requestMap);
        if (!"0".equalsIgnoreCase(result.getCode())) {
            String errorMsg = StrUtil.format("请求领星标记订单不发货失败:,order_list={}, result={}", orderList, JSONUtil.toJsonStr(result));
            log.error(errorMsg);
            throw new ServiceException(errorMsg);
        }
        return result;
    }


    /**
     * 快速出库
     *
     * @param packageList 每个单对应出库信息-最多1000个订单
     * @return 响应
     */
    public static Result<Object> fastOutbound(List<OrderFastOutboundPackageDTO.PackageInfo> packageList) {
        List<Map<String, Object>> itemMap = packageList.stream()
                .map(e -> new TreeMap<>(BeanUtil.beanToMap(e, true, true)))
                .collect(Collectors.toList());
        TreeMap<String, Object> requestMap = new TreeMap<>();
        requestMap.put("package", itemMap);
        Result<Object> result = LingxingApiUtils.postAndSignCheckListConvert(LingxingApiUtils.FAST_OUTBOUND_URI, requestMap);
        if (!"0".equalsIgnoreCase(result.getCode())) {
            String errorMsg = StrUtil.format("请求领星标记订单快速出库失败:,request={}, result={}", requestMap, JSONUtil.toJsonStr(result));
            log.error(errorMsg);
            throw new ServiceException(errorMsg);
        }
        return result;
    }


    /**
     * 编辑/更新自发货订单
     *
     * @param orderList 更新的订单信息
     * @return 响应
     */
    public static Result<Object> updateOrder(List<UpdateOrderDTO.OrderInfo> orderList) {
        Map<String, Object> requestMap = new HashMap<>();
        List<Map<String, Object>> dataMap = orderList.stream()
                .map(e -> new TreeMap<>(BeanUtil.beanToMap(e, true, true)))
                .collect(Collectors.toList());
        requestMap.put("order_list", dataMap);
        log.error("领星更新订单传参,{}", JSONUtil.toJsonStr(requestMap));
        Result<Object> result = LingxingApiUtils.postAndSignCheckListConvert(LingxingApiUtils.UPDATE_ORDER_URI, requestMap);
        if ("10000".equalsIgnoreCase(result.getCode())) {
            String errorMsg = StrUtil.format("请求领星编辑/更新自发货订单失败:,request={}, result={}", requestMap, JSONUtil.toJsonStr(result));
            log.error(errorMsg);
            throw new ServiceException(errorMsg);
        }
        return result;
    }


    /**
     * 添加/编辑本地产品
     *
     * @param productInfo 商品信息
     * @return 响应
     */
    public static Result<Object> checkAddOrUpdateProduct(ProductInfo productInfo) {
//        List<String> skuIdentifierList = queryLxExistSkuNoList(Collections.singletonList(productInfo.getSkuIdentifier()));
//        // 更新sku识别码不允许传
//        if (!CollectionUtils.isEmpty(skuIdentifierList)){
//            productInfo.setSkuIdentifier(null);
//        }
        TreeMap<String, Object> requestMap = new TreeMap<>(BeanUtil.beanToMap(productInfo, true, true));
        Result<Object> result = LingxingApiUtils.postAndSignCheckListConvert(LingxingApiUtils.PRODUCT_SET_URI, requestMap);
        if (!"0".equalsIgnoreCase(result.getCode())) {
            String errorMsg = StrUtil.format("请求领星添加/编辑本地产品失败:,request={}, result={}", requestMap, JSONUtil.toJsonStr(result));
            log.error(errorMsg);
            throw new ServiceException(errorMsg);
        }
        return result;
    }

    /**
     * 领星SKU转换
     * 只允许为:字母,数字,下划线(),短划线(-),英文点(.),并号(#)不限制大小写
     */
    public static String convertLxSku(String sku) {
        return CharSequenceUtil.replace(sku, "+", "-").replace("*", "#");
    }

    /**
     * 替换连续的空格
     */
    public static String convertLxProductName(String name) {
        return name.replaceAll("\\s+", " ");
    }


    /**
     * 公共推送
     */
    public static Result<Object> commonSync(String apiUri, Object requestObj) {
        TreeMap<String, Object> requestMap = new TreeMap<>(BeanUtil.beanToMap(requestObj, true, true));
        return LingxingApiUtils.postAndSignCheckListConvert(apiUri, requestMap);
    }

    /**
     * 检查sku未同步领星
     */
    public static void checkSkuSyncLx(List<String> skuNoList) {
        List<String> skuIdentifierList = queryLxExistSkuNoList(skuNoList);
        boolean match = skuNoList.stream().anyMatch(e -> !skuIdentifierList.contains(e));
        if (match){
            ServiceException.runError("存在SKU未同步领星");
        }
    }


    /**
     * 查询领星已存在的sku
     */
    public static List<String> queryLxExistSkuNoList(List<String> skuNoList) {
        TreeMap<String, Object> treeMap = new TreeMap<>();
        treeMap.put("sku_list", skuNoList);
        Result<Object> result = LingxingApiUtils.postAndSignCheckListConvert(LingxingApiUtils.PRODUCT_LIST_URI, treeMap);
        if (!"0".equalsIgnoreCase(result.getCode())){
            log.error("查询SKU是否同步领星失败:{}", JSONUtil.toJsonStr(result));
            ServiceException.runError("查询SKU是否同步领星失败："+ JSONUtil.toJsonStr(result));
        }
        if (null == result.getData()){
            log.error("查询到SKU未同步领星:{}", JSONUtil.toJsonStr(result));
            ServiceException.runError("SKU未同步领星");
        }
        JSONArray jsonArray = JSONArray.parseArray(JSON.toJSONString(result.getData()));
        return jsonArray.stream()
                .map(e -> ((com.alibaba.fastjson.JSONObject) e).getString("sku")).collect(Collectors.toList());
    }

    /**
     * 添加本地产品
     *
     * @param productInfo 商品信息
     * @return 响应
     */
    public static Result<Object> addProduct(ProductInfo productInfo) {
//        if (StringUtils.isBlank(productInfo.getSkuIdentifier())){
//            ServiceException.runError("添加时识别码必传");
//        }
        TreeMap<String, Object> requestMap = new TreeMap<>(BeanUtil.beanToMap(productInfo, true, true));
        Result<Object> result = LingxingApiUtils.postAndSignCheckListConvert(LingxingApiUtils.PRODUCT_SET_URI, requestMap);
        if (!"0".equalsIgnoreCase(result.getCode())) {
            String errorMsg = StrUtil.format("请求领星添加/编辑本地产品失败:,request={}, result={}", requestMap, JSONUtil.toJsonStr(result));
            log.error(errorMsg);
            throw new ServiceException(errorMsg);
        }
        return result;
    }

    /**
     * 编辑本地产品
     *
     * @param productInfo 商品信息
     * @return 响应
     */
    public static Result<Object> updateProduct(ProductInfo productInfo) {
        // 更新sku识别码不允许传
//        productInfo.setSkuIdentifier(null);
        TreeMap<String, Object> requestMap = new TreeMap<>(BeanUtil.beanToMap(productInfo, true, true));
        Result<Object> result = LingxingApiUtils.postAndSignCheckListConvert(LingxingApiUtils.PRODUCT_SET_URI, requestMap);
        if (!"0".equalsIgnoreCase(result.getCode())) {
            String errorMsg = StrUtil.format("请求领星添加/编辑本地产品失败:,request={}, result={}", requestMap, JSONUtil.toJsonStr(result));
            log.error(errorMsg);
            throw new ServiceException(errorMsg);
        }
        return result;
    }

}
