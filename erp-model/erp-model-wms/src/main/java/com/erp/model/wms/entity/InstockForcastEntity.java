package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * <p>
 * 入库预报表
 * </p>
 *
 * @author lambda
 * @since 2023-05-09
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("instock_forcast")
public class InstockForcastEntity extends BaseEntity<InstockForcastEntity> {

    /**
     * 单据编号
     */
    @TableField("code")
    private String code;

    /**
     * 采购订单id
     */
    @TableField("purchase_order_id")
    private String purchaseOrderId;

    /**
     * 仓库组织id
     */
    @TableField("org_id")
    private String orgId;

    /**
     * 仓库id
     */
    @TableField("warehouse_id")
    private String warehouseId;

    /**
     * 单据日期
     */
    @TableField("bill_date")
    private LocalDate billDate;

    /**
     * 审核状态
     */
    @TableField("approve_status")
    private String approveStatus;

    /**
     * 审核人名称
     */
    @TableField("approve_user_name")
    private String approveUserName;

    /**
     * 审核人id
     */
    @TableField("approve_user_id")
    private String approveUserId;

    /**
     * 审核时间
     */
    @TableField("approve_time")
    private LocalDateTime approveTime;

    /**
     * 作废状态 true已作废，false未作废
     */
    @TableField("invalid_status")
    private Boolean invalidStatus;

    /**
     * 作废原因
     */
    @TableField("invalid_remark")
    private String invalidRemark;

    /**
     * 采购订单编号
     */
    @TableField("purchase_order_code")
    private String purchaseOrderCode;


    public static final String CODE = "code";

    public static final String PURCHASE_ORDER_ID = "purchase_order_id";

    public static final String ORG_ID = "org_id";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String BILL_DATE = "bill_date";

    public static final String APPROVE_STATUS = "approve_status";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String APPROVE_TIME = "approve_time";

    public static final String INVALID_STATUS = "invalid_status";

    public static final String INVALID_REMARK = "invalid_remark";

    public static final String PURCHASE_ORDER_CODE = "purchase_order_code";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
