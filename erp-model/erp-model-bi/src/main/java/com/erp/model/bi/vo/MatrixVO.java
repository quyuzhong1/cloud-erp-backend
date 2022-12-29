package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MatrixVO {
    /**
     * 名称
     */
    private String name;

    /**
     * 销售额
     */
    private String sales;

    /**
     * 净利润
     */
    private String netProfit;
}
