package com.sdk.oms.shopee.dto.merchant.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName ShopResponse
 * @description: TODO
 * @date 2023年12月11日
 * @version: 1.0
 */
@Data
public class MerchantResponse implements Serializable {
    @JSONField(name = "merchant_name")
    private String merchantName;
    @JSONField(name = "is_cnsc")
    private Boolean isCnsc;
    @JSONField(name = "auth_time")
    private Long authTime;
    @JSONField(name = "expire_time")
    private Long expireTime;
    @JSONField(name = "request_id")
    private String requestId;
    @JSONField(name = "merchant_currency")
    private String merchantCurrency;
    @JSONField(name = "merchant_region")
    private String merchantRegion;
    @JSONField(name = "is_upgraded_cbsc")
    private Boolean isUpgradedCbsc;
    @JSONField(name = "error")
    private String error;
    @JSONField(name = "message")
    private String message;
}
