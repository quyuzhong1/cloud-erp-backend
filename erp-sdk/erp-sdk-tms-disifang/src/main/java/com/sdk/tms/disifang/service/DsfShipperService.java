package com.sdk.tms.disifang.service;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.core.exception.ServiceException;
import com.sdk.tms.disifang.constants.AmbientEnum;
import com.sdk.tms.disifang.model.base.AffterentParam;
import com.sdk.tms.disifang.model.base.ResponseMsg;
import com.sdk.tms.disifang.model.label.request.LabelRequest;
import com.sdk.tms.disifang.model.label.request.LabelSingleRequest;
import com.sdk.tms.disifang.model.order.request.*;
import com.sdk.tms.disifang.model.product.request.ChanelRequest;
import com.sdk.tms.disifang.utils.ApiHttpClientUtils;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zdy
 * @ClassName DsfLogisticsServiceImpl
 * @description: 递四方物流服务
 * @date 2023年10月31日
 * @version: 1.0
 */
@Slf4j
@Validated
@Component
public class DsfShipperService {
    //生产环境
    private static String CLIENT_ID = "clientId";
    private static String CLIENT_SECRET = "clientSecret";
    //测试环境
    static String host = "https://open-test.4px.com";
    static String testAppKey = "5dca6db7-6a21-4d31-a5f8-33a24a4f5b9d";
    static String testAppSecret = "b8bd24a5-35b0-4e8a-bbc0-c7458e21c7ad";
    private void validate(String appKey,String appSecret,String method,String url){
        if (StringUtils.isEmpty(appKey) || StringUtils.isEmpty(appSecret) || StringUtils.isEmpty(method) || StringUtils.isEmpty(url) ) {
            throw new ServiceException("授权信息不能为空");
        }
    }
    /**
     * 获取标签 打印标签
     *
     * @param authMap
     * @param labelSingleRequest
     * @return ResponseMsg
     */
    public ResponseMsg getLabel(Map<String, String> authMap, LabelSingleRequest labelSingleRequest) {
        String appKey = authMap.get(CLIENT_ID);
        String appSecret = authMap.get(CLIENT_SECRET);
        String url = authMap.get("url");
        String method = "ds.xms.label.get";
        validate(appKey,appSecret,method,url);
        AffterentParam param = AffterentParam.builder()
                .version("1.0")
                .format("json")
                .language("cn")
                .appKey(appKey)
                .appSecret(appSecret)
                .method(method)
                .build();
        String s = ApiHttpClientUtils.apiJsonPostUrl(param, JSONUtil.toJsonStr(labelSingleRequest), url);
        return JSON.parseObject(s, ResponseMsg.class);
    }

    /**
     * 批量获取标签 打印标签
     *
     * @param authMap
     * @param labelRequest
     * @return ResponseMsg
     */
    public ResponseMsg getLabelList(Map<String, String> authMap, LabelRequest labelRequest) {
        String appKey = authMap.get(CLIENT_ID);
        String appSecret = authMap.get(CLIENT_SECRET);
        String url = authMap.get("url");
        String method = "ds.xms.label.getlist";
        validate(appKey,appSecret,method,url);
        AffterentParam param = AffterentParam.builder()
                .version("1.0")
                .format("json")
                .language("cn")
                .appKey(appKey)
                .appSecret(appSecret)
                .method(method)
                .build();
        String s = ApiHttpClientUtils.apiJsonPostUrl(param, JSONUtil.toJsonStr(labelRequest), url);
        return JSON.parseObject(s, ResponseMsg.class);
    }

    /**
     * 物流产品查询 获取渠道
     *
     * @param authMap
     * @param chanelRequest
     * @return ResponseMsg
     */
    public ResponseMsg getChanelList(Map<String, String> authMap, ChanelRequest chanelRequest) {
        String appKey = authMap.get(CLIENT_ID);
        String appSecret = authMap.get(CLIENT_SECRET);
        String url = authMap.get("url");
        String method = "ds.xms.logistics_product.getlist";
        validate(appKey,appSecret,method,url);
        AffterentParam param = AffterentParam.builder()
                .version("1.0")
                .format("json")
                .language("cn")
                .appKey(appKey)
                .appSecret(appSecret)
                .method(method)
                .build();
        String s = ApiHttpClientUtils.apiJsonPostUrl(param, JSONUtil.toJsonStr(chanelRequest), url);
        return JSON.parseObject(s, ResponseMsg.class);
    }

    /**
     * 创建直发委托单
     *
     * @param authMap
     * @param orderRequest
     * @return ResponseMsg
     */
    public ResponseMsg createOrder(Map<String, String> authMap, OrderRequest orderRequest) {
        log.info("==========DsfShipperService.createOrder==========start");
        log.info("authMap:{}, orderRequest:{}",authMap, orderRequest);
        String appKey = authMap.get(CLIENT_ID);
        String appSecret = authMap.get(CLIENT_SECRET);
        String url = authMap.get("url");
        String method = "ds.xms.order.create";
        validate(appKey,appSecret,method,url);
        AffterentParam param = AffterentParam.builder()
                .version("1.0")
                .format("json")
                .language("cn")
                .appKey(appKey)
                .appSecret(appSecret)
                .method(method)
                .build();
        String result = ApiHttpClientUtils.apiJsonPostUrl(param, JSONUtil.toJsonStr(orderRequest), url);
        return JSON.parseObject(result, ResponseMsg.class);
    }

