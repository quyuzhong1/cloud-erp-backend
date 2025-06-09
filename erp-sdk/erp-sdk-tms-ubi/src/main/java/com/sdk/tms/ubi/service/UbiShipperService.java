package com.sdk.tms.ubi.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.core.exception.ServiceException;
import com.common.core.utils.OkHttpUtils;
import com.sdk.tms.ubi.constant.PathConstants;
import com.sdk.tms.ubi.constant.UbiConstants;
import com.sdk.tms.ubi.model.BaseResult;
import com.sdk.tms.ubi.model.catalog.response.Label;
import com.sdk.tms.ubi.model.catalog.response.ServiceCataLog;
import com.sdk.tms.ubi.model.label.LabelRequest;
import com.sdk.tms.ubi.model.label.LabelResponse;
import com.sdk.tms.ubi.model.order.request.HoldRequest;
import com.sdk.tms.ubi.model.order.request.UbiOrder;
import com.sdk.tms.ubi.model.order.response.OrderResponse;
import com.sdk.tms.ubi.model.order.response.TrackBase;
import com.sdk.tms.ubi.utils.IntegrationHelper;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author zdy
 * @ClassName UbiShipperService

 * @date 2023年10月30日
 * @version: 1.0
 */
@Slf4j
@Component
public class UbiShipperService {
    public static final String CLIENT_ID = "clientId";
    public static final String CLIENT_SECRET = "clientSecret";

    /**
     * 创建订单
     *
     * @param ubiOrder
     * @return
     */
    public List<OrderResponse> createOrder(Map<String, String> authMap, UbiOrder ubiOrder) {
        log.info("==========UbiShipperService.createOrder==========start");
        log.info("authMap:{}, orderRequest:{}",authMap, ubiOrder);
        String token = authMap.get(CLIENT_ID);
        String key = authMap.get(CLIENT_SECRET);
        String url = authMap.get("url");
        validate(token, key, url);
        String serviceUrl = url + PathConstants.POST_CREATE_ORDERS_URL;
        log.info("创建订单url：{}", serviceUrl);
        Map<String, String> headers = IntegrationHelper.buildHeader(UbiConstants.POST_REQUEST_METHOD, serviceUrl, token, key);
        List<UbiOrder> orders = new ArrayList<>();
        orders.add(ubiOrder);
        String res = OkHttpUtils.doPostJsonObject(serviceUrl, orders, headers);
        log.info("下单完成：{}", JSON.toJSONString(res));
        BaseResult<JSONArray> result = JSONUtil.toBean(res, BaseResult.class);
        if (UbiConstants.SUCCESS.equalsIgnoreCase(result.getStatus())) {
            return JSONUtil.toList(result.getData(), OrderResponse.class);
        } else {
            throw new ServiceException(result.getErrors());
        }
    }

    /**
     * 打印标签
     * 产生和打印标签，按照请求顺序返回结果。
     * <p>
     * orderIds、masterIds 两个字段值，如果同时填写了默认取值OrderIds内容；
     * 通过字段值Merged支持去合并PDF格式的面单，其它格式不支持合并；
     *
     * @param labelRequest
     * @param authMap
     */
    public List<LabelResponse> getLabels(Map<String, String> authMap, LabelRequest labelRequest) {
        String token = authMap.get(CLIENT_ID);
        String key = authMap.get(CLIENT_SECRET);
        String url = authMap.get("url");
        validate(token, key, url);
        String serviceUrl = PathConstants.BASE_URL + PathConstants.POST_LABELS_URL;
        Map<String, String> headers = IntegrationHelper.buildHeader(UbiConstants.POST_REQUEST_METHOD, serviceUrl, token, key);
        String res = OkHttpUtils.doPostJsonObject(serviceUrl, labelRequest, headers);
        log.info("打印标签：{}", res);
        BaseResult<JSONArray> result = JSONUtil.toBean(res, BaseResult.class);
        if (UbiConstants.SUCCESS.equalsIgnoreCase(result.getStatus())) {
            return JSONUtil.toList(result.getData(), LabelResponse.class);
        } else {
            throw new ServiceException(result.getErrors());
        }
    }

    /**
     * 获取标签
     * 一个JSON格式的订单序列，可以用客户端订单号(referenceNo)或服务端订单号(orderId)或跟踪号(trackingNo)，一次最多300个。
     *
     * @param referenceNo
     * @param orderId
     * @param trackingNo
     */
    public List<Label> getLabelSpecs(Map<String, String> authMap, String referenceNo, String orderId, String trackingNo) {
        String token = authMap.get(CLIENT_ID);
        String key = authMap.get(CLIENT_SECRET);
        String url = authMap.get("url");
        validate(token, key, url);
        String serviceUrl = PathConstants.BASE_URL + PathConstants.POST_LABEL_SPECS_URL;
        log.info("获取标签url：{}", serviceUrl);
        Map<String, Object> params = new LinkedHashMap<>();
        if (CharSequenceUtil.isNotBlank(referenceNo)) {
            params.put("referenceNo", referenceNo);
        } else if (CharSequenceUtil.isNotBlank(orderId)) {
            params.put("orderId", orderId);
        } else if (CharSequenceUtil.isNotBlank(trackingNo)) {
            params.put("trackingNo", trackingNo);
        } else {
            throw new ServiceException("参数不能为空");
        }
        Map<String, String> headers = IntegrationHelper.buildHeader(UbiConstants.POST_REQUEST_METHOD, serviceUrl, token, key);
        String res = OkHttpUtils.doPostJson(serviceUrl, params, headers);
        log.info("获取标签：{}", res);
        BaseResult<JSONArray> result = JSONUtil.toBean(res, BaseResult.class);
        if (UbiConstants.SUCCESS.equalsIgnoreCase(result.getStatus())) {
            return JSONUtil.toList(result.getData(), Label.class);
        } else {
            throw new ServiceException(result.getErrors());
        }
    }

