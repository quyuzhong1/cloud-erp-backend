package com.erp.model.dmp.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * 亚马逊 店铺 DTO
 *
 * @Author Jim
 **/
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class AmazonShopInfoDTO {

    /**
     * 店铺ID
     */
    private String id;

    /**
     * 访问token
     */
    private String accessToken;

    /**
     * 刷新的token
     */
    private String refreshToken;

    /**
     * 店铺名称
     */
    private String name;

    /**
     * 区域id
     */
    private String dictAreaCode;

    /**
     * 国家id
     */
    private String dictCountryCode;

    /**
     * 负责人id
     */
    private String chargeId;

    /**
     * 亚马逊客户端ID
     */
    private String clientId;

    /**
     * 亚马逊客户端密钥
     */
    private String clientSecret;

    /**
     * 亚马逊Sp-API 访问keyID
     */
    private String accessKeyId;

    /**
     * 亚马逊Sp-API 密钥
     */
    private String secretKey;

    /**
     * 亚马逊Sp-API 角色
     */
    private String roleStr;

    /**
     * 亚马逊Sp-API 用户
     */
    private String userStr;

    /**
     * 授权地址
     * <a href="https://api.amazon.com/auth/o2/token"></a>
     */
    private String authUrl;

    /**
     * 平台店铺编码/卖家编码
     * 亚马逊平台=卖家ID
     */
    private String platformShopCode;

    /**
     * 当前亚马逊账号所有已授权的站点map
     * Map<marketplaceId, shopId>
     * 包含自身
     */
    private Map<String, ShopNameDTO> marketplaceShopIdMap;



    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShopNameDTO {

        /**
         * id
         */
        private String shopId;

        /**
         * 名称
         */
        private String shopName;

    }

}
