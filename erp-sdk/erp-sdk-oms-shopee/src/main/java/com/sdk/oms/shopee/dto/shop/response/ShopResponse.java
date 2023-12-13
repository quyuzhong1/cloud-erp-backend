package com.sdk.oms.shopee.dto.shop.response;

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
public class ShopResponse implements Serializable {
    @JSONField(name = "shop_name")
    private String shopName;
    @JSONField(name = "region")
    private String region;
    @JSONField(name = "status")
    private String status;
    @JSONField(name = "is_cb")
    private Boolean isCb;
    @JSONField(name = "is_cnsc")
    private Boolean isCnsc;
    @JSONField(name = "shop_cbsc")
    private String shopCbsc;
    @JSONField(name = "is_3pf")
    private String is3pf;
    @JSONField(name = "request_id")
    private String requestId;
    @JSONField(name = "auth_time")
    private Long authTime;
    @JSONField(name = "expire_time")
    private Long expireTime;
    @JSONField(name = "is_sip")
    private Boolean isSip;
    @JSONField(name = "is_upgraded_cbsc")
    private Boolean isUpgradedCbsc;
    @JSONField(name = "merchant_id")
    private Integer merchant_id;

    @JSONField(name = "error")
    private String error;
    @JSONField(name = "message")
    private String message;
}
