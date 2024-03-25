package com.sdk.oms.shopee.dto.product.response;

import cn.hutool.core.annotation.Alias;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName PreOrder
 * @description: TODO
 * @date 2023年10月24日
 * @version: 1.0
 */
@Data
public class PreOrder implements Serializable {
    @Alias( "is_pre_order")
    private boolean isPreOrder;
    @Alias( "days_to_ship")
    private Long daysToShip;
}
