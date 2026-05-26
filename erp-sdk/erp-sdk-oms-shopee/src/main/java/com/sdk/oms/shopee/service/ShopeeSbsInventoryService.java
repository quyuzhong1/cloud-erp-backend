package com.sdk.oms.shopee.service;

import com.sdk.oms.shopee.dto.base.ShopeeResponse;
import com.sdk.oms.shopee.dto.sbs.request.SbsInventoryRequest;
import com.sdk.oms.shopee.utils.ShopeeApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * Shopee SBS 库存接口
 */
@Slf4j
@Component
@Service
public class ShopeeSbsInventoryService {

    private static final String PATH = "/api/v2/sbs/get_current_inventory";

    public ShopeeResponse getCurrentInventory(SbsInventoryRequest request) {
        HashMap<String, Object> paramMap = new HashMap<>();
        long timestamp = System.currentTimeMillis() / 1000L;
        paramMap.put("timestamp", timestamp);
        paramMap.put("sign", ShopeeApiUtils.getOrderSign(PATH, request.getToken(), request.getPartnerId(),
                request.getTmpPartnerKey(), request.getShopId()));
        paramMap.put("shop_id", request.getShopId());
        paramMap.put("partner_id", request.getPartnerId());
        paramMap.put("access_token", request.getToken());
        paramMap.put("whs_region", request.getWhsRegion());
        paramMap.put("page_no", request.getPageNo() == null ? 1 : request.getPageNo());
        paramMap.put("page_size", request.getPageSize() == null ? 100 : request.getPageSize());
        return ShopeeApiUtils.sendGet(request.getHost() + PATH, paramMap);
    }
}
