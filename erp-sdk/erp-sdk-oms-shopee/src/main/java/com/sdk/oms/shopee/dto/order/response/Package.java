package com.sdk.oms.shopee.dto.order.response;

import cn.hutool.core.annotation.Alias;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zdy
 * @ClassName Package
 * @description: TODO
 * @date 2023年10月24日
 * @version: 1.0
 */
@Data
public class Package implements Serializable {

    @Alias( "package_number")
    private String packageNumber;

    @Alias( "logistics_status")
    private String logisticsStatus;

    @Alias( "shipping_carrier")
    private String shippingCarrier;
    @Alias( "item_list")
    private List<OrderItemSimple> itemList;

    @Alias( "parcel_chargeable_weight_graml")
    private Long parcelChargeableWeightGraml;
}
