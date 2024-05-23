package com.erp.model.dmp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 生成销售出库单信息DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DmpPullSoOutStockDTO {
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
    private String soB2cId;

}