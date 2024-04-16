package com.erp.model.oms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author zdy
 * @ClassName SplitSkuDTO
 * @description: TODO
 * @date 2024年01月30日
 * @version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SplitSkuDTO {

    private String skuId;

    private String skuNo;

    private Integer qty;

    private BigDecimal length;

    private BigDecimal width;

    private BigDecimal height;

    public SplitSkuDTO(String skuId, String skuNo) {
        this.skuId = skuId;
        this.skuNo = skuNo;
        this.qty = 0;
        this.length = BigDecimal.ZERO;
        this.width = BigDecimal.ZERO;
        this.height = BigDecimal.ZERO;
    }
}
