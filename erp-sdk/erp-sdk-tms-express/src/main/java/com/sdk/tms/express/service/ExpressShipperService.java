package com.sdk.tms.express.service;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.core.exception.ServiceException;
import com.sdk.tms.express.constants.PathConstants;
import com.sdk.tms.express.enums.ExpressServiceCodeEnum;
import com.sdk.tms.express.model.base.BaseResponse;
import com.sdk.tms.express.model.base.BaseResult;
import com.sdk.tms.express.model.order.request.OrderLabelRequest;
import com.sdk.tms.express.model.order.request.OrderQueryRequest;
import com.sdk.tms.express.model.order.request.OrderRequest;
import com.sdk.tms.express.model.order.request.OrderUpdateRequest;
import com.sdk.tms.express.utils.CallExpressServiceTools;
import com.sdk.tms.express.utils.HttpClientUtil;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author zdy
 * @ClassName ExpressShipperService

 * @date 2023年10月30日
 * @version: 1.0
 */
@Slf4j
@Component
public class ExpressShipperService {

    private static String CLIENT_ID = "clientId";
    private static String CLIENT_SECRET = "clientSecret";

    @Value("${tms.sf-express.monthlyCard}")
    private String monthCard;
    /**
     * 创建订单
     * <p>
     * 下订单接口提供以下四个功能：
     * (1) 客户系统向顺丰下发订单
     * (2) 为订单分配运单号
     * (3) 筛单
     * (4) 路由注册（可选）
     *
     * @param authMap
     * @param orderRequest
     * @return OrderResponse
     * @throws UnsupportedEncodingException
     */
    public BaseResult createOrder(Map<String, String> authMap, OrderRequest orderRequest) throws UnsupportedEncodingException {
        log.info("==========ExpressShipperService.createOrder==========start");
        log.info("authMap:{}, orderRequest:{}",authMap, orderRequest);
        String partnerId = authMap.get(CLIENT_ID);
        String md5Key = authMap.get(CLIENT_SECRET);
        String url = authMap.get("url");
        validate(partnerId, md5Key, url);
        IServiceCodeStandard standardService = ExpressServiceCodeEnum.EXP_RECE_CREATE_ORDER; //下订单
        if (StringUtils.isEmpty(orderRequest.getMonthlyCard())) {
            log.info("monthCard {}",monthCard);
            orderRequest.setMonthlyCard(monthCard);
        }
        return doPost(url, partnerId, md5Key, JSONUtil.toJsonStr(orderRequest), standardService.getCode());
    }

    /**
     * 订单确认/取消接口
     * <p>
     * 接口用于以下场景:
     * (1) 客户在确定将货物交付给顺丰托运后，将运单上的一些重要信息，如快件重量通过此接口发送给顺丰。(2) 客户在发货前取消订单。
     * 注意：订单取消之后，订单号也是不能重复利用的。
     *
     * @param authMap
     * @param orderUpdateRequest
     * @return OrderUpdateResponse
     * @throws UnsupportedEncodingException
     */
    public BaseResult updateOrder(Map<String, String> authMap, OrderUpdateRequest orderUpdateRequest) throws UnsupportedEncodingException {
        String partnerId = authMap.get(CLIENT_ID);
        String md5Key = authMap.get(CLIENT_SECRET);
        String url = authMap.get("url");
        validate(partnerId, md5Key, url);
        IServiceCodeStandard standardService = ExpressServiceCodeEnum.EXP_RECE_UPDATE_ORDER; //订单确认/取消接口
        return doPost(url, partnerId, md5Key, JSONUtil.toJsonStr(orderUpdateRequest), standardService.getCode());
    }

    /**
     * 查询订单结果
     *
     * @param authMap
     * @param orderQueryRequest
     * @return OrderSearchRespDto
     * @throws UnsupportedEncodingException
     */
    public BaseResult queryOrder(Map<String, String> authMap, OrderQueryRequest orderQueryRequest) throws UnsupportedEncodingException {
        String partnerId = authMap.get(CLIENT_ID);
        String md5Key = authMap.get(CLIENT_SECRET);
        String url = authMap.get("url");
        validate(partnerId, md5Key, url);
        IServiceCodeStandard standardService = ExpressServiceCodeEnum.EXP_RECE_SEARCH_ORDER_RESP; //查询订单结果
        return doPost(url, partnerId, md5Key, JSONUtil.toJsonStr(orderQueryRequest), standardService.getCode());
    }

