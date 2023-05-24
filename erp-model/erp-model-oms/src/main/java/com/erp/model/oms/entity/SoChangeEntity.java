package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * <p>
 * 销售订单变更
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("so_change")
public class SoChangeEntity extends BaseEntity<SoChangeEntity> {

    /**
     * code
     */
    @TableField("code")
    private String code;

    /**
     * 审核状态
     */
    @TableField("approve_status")
    private ApproveStatusEnum approveStatus;



    /**
     * 变更日期
     */
    @TableField("bill_date")
    private LocalDate billDate;

    /**
     * 销售订单id
     */
    @TableField("so_id")
    private String soId;

    /**
     * 变更人id
     */
    @TableField("user_id")
    private String userId;

    /**
     * 变更人
     */
    @TableField("user_name")
    private String userName;

    /**
     * 变更部门id
     */
    @TableField("dept_id")
    private String deptId;

    /**
     * 变更部门
     */
    @TableField("dept_name")
    private String deptName;

    @TableField("invalid_status")
    private Boolean invalidStatus;

    @TableField("invalid_remark")
    private Boolean invalidRemark;

    /**
     * 变更原因
     */
    @TableField("remark")
    private String remark;



    public static final String CODE = "code";

    public static final String APPROVE_STATUS = "approve_status";

    public static final String CUSTOMER_ID = "customer_id";

    public static final String RECEIVER_NAME = "receiver_name";

    public static final String TEL_NUMBER = "tel_number";

    public static final String RECEIVE_ADDRESS = "receive_address";

    public static final String DELIVERY_MODE = "delivery_mode";

    public static final String CURRENCY = "currency";

    public static final String IS_TAX = "is_tax";

    public static final String BILL_DATE = "bill_date";

    public static final String SO_ID = "so_id";

    public static final String USER_ID = "user_id";

    public static final String USER_NAME = "user_name";

    public static final String DEPT_ID = "dept_id";

    public static final String DEPT_NAME = "dept_name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
