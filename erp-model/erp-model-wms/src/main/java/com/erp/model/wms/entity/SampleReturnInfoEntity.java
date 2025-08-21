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
 * 样品归还单主表
 * </p>
 *
 * @author jack
 * @since 2025-08-20
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("sample_return_info")
public class SampleReturnInfoEntity extends BaseEntity<SampleReturnInfoEntity> {

    /**
    * 归还单号
    */
    @TableField("code")
    private String code;
    /**
    * 来源单据ID
    */
    @TableField("source_id")
    private String sourceId;
    /**
    * 来源单据编号
    */
    @TableField("source_code")
    private String sourceCode;
    /**
    * 来源类型
    */
    @TableField("source_type")
    private String sourceType;
    /**
    * 审批状态
    */
    @TableField("approve_status")
    private ApproveStatusEnum approveStatus;
    /**
    * 审批完成时间
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
    * 归还人ID
    */
    @TableField("return_user_id")
    private String returnUserId;
    /**
    * 归还人姓名
    */
    @TableField("return_user_name")
    private String returnUserName;
    /**
    * 归还人部门ID
    */
    @TableField("return_dept_id")
    private String returnDeptId;
    /**
    * 归还人部门名称
    */
    @TableField("return_dept_name")
    private String returnDeptName;
    /**
    * 接收人ID
    */
    @TableField("receiver_user_id")
    private String receiverUserId;
    /**
    * 接收人姓名
    */
    @TableField("receiver_user_name")
    private String receiverUserName;
    /**
    * 接收人部门ID
    */
    @TableField("receiver_dept_id")
    private String receiverDeptId;
    /**
    * 接收人部门名称
    */
    @TableField("receiver_dept_name")
    private String receiverDeptName;
    /**
    * 归还日期
    */
    @TableField("return_date")
    private LocalDate returnDate;
    /**
    * 备注说明
    */
    @TableField("remark")
    private String remark;


    public static final String CODE = "code";

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_CODE = "source_code";

    public static final String SOURCE_TYPE = "source_type";

    public static final String APPROVE_STATUS = "approve_status";

    public static final String APPROVE_TIME = "approve_time";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String INVALID_STATUS = "invalid_status";

    public static final String RETURN_USER_ID = "return_user_id";

    public static final String RETURN_USER_NAME = "return_user_name";

    public static final String RETURN_DEPT_ID = "return_dept_id";

    public static final String RETURN_DEPT_NAME = "return_dept_name";

    public static final String RECEIVER_USER_ID = "receiver_user_id";

    public static final String RECEIVER_USER_NAME = "receiver_user_name";

    public static final String RECEIVER_DEPT_ID = "receiver_dept_id";

    public static final String RECEIVER_DEPT_NAME = "receiver_dept_name";

    public static final String RETURN_DATE = "return_date";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}