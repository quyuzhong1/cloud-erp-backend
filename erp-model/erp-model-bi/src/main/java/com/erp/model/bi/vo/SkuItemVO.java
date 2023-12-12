package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class SkuItemVO {
    /**
     * 名称
     */
    private String skuNo;

    /**
     * 金额
     */
    private String itemName;
}
