package com.erp.model.oms.dto;

import com.common.business.enums.PlatformDictEnum;
import com.common.core.anno.StateEnumValue;
import com.erp.model.oms.entity.ShopInfoEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 店铺授权DTO
 */
@Data
@NoArgsConstructor
public class ShopAuthorizeUrlDTO implements Serializable {

    /**
     * id
     */
    private String id;
    /**
     * 平台编号
     */
    @NotNull(message = "平台编号不能为空")
    @StateEnumValue(clazz = PlatformDictEnum.class, message = "平台编号有误")
    private String platformCode;
    /**
     * 授权code
     */
    private String code;
    /**
     * hmac
     */
    private String hmac;
    /**
     * 店铺地址
     */
    private String host;
    /**
     * 店铺域名
     */
    private String shop;
    /**
     * 时间戳
     */
    private String timestamp;
    /**
     * 平台账户id
     */
    private String clientId;
    /**
     * 平台账户秘钥
     */
    private String clientSecret;
    /**
     * 店铺id
     */
    private String shopId;

    private Integer mainAccountId;

    /**
     * 亚马逊授权state
     */
    private String state;

    /**
     * 亚马逊授权卖家ID
     */
    private String selling_partner_id;

    /**
     * 亚马逊授权code
     */
    private String spapi_oauth_code;

    /**
     * 亚马逊批量授权的店铺id
     */
    private List<String> shopIdList;


    /**
     * 亚马逊批量授权的店铺
     */
    private List<ShopInfoEntity> shopInfoEntityList;

}
