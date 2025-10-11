package com.sdk.oms.wildberries.service;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.common.core.utils.OkHttpUtils;
import com.sdk.oms.wildberries.constant.WildberriesConstant;
import com.sdk.oms.wildberries.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zdy
 * @ClassName WildberriesSDKService
 * @description: TODO
 * @date 2025年09月19日
 * @version: 1.0
 */
@Slf4j
@Component
public class WildberriesSDKService {

    public WildberriesResponse checkToken(String token) {
        log.error("接口请求：{}", JSONUtil.toJsonStr(token));
        String url = WildberriesConstant.GET_SHOP_CHECK_PING;
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", token);
        String bodyStr = OkHttpUtils.doGet(url, new HashMap<>(), headerMap);
        log.error("接口返回：{}", bodyStr);
        WildberriesResponse response = JSON.parseObject(JSONUtil.toJsonStr(bodyStr),new TypeReference<WildberriesResponse>() {}.getType());
        return response;
    }
    public String getProductCategory(String token) {
        log.error("接口请求：{}", JSONUtil.toJsonStr(token));
        String url = WildberriesConstant.GET_PRODUCT_CATEGORY;
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", token);
        headerMap.put("Content-Type", "application/json");
        headerMap.put("locale", "zh");
        String bodyStr = OkHttpUtils.doGet(url, new HashMap<>(), headerMap);
        log.error("接口返回：{}", bodyStr);
        return bodyStr;
    }
    public String getProductSubject(String token) {
        log.error("接口请求：{}", JSONUtil.toJsonStr(token));
        String url = WildberriesConstant.GET_PRODUCT_SUBJECT;
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", token);
        headerMap.put("Content-Type", "application/json");
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("locale", "zh");
        paramMap.put("limit", 1000);
        paramMap.put("offset", 0);
        String bodyStr = OkHttpUtils.doGet(url, paramMap, headerMap);
        log.error("接口返回：{}", bodyStr);
        return bodyStr;
    }
    public String getProductCharacteristic(String token, Integer subjectId) {
        log.error("接口请求：{}", JSONUtil.toJsonStr(token));
        String url = CharSequenceUtil.format(WildberriesConstant.GET_PRODUCT_CHARACTERISTIC, subjectId);
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", token);
        headerMap.put("Content-Type", "application/json");
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("locale", "zh");
        paramMap.put("limit", 1000);
        paramMap.put("offset", 0);
        String bodyStr = OkHttpUtils.doGet(url, paramMap, headerMap);
        log.error("接口返回：{}", bodyStr);
        return bodyStr;
    }

    /**
     * 创建订单
     * @param token
     * @param requestList
     * @return
     */
    public String createProduct(String token, List<CreateProductRequest> requestList) {
        log.error("接口请求：{}", JSONUtil.toJsonStr(requestList));
        String url = WildberriesConstant.POST_CREATE_PRODUCT_CARD;
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", token);
        headerMap.put("Content-Type", "application/json");
        headerMap.put("locale", "zh");
        String bodyStr = HttpRequest.post(url)
                .headerMap(headerMap, true)
                .body(JSONUtil.toJsonStr(requestList))
                .execute().body();
        log.error("接口返回：{}", bodyStr);
//        CreateSupplyResponse response = JSON.parseObject(JSONUtil.toJsonStr(bodyStr),new TypeReference<CreateSupplyResponse>() {}.getType());
        return bodyStr;
    }

    public SkuResponse getSkuList(String token, SkuRequest request) {
        log.error("接口请求：{}", JSONUtil.toJsonStr(request));
        String url = WildberriesConstant.POST_GET_SKU_LIST;
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", token);
        headerMap.put("Content-Type", "application/json");
        headerMap.put("locale", "zh");
        String bodyStr = OkHttpUtils.doPostJsonObject(url, request, headerMap);
        log.error("接口返回：{}", bodyStr);
        SkuResponse response = JSON.parseObject(JSONUtil.toJsonStr(bodyStr),new TypeReference<SkuResponse>() {}.getType());
        return response;
    }

