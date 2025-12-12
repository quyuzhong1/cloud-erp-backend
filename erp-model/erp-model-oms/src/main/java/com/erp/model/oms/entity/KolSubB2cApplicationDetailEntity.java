package com.erp.model.oms.entity;

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
 * B2C寄样申请单拆分单明细
 * </p>
 *
 * @author jack
 * @since 2025-12-04
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("kol_sub_b2c_application_detail")
public class KolSubB2cApplicationDetailEntity extends BaseEntity<KolSubB2cApplicationDetailEntity> {

    /**
    * 主表ID
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 来源明细id
    */
    @TableField("source_detail_id")
    private String sourceDetailId;
    /**
     * 来源详情id
     */
    @TableField("third_detail_id")
    private String thirdDetailId;
    /**
     * 平台原始详情id
     */
    @TableField("platform_detail_id")
    private String platformDetailId;
    /**
    * SKU ID
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * SKU编码
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 申请数量
    */
    @TableField("apply_qty")
    private Integer applyQty;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 项目名称
    */
    @TableField("project_tag")
    private String projectTag;


    public static final String MAIN_ID = "main_id";

    public static final String SOURCE_DETAIL_ID = "source_detail_id";

    public static final String PLATFORM_DETAIL_ID = "platform_detail_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String APPLY_QTY = "apply_qty";

    public static final String REMARK = "remark";

    public static final String PROJECT_TAG = "project_tag";

    @Override
    public Serializable pkVal() {
        return null;
    }

}