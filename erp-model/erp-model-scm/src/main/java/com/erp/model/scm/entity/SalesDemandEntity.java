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
 * 销售需求主表
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("sales_demand")
public class SalesDemandEntity extends BaseEntity<SalesDemandEntity> {

    /**
     * 审核状态 （waitSubmit待提交，approveIng审核中，reject审核不通过，approve已审核）
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
     * 店铺id
     */
    @TableField("shop_id")
    private String shopId;

    /**
     * 店铺名称
     */
    @TableField("shop_name")
    private String shopName;

    /**
     * 备货原因
     */
    @TableField("remark")
    private String remark;

    /**
     * 作废状态（false未作废，true已作废）
     */
    @TableField("invalid_status")
    private Boolean invalidStatus;

    /**
     * 作废时间
     */
    @TableField("invalid_time")
    private LocalDateTime invalidTime;

    /**
     * 作废原因
     */
    @TableField("invalid_remark")
    private String invalidRemark;

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
     * 来源id
     */
    @TableField("source_id")
    private String sourceId;

    /**
     * 来源类型
     */
    @TableField("source_type")
    private String sourceType;

    /**
     * 来源编码
     */
    @TableField("source_code")
    private String sourceCode;


    public static final String APPROVE_STATUS = "approve_status";

    public static final String CODE = "code";

    public static final String APPLY_DATE = "apply_date";

    public static final String APPLY_USER_ID = "apply_user_id";

    public static final String APPLY_USER_NAME = "apply_user_name";

    public static final String APPLY_DEPT_ID = "apply_dept_id";

    public static final String APPLY_DEPT_NAME = "apply_dept_name";

    public static final String IS_FIRST_MASS_PRODUCT = "is_first_mass_product";

    public static final String SHOP_ID = "shop_id";

    public static final String SHOP_NAME = "shop_name";

    public static final String REMARK = "remark";

    public static final String INVALID_STATUS = "invalid_status";

    public static final String INVALID_TIME = "invalid_time";

    public static final String APPROVE_TIME = "approve_time";

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_TYPE = "source_type";

    public static final String SOURCE_CODE = "source_code";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
