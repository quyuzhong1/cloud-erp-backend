package com.sdk.oms.shopee.dto.shop.response;

import cn.hutool.core.annotation.Alias;
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
    @Alias( "shop_name")
    private String shopName;
    @Alias( "region")
    private String region;
    @Alias( "status")
    private String status;
    @Alias( "is_cb")
    private Boolean isCb;
    @Alias( "is_cnsc")
    private Boolean isCnsc;
    @Alias( "shop_cbsc")
    private String shopCbsc;
    @Alias( "is_3pf")
    private String is3pf;
    @Alias( "request_id")
    private String requestId;
    @Alias( "auth_time")
    private Long authTime;
    @Alias( "expire_time")
    private Long expireTime;
    @Alias( "is_sip")
    private Boolean isSip;
    @Alias( "is_upgraded_cbsc")
    private Boolean isUpgradedCbsc;
    @Alias( "merchant_id")
    private Integer merchant_id;

    @Alias( "error")
    private String error;
    @Alias( "message")
    private String message;
}
