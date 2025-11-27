package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 模具关联sku
 * </p>
 *
 * @author jack
 * @since 2025-10-14
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("mold_ref_sku")
public class MoldRefSkuEntity extends BaseEntity<MoldRefSkuEntity> {

    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 审批状态
    */
    @TableField("approve_status")
    private String approveStatus;
    /**
    * 审批时间
    */
    @TableField("approve_time")
    private LocalDateTime approveTime;
    /**
    * 审批人ID
    */
    @TableField("approve_user_id")
    private String approveUserId;
    /**
    * 审批人姓名
    */
    @TableField("approve_user_name")
    private String approveUserName;
    /**
    * 模具id
    */
    @TableField("mold_id")
    private String moldId;
    /**
    * 模具编码
    */
    @TableField("mold_code")
    private String moldCode;
    /**
    * 模具名称
    */
    @TableField("mold_name")
    private String moldName;
    /**
    * skuId
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * SKU
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 产品名称
    */
    @TableField("product_name")
    private String productName;
    /**
    * 单模产量
    */
    @TableField("output_qty")
    private Integer outputQty;
    /**
    * sku用量
    */
    @TableField("sku_qty")
    private Integer skuQty;
    /**
    * code
    */
    @TableField("code")
    private String code;


    public static final String REMARK = "remark";

    public static final String APPROVE_STATUS = "approve_status";

    public static final String APPROVE_TIME = "approve_time";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String MOLD_ID = "mold_id";

    public static final String MOLD_CODE = "mold_code";

    public static final String MOLD_NAME = "mold_name";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String PRODUCT_NAME = "product_name";

    public static final String OUTPUT_QTY = "output_qty";

    public static final String SKU_QTY = "sku_qty";

    public static final String CODE = "code";

    @Override
    public Serializable pkVal() {
        return null;
    }

}