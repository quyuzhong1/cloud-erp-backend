package com.erp.model.fms.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 资产处置单资产明细表
 * </p>
 *
 * @author jack
 * @since 2025-10-29
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("asset_disposal_detail")
public class AssetDisposalDetailEntity extends BaseEntity<AssetDisposalDetailEntity> {

    /**
    * 来源ID
    */
    @TableField("source_id")
    private String sourceId;
    /**
    * 来源单号
    */
    @TableField("source_code")
    private String sourceCode;
    /**
    * 来源明细ID
    */
    @TableField("source_detail_id")
    private String sourceDetailId;
    /**
    * 主表ID
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 资产名称
    */
    @TableField("asset_name")
    private String assetName;
    /**
    * 单位 Pcs
    */
    @TableField("unit")
    private String unit;
    /**
    * 数量
    */
    @TableField("qty")
    private Integer qty;
    /**
    * 处置数量
    */
    @TableField("disposal_qty")
    private Integer disposalQty;
    /**
    * 处置币类
    */
    @TableField("disposal_currency")
    private String disposalCurrency;
    /**
    * 清理费用
    */
    @TableField("cleanup_cost")
    private BigDecimal cleanupCost;
    /**
    * 残值收入 含税
    */
    @TableField("residual_value")
    private BigDecimal residualValue;
    /**
    * 发票类型：ordinary=普通发票, addedValue增值发票  枚举：AssetDisposalDetailInvoiceTypeEnum
    */
    @TableField("invoice_type")
    private String invoiceType;
    /**
    * 税率
    */
    @TableField("tax_rate")
    private BigDecimal taxRate;
    /**
    * 税额
    */
    @TableField("tax_amount")
    private BigDecimal taxAmount;


    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_CODE = "source_code";

    public static final String SOURCE_DETAIL_ID = "source_detail_id";

    public static final String MAIN_ID = "main_id";

    public static final String ASSET_NAME = "asset_name";

    public static final String UNIT = "unit";

    public static final String QTY = "qty";

    public static final String DISPOSAL_QTY = "disposal_qty";

    public static final String DISPOSAL_CURRENCY = "disposal_currency";

    public static final String CLEANUP_COST = "cleanup_cost";

    public static final String RESIDUAL_VALUE = "residual_value";

    public static final String INVOICE_TYPE = "invoice_type";

    public static final String TAX_RATE = "tax_rate";

    public static final String TAX_AMOUNT = "tax_amount";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
