package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class ContrastTrendVO {
    /**
     * 年
     */
    public String year;

    /**
     * 名称
     */
    public String name;

    /**
     * 店铺编号
     */
    public String shopNo;

    /**
     * 销售额
     */
    public BigDecimal sales;

}