    public OrderResponse getOrderNewList(String token, OrderRequest request) {
        log.error("接口请求：{}", JSONUtil.toJsonStr(request));
        String url = WildberriesConstant.GET_ORDERS_NEW;
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", token);
        headerMap.put("Content-Type", "application/json");
        headerMap.put("locale", "zh");
        Map<String, Object> paramMap = new HashMap<>();
//        paramMap.put("limit", request.getLimit());
//        paramMap.put("next", request.getNext());
//        paramMap.put("dateFrom", request.getDateFrom());
//        paramMap.put("dateTo", request.getDateTo());
        String bodyStr = OkHttpUtils.doGet(url, paramMap, headerMap);
        log.error("接口返回：{}", bodyStr);
        OrderResponse response = JSON.parseObject(JSONUtil.toJsonStr(bodyStr),new TypeReference<OrderResponse>() {}.getType());
        return response;
    }
    public OrderResponse getOrderList(String token, OrderRequest request) {
        log.error("接口请求：{}", JSONUtil.toJsonStr(request));
        String url = WildberriesConstant.GET_ORDERS;
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", token);
        headerMap.put("Content-Type", "application/json");
        headerMap.put("locale", "zh");
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("limit", request.getLimit());
        paramMap.put("next", request.getNext());
        paramMap.put("dateFrom", request.getDateFrom());
        paramMap.put("dateTo", request.getDateTo());
        String bodyStr = OkHttpUtils.doGet(url, paramMap, headerMap);
        log.error("接口返回：{}", bodyStr);
        OrderResponse response = JSON.parseObject(JSONUtil.toJsonStr(bodyStr),new TypeReference<OrderResponse>() {}.getType());
        return response;
    }
    /**
     * 创建订单
     * @param token
     * @param request
     * @return
     */
    public String createOrder(String token, CreateOrderRequest request) {
        log.error("接口请求：{}", JSONUtil.toJsonStr(request));
        String url = WildberriesConstant.POST_CREATE_FBS_ORDER;
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", token);
        headerMap.put("Content-Type", "application/json");
        headerMap.put("locale", "zh");
        String bodyStr = HttpRequest.post(url)
                .headerMap(headerMap, true)
                .body(JSONUtil.toJsonStr(request))
                .execute().body();
        log.error("接口返回：{}", bodyStr);
//        CreateSupplyResponse response = JSON.parseObject(JSONUtil.toJsonStr(bodyStr),new TypeReference<CreateSupplyResponse>() {}.getType());
        return bodyStr;
    }
    /**
     * 获取订单状态
     * @param token
     * @param request
     * @return
     */
    public OrderStatusResponse getOrderStatus(String token, OrderStatusRequest request) {
        log.error("接口请求：{}", JSONUtil.toJsonStr(request));
        String url = WildberriesConstant.POST_ORDERS_STATUS;
        String bodyStr = HttpRequest.post(url)
                .header("Authorization", token)
                .header("Content-Type", "application/json")
                .header("locale", "zh")
                        .body(JSONUtil.toJsonStr(request))
                                .execute().body();
        log.error("接口返回：{}", bodyStr);
        OrderStatusResponse response = JSON.parseObject(JSONUtil.toJsonStr(bodyStr),new TypeReference<OrderStatusResponse>() {}.getType());
        return response;
    }
    /**
     * 创建组包
     * @param token
     * @param request
     * @return
     */
    public CreateSupplyResponse createSupply(String token, CreateSupplyRequest request) {
        log.error("接口请求：{}", JSONUtil.toJsonStr(request));
        String url = WildberriesConstant.POST_CREATE_SUPPLY;
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", token);
        headerMap.put("Content-Type", "application/json");
        headerMap.put("locale", "zh");
        String bodyStr = HttpRequest.post(url)
                .headerMap(headerMap, true)
                .body(JSONUtil.toJsonStr(request))
                .execute().body();
        log.error("接口返回：{}", bodyStr);
        CreateSupplyResponse response = JSON.parseObject(JSONUtil.toJsonStr(bodyStr),new TypeReference<CreateSupplyResponse>() {}.getType());
        return response;
    }

