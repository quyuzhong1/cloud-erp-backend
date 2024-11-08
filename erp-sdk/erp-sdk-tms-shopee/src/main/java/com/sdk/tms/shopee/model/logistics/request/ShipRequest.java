package com.sdk.tms.shopee.model.logistics.request;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName ShipRequest
 * @description: TODO
 * @date 2023年12月11日
 * @version: 1.0
 */
@Data
@Builder
public class ShipRequest implements Serializable {
    String host;
    String path;
    String token;
    long shopId;
    long partnerId;
    String tmpPartnerKey;
    long timestamp;
}
