package com.erp.model.oms.dto;

import cn.hutool.core.util.StrUtil;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.exception.ServiceException;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Objects;

/**
 * 店铺授权DTO
 */
@Data
@NoArgsConstructor
public class ShopAuthorizeDTO implements Serializable {

    /**
     * 【公共】平台编号
     */
    private String platformCode;
    /**
     * 【公共】授权code
     */
    private String code;

    /**
     * 【亚马逊、速卖通】授权state
     */
    private String state;

    // Shopify
    /**
     * 【Shopify】hmac
     */
    private String hmac;
    /**
     * 【Shopify】店铺地址
     */
    private String host;
    /**
     * 【Shopify】店铺域名
     */
    private String shop;

    /**
     * 【Shopify】时间戳
     */
    private String timestamp;

    // 沃尔玛
    /**
     * 【沃尔玛】平台账户id
     */
    private String clientId;
    /**
     * 【沃尔玛】平台账户秘钥
     */
    private String clientSecret;
    /**
     * 【沃尔玛】店铺id
     */
    private String shopId;

    // 虾皮
    /**
     * 【虾皮】发起请求的ID
     */
    private String id;
    /**
     * 【虾皮】发起请求的店铺id
     */
    private String shop_id;

    /**
     * 【虾皮】主账号ID
     */
    private Integer main_account_id;

    // 亚马逊
    /**
     * 【亚马逊】授权卖家ID
     */
    private String selling_partner_id;

    /**
     * 【亚马逊】授权code
     */
    private String spapi_oauth_code;

    /**
     * 根据回调参数判断和设置来源平台
     */
    public ShopAuthorizeDTO checkAndSetPlatform() {
        // 前端指定来源
        if (StringUtils.isNotBlank(this.platformCode)){
            PlatformDictEnum platformDictEnum = PlatformDictEnum.getByCode(this.platformCode);
            if (null != platformDictEnum){
                return this;
            }
            throw new ServiceException("提交的平台类型不存在:"+ this.platformCode);
        }
        // 亚马逊
        if (StringUtils.isNotBlank(this.spapi_oauth_code)){
            this.setPlatformCode(PlatformDictEnum.AMAZON.getCode());
            return this;
        }

        // shopify
        if (StringUtils.isNotBlank(shop)){
            this.setPlatformCode(PlatformDictEnum.SHOPIFY.getCode());
            return this;
        }

        // 美客多
        if (StringUtils.isNotBlank(this.state) && StringUtils.isNotBlank(this.code) && StrUtil.startWith(this.state, PlatformDictEnum.MERCADOLIBRE.getCode())){
            this.setPlatformCode(PlatformDictEnum.MERCADOLIBRE.getCode());
            return this;
        }

        // TikTok
        if (StringUtils.isNotBlank(this.state) && StringUtils.isNotBlank(this.code) && StrUtil.startWith(this.state, PlatformDictEnum.TIK_TOK.getCode())){
            this.setPlatformCode(PlatformDictEnum.TIK_TOK.getCode());
            return this;
        }

        // 速卖通
        if (StringUtils.isNotBlank(this.state) && StringUtils.isNotBlank(this.code)){
            this.setPlatformCode(PlatformDictEnum.ALI_EXPRESS.getCode());
            return this;
        }

        // 虾皮
        if (StringUtils.isNotBlank(this.code) && StringUtils.isNotBlank(this.id) &&
                (StringUtils.isNotBlank(this.shop_id) || Objects.nonNull(this.main_account_id))
        ){
            this.setPlatformCode(PlatformDictEnum.SHOPEE.getCode());
            return this;
        }

        // 沃尔玛
        if (StringUtils.isNotBlank(this.shopId) &&
                StringUtils.isNotBlank(this.clientId) &&
                StringUtils.isNotBlank(this.clientSecret)
        ){
            this.setPlatformCode(PlatformDictEnum.WALMART.getCode());
            return this;
        }



        throw new ServiceException("参数无法识别到对应平台");
    }
}
