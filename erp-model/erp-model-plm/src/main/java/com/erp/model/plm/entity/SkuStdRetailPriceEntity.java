package com.erp.model.plm.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * sku标准零售价表
 * </p>
 *
 * @author shukai
 * @since 2026-03-16
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("sku_std_retail_price")
public class SkuStdRetailPriceEntity extends BaseEntity<SkuStdRetailPriceEntity> {

    /**
    * skuId
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * sku编号
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 币别
    */
    @TableField("currency")
    private String currency;
    /**
    * 标准零售价(含税)
    */
    @TableField("std_retail_price_vat")
    private BigDecimal stdRetailPriceVat;
    /**
    * 税率
    */
    @TableField("vat_rate")
    private BigDecimal vatRate;
    /**
    * 标准零售价(不含税)
    */
    @TableField("std_retail_price")
    private BigDecimal stdRetailPrice;


    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String CURRENCY = "currency";

    public static final String STD_RETAIL_PRICE_VAT = "std_retail_price_vat";

    public static final String VAT_RATE = "vat_rate";

    public static final String STD_RETAIL_PRICE = "std_retail_price";

    @Override
    public Serializable pkVal() {
        return null;
    }

}