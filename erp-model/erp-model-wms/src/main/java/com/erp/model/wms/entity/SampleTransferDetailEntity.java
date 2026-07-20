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
 * 样品转移单明细表
 * </p>
 *
 * @author wuhaotian
 * @since 2025-10-28
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("sample_transfer_detail")
public class SampleTransferDetailEntity extends BaseEntity<SampleTransferDetailEntity> {

    /**
    * 关联转移单主表ID
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 样品台账ID
    */
    @TableField("sample_ledger_id")
    private String sampleLedgerId;
    /**
    * SKU ID
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * SKU编号
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 产品名称
    */
    @TableField("product_name")
    private String productName;
    /**
    * 转移数量
    */
    @TableField("transfer_qty")
    private Integer transferQty;
    /**
    * 可转移数量
    */
    @TableField("available_qty")
    private Integer availableQty;
    /**
    * 目标使用方ID：内部转移=内部用户ID，外部转移=外部使用方字典ID
    */
    @TableField("target_use_user_id")
    private String targetUseUserId;
    /**
    * 目标使用方名称
    */
    @TableField("target_use_user_name")
    private String targetUseUserName;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;


    public static final String MAIN_ID = "main_id";

    public static final String SAMPLE_LEDGER_ID = "sample_ledger_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String PRODUCT_NAME = "product_name";

    public static final String TRANSFER_QTY = "transfer_qty";

    public static final String AVAILABLE_QTY = "available_qty";

    public static final String TARGET_USE_USER_ID = "target_use_user_id";

    public static final String TARGET_USE_USER_NAME = "target_use_user_name";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}