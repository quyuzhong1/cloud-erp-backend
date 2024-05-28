package com.sdk.oms.shopee.dto.product.response;

import cn.hutool.core.annotation.Alias;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName SummaryInfo
 * @description: TODO
 * @date 2023年10月24日
 * @version: 1.0
 */
@Data
public class SummaryInfo implements Serializable {
    @Alias( "total_reserved_stock")
    private Integer totalReservedStock;
    @Alias( "total_available_stock")
    private Integer totalAvailableStock;
}