    /**
     * 添加箱子到组包
     * @param token
     * @param supplyId
     * @param request
     * @return
     */
    public AddBoxToSupplyResponse addBoxToSupply(String token, String supplyId, AddBoxToSupplyRequest request) {
        log.error("接口请求：{}", JSONUtil.toJsonStr(request));
        String url = CharSequenceUtil.format(WildberriesConstant.POST_ADD_BOX_TO_SUPPLY,supplyId);
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", token);
        headerMap.put("Content-Type", "application/json");
        headerMap.put("locale", "zh");
        String bodyStr = HttpRequest.post(url)
                .headerMap(headerMap, true)
                .body(JSONUtil.toJsonStr(request))
                .execute().body();
        log.error("接口返回：{}", bodyStr);
        AddBoxToSupplyResponse response = JSON.parseObject(JSONUtil.toJsonStr(bodyStr),new TypeReference<AddBoxToSupplyResponse>() {}.getType());
        return response;
    }
    /**
     * 添加箱子到组包
     * @param token
     * @param request
     * @return
     */
    public AddOrderToSupplyResponse addOrderToSupply(String token, AddOrderToSupplyRequest request) {
        log.error("接口请求：{}", JSONUtil.toJsonStr(request));
        String url = CharSequenceUtil.format(WildberriesConstant.PATCH_ADD_ORDER_TO_SUPPLY,request.getSupplyId(),request.getOrderId());
        String bodyStr = HttpRequest.patch(url)
                .header("Authorization", token)
                .header("Content-Type", "application/json")
                .header("locale", "zh")
                .execute().body();
        log.error("接口返回：{}", bodyStr);
        AddOrderToSupplyResponse response = JSON.parseObject(JSONUtil.toJsonStr(bodyStr),new TypeReference<AddOrderToSupplyResponse>() {}.getType());
        return response;
    }


    /**
     * 获取订单面签
     * @param token
     * @param request
     * @return
     */
    public OrderLabelResponse getOrderLabel(String token, OrderLabelRequest request) {
        log.error("接口请求：{} token:{}", JSONUtil.toJsonStr(request),token);
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", token);
        headers.put("Content-Type", "application/json");
        headers.put("locale", "zh");
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("orders",request.getOrders());
        okhttp3.HttpUrl httpUrl = new okhttp3.HttpUrl.Builder()
                .scheme("https")
                .host("marketplace-api.wildberries.ru")
                .addPathSegment("api")
                .addPathSegment("v3")
                .addPathSegment("orders")
                .addPathSegment("stickers")
                .addQueryParameter("type", "png")
                .addQueryParameter("width", "58")
                .addQueryParameter("height", "40")
                .build();
        String bodyStr = OkHttpUtils.doPostJsonQueryParam(httpUrl, paramMap, headers);
        log.error("接口返回：{}", bodyStr);
        OrderLabelResponse response = JSON.parseObject(JSONUtil.toJsonStr(bodyStr),new TypeReference<OrderLabelResponse>() {}.getType());
        return response;
    }

    /**
     * Move the supply to the delivery
     * @param token
     * @param supplyId
     * @return
     */
    public BaseResponse signDelivery(String token, String supplyId) {
        log.error("接口请求：{}", JSONUtil.toJsonStr(supplyId));
        String url = CharSequenceUtil.format(WildberriesConstant.PATCH_SIGN_DELIVERY,supplyId);
        String bodyStr = HttpRequest.patch(url)
                .header("Authorization", token)
                .header("Content-Type", "application/json")
                .header("locale", "zh")
                .execute().body();
        log.error("接口返回：{}", bodyStr);
        BaseResponse response = JSON.parseObject(JSONUtil.toJsonStr(bodyStr),new TypeReference<BaseResponse>() {}.getType());
        return response;
    }
    /**
     * 获取跨境订单面签
     * @param token
     * @param request
     * @return
     */
    public CrossOrderLabelResponse getCrossOrderLabel(String token, OrderLabelRequest request) {
        log.error("接口请求：{}", JSONUtil.toJsonStr(request));
        String url = WildberriesConstant.POST_CROSS_ORDER_LABEL;
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", token);
        headerMap.put("Content-Type", "application/json");
        headerMap.put("locale", "zh");
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("orders",request.getOrders());
        String bodyStr = OkHttpUtils.doPostJsonObject(url, paramMap, headerMap);
        log.error("接口返回：{}", bodyStr);
        CrossOrderLabelResponse response = JSON.parseObject(JSONUtil.toJsonStr(bodyStr),new TypeReference<CrossOrderLabelResponse>() {}.getType());
        return response;
    }


    /**
     * Move the supply to the delivery
     * @param token
     * @param supplyId
     * @return
     */
    public SupplyLabelResponse getSupplyLabel(String token, String supplyId) {
        log.error("接口请求：{}", JSONUtil.toJsonStr(supplyId));
        String url = CharSequenceUtil.format(WildberriesConstant.POST_SUPPLY_LABEL,supplyId);
        String bodyStr = HttpRequest.patch(url)
                .header("Authorization", token)
                .header("Content-Type", "application/json")
                .header("type", "png")
                .header("locale", "zh")
                .execute().body();
        log.error("接口返回：{}", bodyStr);
        SupplyLabelResponse response = JSON.parseObject(JSONUtil.toJsonStr(bodyStr),new TypeReference<SupplyLabelResponse>() {}.getType());
        return response;
    }
}
