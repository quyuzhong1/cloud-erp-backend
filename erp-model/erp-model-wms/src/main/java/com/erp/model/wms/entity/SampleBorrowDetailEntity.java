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
 * 借用变更单明细表
 * </p>
 *
 * @author jack
 * @since 2025-08-26
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("sample_borrow_detail")
public class SampleBorrowDetailEntity extends BaseEntity<SampleBorrowDetailEntity> {

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
    * 借用数量
    */
    @TableField("borrow_qty")
    private Integer borrowQty;
    /**
    * 待归还数量
    */
    @TableField("wait_return_qty")
    private Integer waitReturnQty;
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

    public static final String BORROW_QTY = "borrow_qty";

    public static final String WAIT_RETURN_QTY = "wait_return_qty";

    public static final String REMARK = "remark";

    public static final String SAMPLE_LEDGER_ID = "sample_ledger_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}