package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;

import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 样品期初台账
 * </p>
 *
 * @author wuhaotian
 * @since 2025-08-21
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("sample_initial_ledger")
public class SampleInitialLedgerEntity extends BaseEntity<SampleInitialLedgerEntity> {

    /**
     * 审批状态
     */
    @TableField("approve_status")
    private String approveStatus;

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
     * 审批时间
     */
    @TableField("approve_time")
    private Date approveTime;

    /**
     * 作废状态
     */
    @TableField("invalid_status")
    private Boolean invalidStatus;

    /**
     * 作废原因
     */
    @TableField("invalid_remark")
    private String invalidRemark;

    /**
     * 作废时间
     */
    @TableField("invalid_time")
    private Date invalidTime;

    /**
     * 期初台账单号
     */
    @TableField("code")
    private String code;

    /**
     * 单据状态
     */
    @TableField("status")
    private String status;

    /**
     * 归属人ID
     */
    @TableField("user_id")
    private String userId;

    /**
     * 归属人姓名
     */
    @TableField("user_name")
    private String userName;

    /**
     * 归属部门ID
     */
    @TableField("dept_id")
    private String deptId;

    /**
     * 业务日期
     */
    @TableField("bill_date")
    private Date billDate;

    /**
     * SKU编码
     */
    @TableField("sku_no")
    private String skuNo;

    /**
     * 产品名称
     */
    @TableField("product_name")
    private String productName;

    /**
     * 数量
     */
    @TableField("qty")
    private Integer qty;


    public static final String APPROVE_STATUS = "approve_status";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String APPROVE_TIME = "approve_time";

    public static final String INVALID_STATUS = "invalid_status";

    public static final String INVALID_REMARK = "invalid_remark";

    public static final String INVALID_TIME = "invalid_time";

    public static final String CODE = "code";

    public static final String STATUS = "status";

    public static final String USER_ID = "user_id";

    public static final String USER_NAME = "user_name";

    public static final String DEPT_ID = "dept_id";

    public static final String BILL_DATE = "bill_date";

    public static final String SKU_NO = "sku_no";

    public static final String PRODUCT_NAME = "product_name";

    public static final String QTY = "qty";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
