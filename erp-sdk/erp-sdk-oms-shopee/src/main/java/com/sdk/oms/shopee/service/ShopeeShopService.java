package com.sdk.oms.shopee.service;

import com.sdk.oms.shopee.dto.base.ShopeeResponse;
import com.sdk.oms.shopee.dto.global.request.GlobalProductRequest;
import com.sdk.oms.shopee.dto.shop.request.ShopRequest;
import com.sdk.oms.shopee.dto.shop.response.ShopResponse;
import com.sdk.oms.shopee.utils.ShopeeApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;

import static com.sdk.oms.shopee.constants.ShopeeConstants.*;

/**
 * @author zdy
 * @ClassName ShopeeShopService
 * @description: 店铺服务
 * @date 2023年12月11日
 * @version: 1.0
 */
@Component
@Slf4j
public class ShopeeShopService {

    public static void main(String[] args) {
        ShopeeShopService service = new ShopeeShopService();
        ShopRequest shopRequest = ShopRequest.builder()
                .host(host)
                .token(shop_access_token)
                .partnerId(partner_id)
                .tmpPartnerKey(tmp_partner_key)
                .shopId(shop_id)
                .build();
        ShopResponse shopInfo = service.getShopInfo(shopRequest);
        System.out.println(shopInfo);
    }
    /**
     * 获取店铺信息
     * @param shopRequest
     * @return
     */
    public ShopResponse getShopInfo(ShopRequest shopRequest) {
        HashMap<String, Object> paramMap = new HashMap<>();
        String path = "/api/v2/shop/get_shop_info";
        paramMap.put("timestamp", new Long(System.currentTimeMillis() / 1000).toString());
        paramMap.put("sign", ShopeeApiUtils.getOrderSign(path, shopRequest.getToken(), shopRequest.getPartnerId(),
                shopRequest.getTmpPartnerKey(), shopRequest.getShopId()));
        paramMap.put("shop_id", shopRequest.getShopId());
        paramMap.put("partner_id", shopRequest.getPartnerId());
        paramMap.put("access_token", shopRequest.getToken());
        return ShopeeApiUtils.sendShopGet(shopRequest.getHost() + path, paramMap);
    }
}
