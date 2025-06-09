package com.sdk.tms.shopee.model.logistics.request;

import cn.hutool.core.annotation.Alias;
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
    @Alias( "order_sn")
    private String orderSn;
    @Alias( "package_number")
    private String packageNumber;
    private PickUp pickup;
    private Dropoff dropoff;
    @Alias( "non_integrated")
    private Integrated nonIntegrated;
}
