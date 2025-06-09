package com.sdk.oms.shopee.dto.merchant.response;

import cn.hutool.core.annotation.Alias;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName ShipResponse
 * @description: TODO
 * @date 2023年12月11日
 * @version: 1.0
 */
@Data
public class MerchantResponse implements Serializable {
    @Alias( "merchant_name")
    private String merchantName;
    @Alias( "is_cnsc")
    private Boolean isCnsc;
    @Alias( "auth_time")
    private Long authTime;
    @Alias( "expire_time")
    private Long expireTime;
    @Alias( "request_id")
    private String requestId;
    @Alias( "merchant_currency")
    private String merchantCurrency;
    @Alias( "merchant_region")
    private String merchantRegion;
    @Alias( "is_upgraded_cbsc")
    private Boolean isUpgradedCbsc;
    @Alias( "error")
    private String error;
    @Alias( "message")
    private String message;
}
