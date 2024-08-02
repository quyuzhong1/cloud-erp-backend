package com.erp.model.dmp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 生成其他出库单信息DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DmpPullOtherOutStockDTO {
    /**
     * 店铺id
     */
    private String shopId;

    /**
     * 单号
     */
    private String platformCode;

    /**
     * 单号ID
     */
    private String mainId;

    /**
     * 销售平台
     */
    private String dictPlatform;

}