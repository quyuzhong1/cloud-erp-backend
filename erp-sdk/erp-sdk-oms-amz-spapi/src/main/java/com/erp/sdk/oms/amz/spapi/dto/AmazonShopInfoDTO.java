package com.erp.sdk.oms.amz.spapi.dto;

import com.common.core.anno.Panno;
import com.common.core.enums.PannoEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

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
}
