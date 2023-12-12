package com.sdk.oms.shopee.service;

import com.sdk.oms.shopee.dto.merchant.request.MerchantRequest;
import com.sdk.oms.shopee.dto.merchant.response.MerchantResponse;
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
public class ShopeeMerchantService {

    public static void main(String[] args) {
        ShopeeMerchantService service = new ShopeeMerchantService();
        MerchantRequest shopRequest = MerchantRequest.builder()
                .host(host)
                .token(merchant_access_token)
                .partnerId(partner_id)
                .tmpPartnerKey(tmp_partner_key)
                .merchantId(merchant_id)
                .build();
        MerchantResponse shopInfo = service.getMerchantInfo(shopRequest);
        System.out.println(shopInfo);
    }
    /**
     * 获取店铺信息
     * @param merchantRequest
     * @return
     */
    public MerchantResponse getMerchantInfo(MerchantRequest merchantRequest) {
        HashMap<String, Object> paramMap = new HashMap<>();
        String path = "/api/v2/merchant/get_merchant_info";
        paramMap.put("timestamp", new Long(System.currentTimeMillis() / 1000).toString());
        paramMap.put("sign", ShopeeApiUtils.getMerchantSign(path, merchantRequest.getToken(), merchantRequest.getPartnerId(),
                merchantRequest.getTmpPartnerKey(), merchantRequest.getMerchantId()));
        paramMap.put("merchant_id", merchantRequest.getMerchantId());
        paramMap.put("partner_id", merchantRequest.getPartnerId());
        paramMap.put("access_token", merchantRequest.getToken());
        return ShopeeApiUtils.sendMerchantGet(merchantRequest.getHost() + path, paramMap);
    }
}
