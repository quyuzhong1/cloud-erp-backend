package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 样品借用单
 * </p>
 *
 * @author jack
 * @since 2025-08-20
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("sample_borrow_info")
public class SampleBorrowInfoEntity extends BaseEntity<SampleBorrowInfoEntity> {

    /**
    * 单据编号
    */
    @TableField("code")
    private String code;
    /**
    * 审批状态
    */
    @TableField("approve_status")
    private ApproveStatusEnum approveStatus;
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
    * 是否作废
    */
    @TableField("invalid_status")
    private Boolean invalidStatus;
    /**
    * 借用人ID
    */
    @TableField("borrow_user_id")
    private String borrowUserId;
    /**
    * 借用人姓名
    */
    @TableField("borrow_user_name")
    private String borrowUserName;
    /**
    * 借用部门ID
    */
    @TableField("borrow_dept_id")
    private String borrowDeptId;
    /**
    * 借用部门名称
    */
    @TableField("borrow_dept_name")
    private String borrowDeptName;
    /**
    * 借出人ID
    */
    @TableField("lend_user_id")
    private String lendUserId;
    /**
    * 借出人姓名
    */
    @TableField("lend_user_name")
    private String lendUserName;
    /**
    * 借出部门ID
    */
    @TableField("lend_dept_id")
    private String lendDeptId;
    /**
    * 借出部门名称
    */
    @TableField("lend_dept_name")
    private String lendDeptName;
    /**
    * 借用日期
    */
    @TableField("borrow_date")
    private LocalDate borrowDate;
    /**
    * 预计退回日期
    */
    @TableField("estimated_return_date")
    private LocalDate estimatedReturnDate;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;


    public static final String CODE = "code";

    public static final String APPROVE_STATUS = "approve_status";

    public static final String APPROVE_TIME = "approve_time";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String INVALID_STATUS = "invalid_status";

    public static final String BORROW_USER_ID = "borrow_user_id";

    public static final String BORROW_USER_NAME = "borrow_user_name";

    public static final String BORROW_DEPT_ID = "borrow_dept_id";

    public static final String BORROW_DEPT_NAME = "borrow_dept_name";

    public static final String LEND_USER_ID = "lend_user_id";

    public static final String LEND_USER_NAME = "lend_user_name";

    public static final String LEND_DEPT_ID = "lend_dept_id";

    public static final String LEND_DEPT_NAME = "lend_dept_name";

    public static final String BORROW_DATE = "borrow_date";

    public static final String ESTIMATED_RETURN_DATE = "estimated_return_date";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}