    /**
     * 获取标签
     *
     * @param authMap
     * @param orderLabelRequest
     * @return
     * @throws UnsupportedEncodingException
     */
    public BaseResult getLabel(Map<String, String> authMap, OrderLabelRequest orderLabelRequest) throws UnsupportedEncodingException {
        String partnerId = authMap.get(CLIENT_ID);
        String md5Key = authMap.get(CLIENT_SECRET);
        String url = authMap.get("url");
        validate(partnerId, md5Key, url);
        IServiceCodeStandard standardService = ExpressServiceCodeEnum.COM_RECE_CLOUD_PRINT_WAYBILLS; //面单打印
        return doPost(url, partnerId, md5Key, JSONUtil.toJsonStr(orderLabelRequest), standardService.getCode());
    }

    /**
     * 校验运单号合法性
     *
     * @param authMap
     * @param waybillNo
     * @return
     * @throws UnsupportedEncodingException
     */
    public BaseResponse validateWaybillNo(Map<String, String> authMap, String waybillNo) throws UnsupportedEncodingException {
        String partnerId = authMap.get(CLIENT_ID);
        String md5Key = authMap.get(CLIENT_SECRET);
        String url = authMap.get("url");
        validate(partnerId, md5Key, url);
        JSONObject jsonObject = new JSONObject();
        jsonObject.putOpt("waybillNo", waybillNo);
        IServiceCodeStandard standardService = ExpressServiceCodeEnum.EXP_RECE_VALIDATE_WAYBILLNO; //提供顺丰运单号合法性校验功能。
        return doPostValidate(url, partnerId, md5Key, JSONUtil.toJsonStr(jsonObject), standardService.getCode());
    }

    private static BaseResponse doPostValidate(String host, String partnerId, String md5Key, String msgData, String serviceCode) throws UnsupportedEncodingException {
        Map<String, String> params = new HashMap<String, String>();

        String timeStamp = String.valueOf(System.currentTimeMillis());
        params.put("partnerID", partnerId);  // 顾客编码 ，对应丰桥上获取的clientCode
        params.put("requestID", UUID.randomUUID().toString().replace("-", ""));
        params.put("serviceCode", serviceCode);// 接口服务码
        params.put("timestamp", timeStamp);
        params.put("msgData", msgData);
        params.put("msgDigest", CallExpressServiceTools.getMsgDigest(msgData, timeStamp, md5Key));
        log.info("====调用实际请求：{}", params);
        String result = HttpClientUtil.post(host, params);
        log.info("====返回结果：{}", params);
        BaseResponse baseResponse = JSONUtil.toBean(result, BaseResponse.class);
        return baseResponse;
    }

    private static BaseResult doPost(String host, String partnerId, String md5Key, String msgData, String serviceCode) throws UnsupportedEncodingException {
        Map<String, String> params = new HashMap<String, String>();

        String timeStamp = String.valueOf(System.currentTimeMillis());
        params.put("partnerID", partnerId);  // 顾客编码 ，对应丰桥上获取的clientCode
        params.put("requestID", UUID.randomUUID().toString().replace("-", ""));
        params.put("serviceCode", serviceCode);// 接口服务码
        params.put("timestamp", timeStamp);
        params.put("msgData", msgData);
        params.put("msgDigest", CallExpressServiceTools.getMsgDigest(msgData, timeStamp, md5Key));
        log.warn("====顺丰调用实际请求：{}", params);
        String result = HttpClientUtil.post(host, params);
        log.warn("====顺丰返回结果：{}", params);
        BaseResponse baseResponse = JSONUtil.toBean(result, BaseResponse.class);
        BaseResult baseResult = JSONUtil.toBean(baseResponse.getApiResultData(), BaseResult.class);
        return baseResult;
    }

    private void validate(String partnerId, String md5Key, String url) {
        if (StringUtils.isEmpty(partnerId) || StringUtils.isEmpty(md5Key) || StringUtils.isEmpty(url))
            throw new ServiceException("授权信息不能为空");

    }
}
