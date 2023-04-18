package com.erp.model.dmp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/19 11:20
 */
@Data
@NoArgsConstructor
public class DmpOrderItemDTO {

    /**
     * 图片
     */
    private String pictureUrl;

    /**
     * SKU
     */
    private String skuNo;

    /**
     * 品名
     */
    private String itemName;

    /**
     * 单价
     */
    private BigDecimal sellPriceOrigin;

    /**
     * 数量
     */
    private BigDecimal quantity;

    /**
     * 销售额（原币种）
     */
    private BigDecimal sellAmountOrigin;
}
