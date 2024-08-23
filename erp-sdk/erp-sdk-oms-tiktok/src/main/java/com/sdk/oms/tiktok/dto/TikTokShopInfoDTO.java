package com.sdk.oms.tiktok.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 美客多店铺信息
 * @Author Luo_WG
 * @Date 2023/10/18 14:02
 **/
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class TikTokShopInfoDTO {

    /**
     * 店铺ID
     */
    private String id;

    /**
     * 访问token
     */
    private String accessToken;

    /**
     * 店铺名称
     */
    private String name;

    /**
     * 店铺全域名
     */
    private String shopDomain;

    /**
     * 客户端id
     */
    private String clientId;

    /**
     * 客户端secret
     */
    private String clientSecret;

    /**
     * 请求地址
     */
    private String baseUrl;

    /**
     * 店铺站点
     */
    private String site;

    /**
     * 店铺标识
     */
    private String shopCipher;

    /**
     * 店铺类型
     */
    private String sellerType;
}
