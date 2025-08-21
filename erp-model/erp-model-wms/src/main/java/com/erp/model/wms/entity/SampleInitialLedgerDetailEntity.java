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
 * 样品期初台账详情
 * </p>
 *
 * @author wuhaotian
 * @since 2025-08-21
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("sample_initial_ledger_detail")
public class SampleInitialLedgerDetailEntity extends BaseEntity<SampleInitialLedgerDetailEntity> {

    /**
    * 主表ID（关联期初台账）
    */
    @TableField("main_id")
    private String mainId;
    /**
    * SKU编码
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * SKU ID
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * 产品名称
    */
    @TableField("product_name")
    private String productName;
    /**
    * 数量
    */
    @TableField("qty")
    private Integer qty;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;


    public static final String MAIN_ID = "main_id";

    public static final String SKU_NO = "sku_no";

    public static final String SKU_ID = "sku_id";

    public static final String PRODUCT_NAME = "product_name";

    public static final String QTY = "qty";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}