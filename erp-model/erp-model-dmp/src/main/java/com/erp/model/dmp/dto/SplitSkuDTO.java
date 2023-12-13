package com.erp.model.dmp.dto;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class SplitSkuDTO implements Serializable {
    /**
     * 来源平台
     */
    private String platformSign;

    /**
     * id
     */
    private String id;

    /**
     * sku
     */
    private String skuNo;

    /**
     * 马帮SKU
     */
    private String mabangSkuNo;

    /**
     * 原始sku
     */
    private String originalSkuNo;

    /**
     * 清洗前成本价
     */
    private BigDecimal originalCostPrice;

    /**
     * 清洗后成本价
     */
    private BigDecimal cleanCostPrice;

    /**
     * 是否拆分订单 1.拆分 2.非拆分
     */
    private Integer isSplitSku;

    /**
     * 是否是赠品 1. 是 2. 否
     */
    private Integer isGift;

    /**
     * 商品数量
     */
    private Integer quantity;

    /**
     * 清洗前商品售价
     */
    private BigDecimal originalAmountAfter;

    /**
     * 商品售价
     */
    private BigDecimal amountAfter;

    public SplitSkuDTO() {
        this.cleanCostPrice = BigDecimal.ZERO;
        this.amountAfter = BigDecimal.ZERO;
    }
}
