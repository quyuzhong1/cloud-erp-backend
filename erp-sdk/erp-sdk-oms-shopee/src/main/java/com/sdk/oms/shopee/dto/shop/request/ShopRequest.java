package com.sdk.oms.shopee.dto.shop.request;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName ShopRequest
 * @description: TODO
 * @date 2023年12月11日
 * @version: 1.0
 */
@Data
@Builder
public class ShopRequest implements Serializable {
    String host;
    String path;
    String token;
    long shopId;
    long partnerId;
    String tmpPartnerKey;
    long timestamp;
}
