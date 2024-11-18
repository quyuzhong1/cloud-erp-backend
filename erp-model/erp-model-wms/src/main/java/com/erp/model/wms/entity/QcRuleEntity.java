package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.erp.model.wms.enums.QcTypeEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 质检规则
 * </p>
 *
 * @author lambda
 * @since 2023-04-13
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("qc_rule")
public class QcRuleEntity extends BaseEntity<QcRuleEntity> {

    /**
     * 是否有报告 true 有  false 没有
     */
    @TableField("exist_report")
    private Boolean existReport;

    /**
     * 是否禁用 true 禁用 false 没有
     */
    @TableField("disabled")
    private Boolean disabled;

    /**
     * 质检类型
     */
    @TableField("qc_type")
    private QcTypeEnum qcType;

    /**
     * 产品等级 多个以逗号分割
     */
    @TableField("product_grade_key")
    private String productGradeKey;

    /**
     * 质检编号
     */
    @TableField("code")
    private String code;

    /**
     * 审核状态 waitSubmit 待提交,approveIng 审核中 reject 审核不通过 approve 已审核  
     */
    @TableField("approve_status")
    private String approveStatus;

    /**
     * 销售方式 多个以逗号分割
     */
    @TableField("sale_method")
    private String saleMethod;

    public static final String EXIST_REPORT = "exist_report";

    

    public static final String QC_TYPE = "qc_type";

    public static final String PRODUCT_GRADE_KEY = "product_grade_key";

    

    public static final String APPROVE_STATUS = "approve_status";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
