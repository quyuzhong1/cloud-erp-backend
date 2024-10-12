package com.sdk.oms.shopee.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.sdk.oms.shopee.dto.base.ShopeeResponse;
import com.sdk.oms.shopee.dto.logistics.request.ShipOrderRequest;
import com.sdk.oms.shopee.dto.logistics.request.ShipRequest;
import com.sdk.oms.shopee.dto.logistics.response.ShipResponse;
import com.sdk.oms.shopee.dto.order.request.OrderRequest;
import com.sdk.oms.shopee.dto.shop.request.ShopRequest;
import com.sdk.oms.shopee.dto.shop.response.ShopResponse;
import com.sdk.oms.shopee.utils.ShopeeApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;

import static com.sdk.oms.shopee.constants.ShopeeConstants.*;

/**
 * @author zdy
 * @ClassName ShopeeLogisticsService
 * @description: 物流服务
 * @date 2024年10月09日
 * @version: 1.0
 */
@Component
@Slf4j
public class ShopeeLogisticsService {

    public static void main(String[] args) {
        ShopeeLogisticsService service = new ShopeeLogisticsService();
        ShipRequest shipRequest = ShipRequest.builder()
                .host(host)
                .token(shop_access_token)
                .partnerId(partner_id)
                .tmpPartnerKey(tmp_partner_key)
                .shopId(shop_id)
                .build();
        String orderSn = "2409068X5S22U7";
        String packageNumber ="OFG179307286219949";
        ShipResponse shopInfo = service.getShipping(shipRequest, orderSn,packageNumber);
        System.out.println(shopInfo);
    }
    /**
     * 获取标发参数
     * @param shipRequest
     * @return
     */
    public ShipResponse getShipping(ShipRequest shipRequest, String orderSn, String packageNumber) {
        String path = "/api/v2/logistics/get_shipping_parameter";
        shipRequest.setPath(path);
        long timestamp = System.currentTimeMillis() / 1000L;
        shipRequest.setTimestamp(timestamp);
        HashMap<String, Object> paramMap = getOrderCommonParam(shipRequest);
        //请求参数
        paramMap.put("order_sn", orderSn);
        if (StrUtil.isNotBlank(packageNumber)){
            paramMap.put("package_number", packageNumber);
        }
        return ShopeeApiUtils.sendShipGet(shipRequest.getHost() + path, paramMap);
    }
    public ShopeeResponse shippingOrder(ShipRequest shipRequest, ShipOrderRequest shipOrderRequest) {
        String path = "/api/v2/logistics/ship_order";
        shipRequest.setPath(path);
        long timestamp = System.currentTimeMillis() / 1000L;
        shipRequest.setTimestamp(timestamp);
        HashMap<String, Object> paramMap = getOrderCommonParam(shipRequest);
        return ShopeeApiUtils.sendShipOrderPost(shipRequest.getHost() + path, paramMap, JSONUtil.toJsonStr(shipOrderRequest));
    }

    private HashMap<String, Object> getOrderCommonParam(ShipRequest shipRequest) {
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("timestamp", shipRequest.getTimestamp());
        paramMap.put("sign", ShopeeApiUtils.getOrderSign(shipRequest.getPath(), shipRequest.getToken(), shipRequest.getPartnerId(),
                shipRequest.getTmpPartnerKey(), shipRequest.getShopId()));
        paramMap.put("shop_id", shipRequest.getShopId());
        paramMap.put("partner_id", shipRequest.getPartnerId());
        paramMap.put("access_token", shipRequest.getToken());
        return paramMap;
    }
}
