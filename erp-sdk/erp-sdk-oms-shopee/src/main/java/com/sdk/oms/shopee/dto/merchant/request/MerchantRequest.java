package com.sdk.oms.shopee.dto.merchant.request;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName MerchantRequest
 * @description: TODO
 * @date 2023年12月11日
 * @version: 1.0
 */
@Data
@Builder
public class MerchantRequest implements Serializable {
    String host;
    String path;
    String token;
    long merchantId;
    long partnerId;
    String tmpPartnerKey;
}
