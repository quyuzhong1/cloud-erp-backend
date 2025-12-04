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
 * B2B寄样申请明细表
 * </p>
 *
 * @author will
 * @since 2025-12-01
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("kol_b2b_application_detail")
public class KolB2bApplicationDetailEntity extends BaseEntity<KolB2bApplicationDetailEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * skuId
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * sku编码
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 申请数量
    */
    @TableField("qty")
    private Integer qty;
    /**
    * 预计回片日期
    */
    @TableField("plan_feedback_date")
    private LocalDate planFeedbackDate;
    /**
    * 项目名称
    */
    @TableField("project_tag")
    private String projectTag;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;


    public static final String MAIN_ID = "main_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String QTY = "qty";

    public static final String PLAN_FEEDBACK_DATE = "plan_feedback_date";

    public static final String PROJECT_TAG = "project_tag";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}