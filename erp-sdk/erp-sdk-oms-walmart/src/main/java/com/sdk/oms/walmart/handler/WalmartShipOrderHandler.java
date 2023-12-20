package com.sdk.oms.walmart.handler;

import com.alibaba.fastjson.JSONObject;
import cn.hutool.core.bean.BeanUtil;
import com.sdk.oms.walmart.api.WalmartStaticKey;
import com.sdk.oms.walmart.dto.walmart.WalmartShipOrderDTO;
import com.sdk.oms.walmart.dto.walmart.WalmartTokenDTO;
import com.sdk.oms.walmart.dto.walmart.ship.*;
import com.sdk.oms.walmart.service.WalmartSdkClientService;

import javax.annotation.Resource;
import java.util.*;

public class WalmartShipOrderHandler {

    @Resource
    private  WalmartSdkClientService walmartSdkClientService;

    public static void main(String[] args) {
        String baseUrl = "https://marketplace.walmartapis.com/v3/orders/{purchaseOrderId}/shipping";
        String clientId = "2434a35c-7c42-4420-9618-0c179b68a8c2";
        String clientSecret = "AMW5lbVFqG2DMP4DuLezhSkbk4u0JLGUjdFlsrl_p0sagsBkYPPiQhRbEvkE4a6k6KXNKhB--RGlqPKIfhUoV28";
        //获取令牌
        baseUrl = WalmartStaticKey.baseUrl + "token";

        List<String> list = new ArrayList<>();
        list.add("1");
        WalmartSdkClientService walmartSdkClientService = new WalmartSdkClientService();
        WalmartTokenDTO walmartTokenDTO = walmartSdkClientService.sendWalmartPostToken(baseUrl, clientId, clientSecret);

        baseUrl = WalmartStaticKey.baseUrl + "orders/108835047622134/shipping";
        WalmartShipOrderDTO walmartShipOrderDTO = new WalmartShipOrderDTO();

        OrderShipmentBean orderShipmentBean = new OrderShipmentBean();

        OrderLinesBean orderLinesBean = new OrderLinesBean();

        List<OrderLineBean> orderLineBeanList = new ArrayList<>();
        //详情：
        for (String s : list) {

            OrderLineBean orderLineBean = new OrderLineBean();
            //详情序号
            orderLineBean.setLineNumber("1");
            //订单号：要求唯一
            orderLineBean.setSellerOrderId("savsad1");

            //订单状态
            OrderLineStatusesBean orderLineStatuses = new OrderLineStatusesBean();
            List<OrderLineStatusBean> orderLineStatus = new ArrayList<>();
            OrderLineStatusBean orderLineStatusBean = new OrderLineStatusBean();
            //发货状态
            orderLineStatusBean.setStatus("Shipped");

            //----------------设置发货数量信息-----------------------------------
            StatusQuantityBean statusQuantity = new StatusQuantityBean();
            //发货数量
            statusQuantity.setAmount("0");
            //计量单位
            statusQuantity.setUnitOfMeasurement("EACH");
            orderLineStatusBean.setStatusQuantity(statusQuantity);

            //----------------有关包裹装运和跟踪更新的信息列表-----------------------------------
            TrackingInfoBean trackingInfo = new TrackingInfoBean();
            //包裹的发货日期
            trackingInfo.setShipDateTime(System.currentTimeMillis());
            //有关包裹承运商的信息
            CarrierNameBean carrierName = new CarrierNameBean();
            carrierName.setCarrier("UPS");
            trackingInfo.setCarrierName(carrierName);
            //运输方式。可以是以下类型之一：Standard、Express、OneDay、WhiteGlove、Value或Freight
            trackingInfo.setMethodCode("Standard");
            //运单号
            trackingInfo.setTrackingNumber("1");
            orderLineStatusBean.setTrackingInfo(trackingInfo);
            orderLineStatus.add(orderLineStatusBean);
            orderLineStatuses.setOrderLineStatus(orderLineStatus);
            orderLineBean.setOrderLineStatuses(orderLineStatuses);

            orderLineBeanList.add(orderLineBean);
        }
        orderLinesBean.setOrderLine(orderLineBeanList);


        orderShipmentBean.setOrderLines(orderLinesBean);

        walmartShipOrderDTO.setOrderShipment(orderShipmentBean);


        HashMap<String, Object> map = (HashMap<String, Object>) BeanUtil.beanToMap(walmartShipOrderDTO);
        String param = JSONObject.toJSONString(map);
        Map<String, Object> map1 = walmartSdkClientService.sendPost(baseUrl, clientId, clientSecret, walmartTokenDTO.getAccessToken(), param);
//        String s = walmartSdkClientService.sendWalmartPost(baseUrl, clientId, clientSecret, walmartTokenDTO.getAccessToken(), map);
        System.out.println(map1);
    }


}
