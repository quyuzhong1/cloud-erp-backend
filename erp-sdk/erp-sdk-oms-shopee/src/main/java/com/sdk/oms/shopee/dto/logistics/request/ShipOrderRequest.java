package com.sdk.oms.shopee.dto.logistics.request;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName ShipOrderRequest
 * @description: 标发请求参数
 * @date 2023年12月11日
 * @version: 1.0
 */
@Data
@Builder
public class ShipOrderRequest implements Serializable {
    private String orderSn;
    private String packageNumber;
    private PickUp pickup;
    private Dropoff dropoff;
    private Integrated nonIntegrated;
}
