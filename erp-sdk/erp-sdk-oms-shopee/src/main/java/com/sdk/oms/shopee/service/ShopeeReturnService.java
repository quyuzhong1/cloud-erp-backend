package com.sdk.oms.shopee.service;

import static com.sdk.oms.shopee.constants.ShopeeConstants.host;
import static com.sdk.oms.shopee.constants.ShopeeConstants.pageSize;
import static com.sdk.oms.shopee.constants.ShopeeConstants.partner_id;
import static com.sdk.oms.shopee.constants.ShopeeConstants.shop_id;
import static com.sdk.oms.shopee.constants.ShopeeConstants.tmp_partner_key;

import java.util.HashMap;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.sdk.oms.shopee.dto.base.ShopeeResponse;
import com.sdk.oms.shopee.dto.order.request.OrderRequest;
import com.sdk.oms.shopee.utils.ShopeeApiUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zdy
 * @ClassName GlobalProductServiceImpl
 * @description: TODO
 * @date 2023年10月19日
 * @version: 1.0
 */
@Component
@Slf4j
public class ShopeeReturnService {
    public static void main(String[] args) {
        ShopeeReturnService shopeeOrderService = new ShopeeReturnService();
        long timest = System.currentTimeMillis() / 1000L;
        Long time_from = timest - (3600 * 24 * 365);
        Long time_to = timest;
        //订单列表
        OrderRequest orderRequest = OrderRequest.builder()
                .offset(0)
                .timeFrom(time_from)
                .timeTo(time_to)
                .tmpPartnerKey(tmp_partner_key)
                .partnerId(partner_id)
                .token("4d4a564a457a766b6876526c6c414f54")
                .shopId(shop_id)
                .host(host)
                .cursor("")
                .orderSns("200203171852695")
                .build();
        shopeeOrderService.getReturnDetail(orderRequest);
    }


    private HashMap<String, Object> getOrderCommonParam(OrderRequest orderRequest) {
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("timestamp", orderRequest.getTimestamp());
        paramMap.put("sign", ShopeeApiUtils.getOrderSign(orderRequest.getPath(), orderRequest.getToken(), orderRequest.getPartnerId(),
                orderRequest.getTmpPartnerKey(), orderRequest.getShopId()));
        paramMap.put("shop_id", orderRequest.getShopId());
        paramMap.put("partner_id", orderRequest.getPartnerId());
        paramMap.put("access_token", orderRequest.getToken());
        return paramMap;
    }

    public ShopeeResponse getReturnList(OrderRequest orderRequest) {
        String path = "/api/v2/returns/get_return_list";
        orderRequest.setPath(path);
        long timestamp = System.currentTimeMillis() / 1000L;
        orderRequest.setTimestamp(timestamp);
        HashMap<String, Object> paramMap = getOrderCommonParam(orderRequest);
        if (Objects.nonNull(orderRequest.getTimeFrom())) {
            paramMap.put("update_time_from", orderRequest.getTimeFrom());
        }
        if (Objects.nonNull(orderRequest.getTimeTo())) {
            paramMap.put("update_time_to", orderRequest.getTimeTo());
        }
        paramMap.put("page_no", 1);
        //1-100
        paramMap.put("page_size", pageSize);
        paramMap.put("cursor", orderRequest.getCursor());
        return ShopeeApiUtils.sendGet(orderRequest.getHost() + path, paramMap);
    }

    public ShopeeResponse getReturnDetail(OrderRequest orderRequest) {
    	String path = "/api/v2/returns/get_return_detail";
        orderRequest.setPath(path);
        long timestamp = System.currentTimeMillis() / 1000L;
        orderRequest.setTimestamp(timestamp);
        HashMap<String, Object> paramMap = getOrderCommonParam(orderRequest);
        paramMap.put("return_sn", orderRequest.getOrderSns());
        return ShopeeApiUtils.sendGet(orderRequest.getHost() + path, paramMap);
    }
}
