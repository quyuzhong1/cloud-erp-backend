package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * B2C寄样申请单明细
 * </p>
 *
 * @author jack
 * @since 2025-12-04
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("kol_b2c_application_detail")
public class KolB2cApplicationDetailEntity extends BaseEntity<KolB2cApplicationDetailEntity> {

    /**
    * 主表ID
    */
    @TableField("main_id")
    private String mainId;
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
    * 品牌id
    */
    @TableField("brand_id")
    private String brandId;
    /**
    * 品牌
    */
    @TableField("brand_name")
    private String brandName;
    /**
    * 达人ID
    */
    @TableField("partner_id")
    private String partnerId;
    /**
    * 达人昵称
    */
    @TableField("nickname")
    private String nickname;
    /**
    * 申请数量
    */
    @TableField("apply_qty")
    private Integer applyQty;
    /**
    * 预计回片日期
    */
    @TableField("plan_feedback_date")
    private LocalDate planFeedbackDate;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 项目标签
    */
    @TableField("project_tag")
    private String projectTag;
    /**
    * 项目标签
    */
    @TableField(exist = false)
    private String projectTagName;


    public static final String MAIN_ID = "main_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String BRAND_ID = "brand_id";

    public static final String BRAND_NAME = "brand_name";

    public static final String PARTNER_ID = "partner_id";

    public static final String NICKNAME = "nickname";

    public static final String APPLY_QTY = "apply_qty";

    public static final String PLAN_FEEDBACK_DATE = "plan_feedback_date";

    public static final String REMARK = "remark";

    public static final String PROJECT_TAG = "project_tag";

    @Override
    public Serializable pkVal() {
        return null;
    }

}