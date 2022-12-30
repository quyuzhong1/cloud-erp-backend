package com.erp.model.bi.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * TODO
 *
 * @Author Cloud
 * @Date 2022/12/30 10:34
 **/

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeptCostVO {

    /**
     * 名称
     */
    private String name;

    /**
     * 成本类型
     */
    private String costType;

    /**
     * 成本值
     */
    private BigDecimal costValue;
}
