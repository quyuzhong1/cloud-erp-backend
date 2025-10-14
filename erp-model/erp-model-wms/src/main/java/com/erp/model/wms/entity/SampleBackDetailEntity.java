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
 * 样品退回详情
 * </p>
 *
 * @author wuhaotian
 * @since 2025-08-21
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("sample_back_detail")
public class SampleBackDetailEntity extends BaseEntity<SampleBackDetailEntity> {

    /**
    * 主表ID（关联样品退回单）
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 来源明细ID
    */
    @TableField("source_detail_id")
    private String sourceDetailId;
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
    * 使用方ID
    */
    @TableField("use_user_id")
    private String useUserId;
    /**
    * 退回数量
    */
    @TableField("qty")
    private Integer qty;
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

    public static final String SOURCE_DETAIL_ID = "source_detail_id";

    public static final String SKU_NO = "sku_no";

    public static final String SKU_ID = "sku_id";

    public static final String PRODUCT_NAME = "product_name";

    public static final String USE_USER_ID = "use_user_id";

    public static final String QTY = "qty";

    public static final String REMARK = "remark";

    public static final String SAMPLE_LEDGER_ID = "sample_ledger_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}