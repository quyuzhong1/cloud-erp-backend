package com.sdk.oms.shopee.service;

import com.sdk.oms.shopee.dto.base.ShopeeAuth;
import com.sdk.oms.shopee.dto.base.ShopeeTokenAuth;
import com.sdk.oms.shopee.utils.ShopeeApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;

import static com.sdk.oms.shopee.constants.ShopeeConstants.*;

/**
 * @author zdy
 * @ClassName ShopeeAuthService
 * @description: 授权接口
 * @date 2023年10月20日
 * @version: 1.0
 */
@Component
@Slf4j
public class ShopeeAuthService {

    public static void main(String[] args) {
        ShopeeAuthService shopeeAuthService = new ShopeeAuthService();
        shopeeAuthService.refreshShopToken(host, shop_refresh_token, partner_id, tmp_partner_key, shop_id);
//        shopeeAuthService.refreshMerchantToken(host,merchant_refresh_token,partner_id,tmp_partner_key, merchant_id);

    }

    public String getCodeUrl(String host,long partner_id, String tmp_partner_key, String redirect) {
        HashMap<String, Object> paramMap = new HashMap<>();
        String path = "/api/v2/shop/auth_partner";
        paramMap.put("partner_id", partner_id);
        paramMap.put("timestamp", System.currentTimeMillis() / 1000L);
        paramMap.put("sign", ShopeeApiUtils.getPublicSign(path, partner_id, tmp_partner_key));
        paramMap.put("redirect", redirect);
        return ShopeeApiUtils.buildUrl(host + path, paramMap).toString();
    }

    /**
     * 获取主账号后，刷新所有店铺refresh_token
     *
     * @param host
     * @param code
     * @param partner_id
     * @param tmp_partner_key
     * @param main_account_id
     * @return
     */
    public ShopeeAuth getMainAccountToken(String host, String code, long partner_id, String tmp_partner_key, long main_account_id) {
        HashMap<String, Object> paramMap = new HashMap<>();
        String path = "/api/v2/auth/token/get";
        long timestamp = System.currentTimeMillis() / 1000L;
        paramMap.put("partner_id", partner_id);
        paramMap.put("timestamp", timestamp);
        paramMap.put("sign", ShopeeApiUtils.getPublicSign(path, partner_id, tmp_partner_key));
        HashMap<String, Object> bodyParam = new HashMap<>();
        bodyParam.put("code", code);
        bodyParam.put("main_account_id", main_account_id);
        bodyParam.put("partner_id", partner_id);
        return ShopeeApiUtils.sendAuthPost(host + path, paramMap, bodyParam);
    }

    /**
     * 获取店铺授权
     *
     * @param host
     * @param code
     * @param partner_id
     * @param tmp_partner_key
     * @param shop_id
     * @return
     */
    public ShopeeAuth getShopAccountToken(String host, String code, long partner_id, String tmp_partner_key, long shop_id) {
        HashMap<String, Object> paramMap = new HashMap<>();
        String path = "/api/v2/auth/token/get";
        long timestamp = System.currentTimeMillis() / 1000L;
        paramMap.put("partner_id", partner_id);
        paramMap.put("timestamp", timestamp);
        paramMap.put("sign", ShopeeApiUtils.getPublicSign(path, partner_id, tmp_partner_key));
        HashMap<String, Object> bodyParam = new HashMap<>();
        bodyParam.put("code", code);
        bodyParam.put("shop_id", shop_id);
        bodyParam.put("partner_id", partner_id);
        return ShopeeApiUtils.sendAuthPost(host + path, paramMap, bodyParam);
    }

    /**
     * 刷新店铺token
     *
     * @param host
     * @param refresh_token
     * @param partner_id
     * @param tmp_partner_key
     * @param shopId
     * @return
     */
    public ShopeeTokenAuth refreshShopToken(String host, String refresh_token, long partner_id, String tmp_partner_key, long shopId) {
        HashMap<String, Object> urlParam = new HashMap<>();
        String path = "/api/v2/auth/access_token/get";
        urlParam.put("timestamp", new Long(System.currentTimeMillis() / 1000).toString());
        urlParam.put("sign", ShopeeApiUtils.getPublicSign(path, partner_id, tmp_partner_key));
        urlParam.put("partner_id", partner_id);
        HashMap<String, Object> bodyParam = new HashMap<>();
        bodyParam.put("refresh_token", refresh_token);
        bodyParam.put("shop_id", shopId);
        bodyParam.put("partner_id", partner_id);
        return ShopeeApiUtils.sendRefreshPost(host + path, urlParam, bodyParam);
    }

    /**
     * 刷新merchantToken
     *
     * @param host
     * @param refresh_token
     * @param partner_id
     * @param tmp_partner_key
     * @param merchant_id
     * @return
     */
    public ShopeeTokenAuth refreshMerchantToken(String host, String refresh_token, long partner_id, String tmp_partner_key, long merchant_id) {
        HashMap<String, Object> urlParam = new HashMap<>();
        String path = "/api/v2/auth/access_token/get";
        urlParam.put("timestamp", new Long(System.currentTimeMillis() / 1000).toString());
        urlParam.put("sign", ShopeeApiUtils.getPublicSign(path, partner_id, tmp_partner_key));
        urlParam.put("partner_id", partner_id);
        HashMap<String, Object> bodyParam = new HashMap<>();
        bodyParam.put("refresh_token", refresh_token);
        bodyParam.put("merchant_id", merchant_id);
        bodyParam.put("partner_id", partner_id);
        return ShopeeApiUtils.sendRefreshPost(host + path, urlParam, bodyParam);
    }
}
