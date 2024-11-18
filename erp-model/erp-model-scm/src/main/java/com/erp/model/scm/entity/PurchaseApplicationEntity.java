package com.erp.model.scm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 采购申请表
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("purchase_application")
public class PurchaseApplicationEntity extends BaseEntity<PurchaseApplicationEntity> {

    /**
     * 审核状态 
     */
    @TableField("approve_status")
    private String approveStatus;

    /**
     * 单据编号
     */
    @TableField("code")
    private String code;

    /**
     * 申请日期
     */
    @TableField("apply_date")
    private LocalDate applyDate;

    /**
     * 申请人id
     */
    @TableField("apply_user_id")
    private String applyUserId;

    /**
     * 申请人名称
     */
    @TableField("apply_user_name")
    private String applyUserName;

    /**
     * 申请人部门id
     */
    @TableField("apply_dept_id")
    private String applyDeptId;

    /**
     * 申请人部门名称
     */
    @TableField("apply_dept_name")
    private String applyDeptName;

    /**
     * 新品首批（false否,true是）
     */
    @TableField("is_first_mass_product")
    private Boolean isFirstMassProduct;

    /**
     * 审核时间
     */
    @TableField("approve_time")
    private LocalDateTime approveTime;

    /**
     * 审核人id
     */
    @TableField("approve_user_id")
    private String approveUserId;

    /**
     * 审核人名称
     */
    @TableField("approve_user_name")
    private String approveUserName;

    /**
     * 来源单据ID
     */
    @TableField("source_id")
    private String sourceId;

    /**
     * 来源单据编码
     */
    @TableField("source_code")
    private String sourceCode;

    /**
     * 来源单据类型
     */
    @TableField("source_type")
    private String sourceType;


    public static final String APPROVE_STATUS = "approve_status";

    public static final String FIELD_CODE = "code";

    public static final String APPLY_DATE = "apply_date";

    public static final String APPLY_USER_ID = "apply_user_id";

    public static final String APPLY_USER_NAME = "apply_user_name";

    public static final String APPLY_DEPT_ID = "apply_dept_id";

    public static final String APPLY_DEPT_NAME = "apply_dept_name";

    public static final String IS_FIRST_MASS_PRODUCT = "is_first_mass_product";

    public static final String APPROVE_TIME = "approve_time";

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_CODE = "source_code";

    public static final String SOURCE_TYPE = "source_type";


    @Override
    public Serializable pkVal() {
        return null;
    }

}
