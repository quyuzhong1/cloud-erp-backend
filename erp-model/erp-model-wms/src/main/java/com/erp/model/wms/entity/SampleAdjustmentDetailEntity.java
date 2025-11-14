package com.erp.model.wms.entity;

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
 * 样品调整单明细表
 * </p>
 *
 * @author wuhaotian
 * @since 2025-11-14
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("sample_adjustment_detail")
public class SampleAdjustmentDetailEntity extends BaseEntity<SampleAdjustmentDetailEntity> {

    /**
    * 关联主表ID
    */
    @TableField("main_id")
    private String mainId;
    /**
    * SKU ID
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * sku编号
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 产品名称
    */
    @TableField("product_name")
    private String productName;
    /**
    * 台账数量
    */
    @TableField("ledger_qty")
    private Integer ledgerQty;
    /**
    * 实际数量
    */
    @TableField("actual_qty")
    private Integer actualQty;
    /**
    * 差异数量
    */
    @TableField("difference_qty")
    private Integer differenceQty;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 样品台账id
    */
    @TableField("sample_ledger_id")
    private String sampleLedgerId;


    public static final String MAIN_ID = "main_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String PRODUCT_NAME = "product_name";

    public static final String LEDGER_QTY = "ledger_qty";

    public static final String ACTUAL_QTY = "actual_qty";

    public static final String DIFFERENCE_QTY = "difference_qty";

    public static final String REMARK = "remark";

    public static final String SAMPLE_LEDGER_ID = "sample_ledger_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}