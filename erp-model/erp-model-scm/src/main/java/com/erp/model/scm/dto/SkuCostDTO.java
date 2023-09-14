package com.erp.model.scm.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * sku成本信息
 * @Author Luo_WG
 * @Date 2023/9/13 18:45
 **/
@Data
public class SkuCostDTO {
    /**
     * id
     */
    private String id;

    /**
    * sku编号
    */
    private String skuNo;

    /**
    * 成本日期
    */
    private LocalDate costDate;

    /**
    * 备注
    */
    private String remark;

    /**
    * 有效状态
    */
    private Boolean status;

    /**
    * 成本价格
    */
    private BigDecimal costPrice;

    /**
    * 最近的采购日期3个月前的日期
    */
    private LocalDate threeMonthsAgoDate;

    /**
    * 最近的采购日期
    */
    private LocalDate latestPurchaseDate;
}