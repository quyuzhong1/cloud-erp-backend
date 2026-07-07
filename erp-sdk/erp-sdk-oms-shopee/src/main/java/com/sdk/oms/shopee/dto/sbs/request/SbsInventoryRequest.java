package com.sdk.oms.shopee.dto.sbs.request;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * Shopee SBS 当前库存请求参数
 */
@Data
@Builder
public class SbsInventoryRequest implements Serializable {

    private String host;
    private String token;
    private long shopId;
    private long partnerId;
    private String tmpPartnerKey;
    private String whsRegion;
    private Integer pageNo;
    private Integer pageSize;
}