    /**
     * 单件删除
     * URL支持用客户端订单号(referenceNo)或服务端订单号(orderId)来进行删除，如果为一票多件订单需要同时删除，
     * 则将客户端订单号(referenceNo)或服务端订单号(orderId)传值主单对应的值
     *
     * @param authMap
     * @param code
     */
    public OrderResponse deleteShipperOrder(Map<String, String> authMap, String code) {
        String token = authMap.get(CLIENT_ID);
        String key = authMap.get(CLIENT_SECRET);
        String url = authMap.get("url");
        validate(token, key, url);
        String serviceUrl = PathConstants.BASE_URL + CharSequenceUtil.format(PathConstants.DELETE_LABEL_SPECS_URL, code);
        log.info("单件删除 serviceUrl：{}", serviceUrl);
        Map<String, Object> params = new LinkedHashMap<>();
        Map<String, String> headers = IntegrationHelper.buildHeader(UbiConstants.DELETE_REQUEST_METHOD, serviceUrl, token, key);
        String res = OkHttpUtils.doDelete(serviceUrl, params, headers);
        log.info("单件删除：{}", res);
        BaseResult<String> result = JSONUtil.toBean(res, BaseResult.class);
        if (UbiConstants.SUCCESS.equalsIgnoreCase(result.getStatus())) {
            return JSONUtil.toBean(JSON.toJSONString(result.getData()), OrderResponse.class);
        } else {
            throw new ServiceException(result.getErrors());
        }
    }

    /**
     * 扣货接口
     *
     * @param authMap
     * @param holdRequest
     * @return
     */
    public List<OrderResponse> interceptOrder(Map<String, String> authMap, HoldRequest holdRequest) {
        String token = authMap.get(CLIENT_ID);
        String key = authMap.get(CLIENT_SECRET);
        String url = authMap.get("url");
        validate(token, key, url);
        String serviceUrl = PathConstants.BASE_URL + PathConstants.POST_INTERCEPT_ORDER_URL;
        log.info("单件删除 serviceUrl：{}", serviceUrl);
        Map<String, String> headers = IntegrationHelper.buildHeader(UbiConstants.POST_REQUEST_METHOD, serviceUrl, token, key);
        String res = OkHttpUtils.doPostJsonObject(serviceUrl, holdRequest, headers);
        log.info("单件删除：{}", res);
        BaseResult<JSONArray> result = JSONUtil.toBean(res, BaseResult.class);
        if (UbiConstants.SUCCESS.equalsIgnoreCase(result.getStatus())) {
            return JSONUtil.toList(result.getData(), OrderResponse.class);
        } else {
            throw new ServiceException(result.getErrors());
        }
    }

    /**
     * 获取开通的服务
     */
    public List<ServiceCataLog> getServiceCatalog(Map<String, String> authMap) {
        String token = authMap.get(CLIENT_ID);
        String key = authMap.get(CLIENT_SECRET);
        String url = authMap.get("url");
        validate(token, key, url);
        String serviceUrl = PathConstants.BASE_URL + PathConstants.GET_SERVICE_CATALOG_URL;
        log.info("获取开通的服务url：{}", serviceUrl);
        java.util.Map<String, Object> params = new LinkedHashMap<>();
        Map<String, String> headers = IntegrationHelper.buildHeader(UbiConstants.GET_REQUEST_METHOD, serviceUrl, token, key);
        String res = OkHttpUtils.doGet(serviceUrl, params, headers);
        log.info("获取开通的服务：{}", res);
        BaseResult<JSONArray> result = JSONUtil.toBean(res, BaseResult.class);
        if (UbiConstants.SUCCESS.equalsIgnoreCase(result.getStatus())) {
            return JSONUtil.toList((JSONArray) result.getData(), ServiceCataLog.class);
        } else {
            throw new ServiceException(result.getErrors());
        }
    }

    /**
     * 获取跟踪号
     * 客户使用 参考号 查询跟踪号，最多获取300条记录
     *
     * @param authMap
     * @param numbers
     */
    public List<TrackBase> getTrackNumber(Map<String, String> authMap, List<String> numbers) {
        String token = authMap.get(CLIENT_ID);
        String key = authMap.get(CLIENT_SECRET);
        String url = authMap.get("url");
        validate(token, key, url);
        String serviceUrl = PathConstants.BASE_URL + PathConstants.POST_TRACK_NUMBER_URL;
        Map<String, String> headers = IntegrationHelper.buildHeader(UbiConstants.POST_REQUEST_METHOD, serviceUrl, token, key);
        String res = OkHttpUtils.doPostJsonObject(serviceUrl, numbers, headers);
        log.info("获取跟踪号：{}", res);
        BaseResult<JSONArray> result = JSONUtil.toBean(res, BaseResult.class);
        if (UbiConstants.SUCCESS.equalsIgnoreCase(result.getStatus())) {
            return JSONUtil.toList((JSONArray) result.getData(), TrackBase.class);
        } else {
            throw new ServiceException(result.getErrors());
        }
    }

    private void validate(String token, String key, String url) {
        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(key) || StringUtils.isEmpty(url))
            throw new ServiceException("授权信息不能为空");
    }
}
