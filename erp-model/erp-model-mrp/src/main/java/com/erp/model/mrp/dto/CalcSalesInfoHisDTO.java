package com.erp.model.mrp.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CalcSalesInfoHisDTO {

    /**
     * SKU
     */
    private String skuId;
    /**
     * SKU
     */
    private String skuNo;
    /**
     * 店铺
     */
    private String shopId;
    /**
     * 店铺
     */
    private String shopName;
    /**
     * 日期
     */
    private LocalDate date;
    /**
     * 数量
     */
    private Integer qty;
    /**
     * 试算配置id
     */
    private String cfgRuleCalcId;

    /**
     * 平台
     */
    private String platform;
}
