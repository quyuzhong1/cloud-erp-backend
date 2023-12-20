package com.erp.model.bi.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 *
 *
 * @Author Cloud
 * @Date 2022/12/30 10:34
 **/

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerSaleVO {
    /**
     * 成本类型
     */
    private String name;

    /**
     * 成本值
     */
    private BigDecimal value;
}