    /**
     * 取消直发委托单
     *
     * @param authMap
     * @param orderCancelRequest
     * @return ResponseMsg
     */
    public ResponseMsg cancelOrder(Map<String, String> authMap, OrderCancelRequest orderCancelRequest) {
        String appKey = authMap.get(CLIENT_ID);
        String appSecret = authMap.get(CLIENT_SECRET);
        String url = authMap.get("url");
        String method = "ds.xms.order.cancel";
        validate(appKey,appSecret,method,url);
        AffterentParam param = AffterentParam.builder()
                .version("1.0")
                .format("json")
                .language("cn")
                .appKey(appKey)
                .appSecret(appSecret)
                .method(method)
                .build();
        String result = ApiHttpClientUtils.apiJsonPostUrl(param, JSONUtil.toJsonStr(orderCancelRequest), url);
        return JSON.parseObject(result, ResponseMsg.class);
    }

    /**
     * 申请|取消拦截订单
     *
     * @param authMap
     * @param orderInterceptRequest
     * @return ResponseMsg
     */
    public ResponseMsg interceptOrder(Map<String, String> authMap, OrderInterceptRequest orderInterceptRequest) {
        String appKey = authMap.get(CLIENT_ID);
        String appSecret = authMap.get(CLIENT_SECRET);
        String url = authMap.get("url");
        String method = "ds.xms.order.hold";
        validate(appKey,appSecret,method,url);
        AffterentParam param = AffterentParam.builder()
                .version("1.0")
                .format("json")
                .language("cn")
                .appKey(appKey)
                .appSecret(appSecret)
                .method(method)
                .build();
        String result = ApiHttpClientUtils.apiJsonPostUrl(param, JSONUtil.toJsonStr(orderInterceptRequest), url);
        return JSON.parseObject(result, ResponseMsg.class);
    }

    /**
     * 查询直发委托单
     *
     * @param authMap
     * @param orderQueryRequest
     * @return ResponseMsg
     */
    public ResponseMsg queryOrder(Map<String, String> authMap, OrderQueryRequest orderQueryRequest) {
        String appKey = authMap.get(CLIENT_ID);
        String appSecret = authMap.get(CLIENT_SECRET);
        String url = authMap.get("url");
        String method = "ds.xms.order.get";
        validate(appKey,appSecret,method,url);
        AffterentParam param = AffterentParam.builder()
                .version("1.0")
                .format("json")
                .language("cn")
                .appKey(appKey)
                .appSecret(appSecret)
                .method(method)
                .build();
        String result = ApiHttpClientUtils.apiJsonPostUrl(param, JSONUtil.toJsonStr(orderQueryRequest), url);
        return JSON.parseObject(result, ResponseMsg.class);
    }

    /**
     * 创建揽收预约单 下单
     *
     * @param authMap
     * @param orderCollectRequest
     * @return ResponseMsg
     */
    public ResponseMsg createCollectOrder(Map<String, String> authMap, OrderCollectRequest orderCollectRequest) {
        String appKey = authMap.get(CLIENT_ID);
        String appSecret = authMap.get(CLIENT_SECRET);
        String url = authMap.get("url");
        String method = "ds.xms.api.collect.create.order";
        validate(appKey,appSecret,method,url);
        AffterentParam param = AffterentParam.builder()
                .version("1.0")
                .format("json")
                .language("cn")
                .appKey(appKey)
                .appSecret(appSecret)
                .method(method)
                .build();
        String s = ApiHttpClientUtils.apiJsonPostUrl(param, JSONUtil.toJsonStr(orderCollectRequest), url);
        return JSON.parseObject(s, ResponseMsg.class);
    }

    /**
     * 创建揽收预约单 下单
     *
     * @param authMap
     * @param orderCollectRequest
     * @return ResponseMsg
     */
    public ResponseMsg cancelCollectOrder(Map<String, String> authMap, OrderCollectRequest orderCollectRequest) {
        String appKey = authMap.get(CLIENT_ID);
        String appSecret = authMap.get(CLIENT_SECRET);
        String url = authMap.get("url");
        String method = "ds.xms.order.cancel";
        validate(appKey,appSecret,method,url);
        AffterentParam param = AffterentParam.builder()
                .version("1.0")
                .format("json")
                .language("cn")
                .appKey(appKey)
                .appSecret(appSecret)
                .method(method)
                .build();
        String s = ApiHttpClientUtils.apiJsonPostUrl(param, JSONUtil.toJsonStr(orderCollectRequest), url);
        return JSON.parseObject(s, ResponseMsg.class);
    }

    public ResponseMsg updateWeight(Map<String, String> authMap, DsfUpdateWeightReq dsfUpdateWeightReq) {
        String appKey = authMap.get(CLIENT_ID);
        String appSecret = authMap.get(CLIENT_SECRET);
        String url = authMap.get("url");
        String method = "ds.xms.order.updateweight";
        validate(appKey,appSecret,method,url);
        AffterentParam param = AffterentParam.builder()
                .version("1.0")
                .format("json")
                .language("cn")
                .appKey(appKey)
                .appSecret(appSecret)
                .method(method)
                .build();
        String s = ApiHttpClientUtils.apiJsonPostUrl(param, JSONUtil.toJsonStr(dsfUpdateWeightReq), url);
        ResponseMsg responseMsg = JSON.parseObject(s, ResponseMsg.class);
        return responseMsg;
    }

}
