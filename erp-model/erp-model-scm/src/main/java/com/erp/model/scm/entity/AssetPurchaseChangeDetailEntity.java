package com.erp.model.scm.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * 
 * </p>
 *
 * @author wtr
 * @since 2025-10-16
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("asset_purchase_change_detail")
public class AssetPurchaseChangeDetailEntity extends BaseEntity<AssetPurchaseChangeDetailEntity> {

    /**
    * 资产采购变更单单头id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 资产id
    */
    @TableField("asset_id")
    private String assetId;
    /**
    * 资产编码
    */
    @TableField("asset_code")
    private String assetCode;
    /**
    * 资产名称
    */
    @TableField("asset_name")
    private String assetName;
    /**
    * 原采购数量
    */
    @TableField("old_purchase_qty")
    private BigDecimal oldPurchaseQty;
    /**
    * 原含税单价
    */
    @TableField("old_tax_price")
    private BigDecimal oldTaxPrice;
    /**
    * 原价税合计
    */
    @TableField("old_total_amount")
    private BigDecimal oldTotalAmount;
    /**
     * 税率
     */
    @TableField("old_tax_rate")
    private BigDecimal oldTaxRate;
    /**
    * 新采购数量
    */
    @TableField("purchase_qty")
    private BigDecimal purchaseQty;
    /**
    * 新含税单价
    */
    @TableField("tax_price")
    private BigDecimal taxPrice;
    /**
    * 新价税合计
    */
    @TableField("total_amount")
    private BigDecimal totalAmount;
    /**
     * 新税率
     */
    @TableField("tax_rate")
    private BigDecimal taxRate;
    /**
    * 币种
    */
    @TableField("currency")
    private String currency;
    /**
    * 币种符号
    */
    @TableField("currency_symbol")
    private String currencySymbol;

    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 金蝶明细id
    */
    @TableField("kingdee_detail_id")
    private String kingdeeDetailId;
    /**
    * 来源明细id
    */
    @TableField("source_detail_id")
    private String sourceDetailId;


    public static final String MAIN_ID = "main_id";

    public static final String ASSET_ID = "asset_id";

    public static final String ASSET_CODE = "asset_code";

    public static final String ASSET_NAME = "asset_name";

    public static final String OLD_PURCHASE_QTY = "old_purchase_qty";

    public static final String OLD_TAX_PRICE = "old_tax_price";

    public static final String OLD_TOTAL_AMOUNT = "old_total_amount";

    public static final String PURCHASE_QTY = "purchase_qty";

    public static final String TAX_PRICE = "tax_price";

    public static final String TOTAL_AMOUNT = "total_amount";

    public static final String CURRENCY = "currency";

    public static final String CURRENCY_SYMBOL = "currency_symbol";

    public static final String TAX_RATE = "tax_rate";

    public static final String REMARK = "remark";

    public static final String KINGDEE_DETAIL_ID = "kingdee_detail_id";

    public static final String SOURCE_DETAIL_ID = "source_detail_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}