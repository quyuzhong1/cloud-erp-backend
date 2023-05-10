package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("transfer_application")
public class TransferApplicationEntity extends BaseEntity<TransferApplicationEntity> {

    /**
     * 单据编号
     */
    @TableField("code")
    private String code;

    /**
     * 审核状态 
     */
    @TableField("approve_status")
    private String approveStatus;

    /**
     * 单据日期
     */
    @TableField("bill_date")
    private Date billDate;

    /**
     * 调拨类型
     */
    @TableField("type")
    private String type;

    /**
     * 调入仓库id
     */
    @TableField("in_warehouse_id")
    private String inWarehouseId;

    /**
     * 调入仓库名称
     */
    @TableField("in_warehouse_name")
    private String inWarehouseName;

    /**
     * 调出仓库id
     */
    @TableField("out_warehouse_id")
    private String outWarehouseId;

    /**
     * 调出仓库名称
     */
    @TableField("out_warehouse_name")
    private String outWarehouseName;

    /**
     * 调入库存组织id
     */
    @TableField("in_org_id")
    private String inOrgId;

    /**
     * 调入库存组织名称
     */
    @TableField("in_org_name")
    private String inOrgName;

    /**
     * 调出库存组织id
     */
    @TableField("out_org_id")
    private String outOrgId;

    /**
     * 调出库存组织名称
     */
    @TableField("out_org_name")
    private String outOrgName;

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
     * 申请日期
     */
    @TableField("apply_date")
    private Date applyDate;

    /**
     * 调拨方向
     */
    @TableField("transfer_direction")
    private String transferDirection;

    /**
     * 作废状态（false未作废，true已作废）
     */
    @TableField("invalid_status")
    private Boolean invalidStatus;

    /**
     * 作废原因
     */
    @TableField("invalid_remark")
    private String invalidRemark;

    /**
     * 审核时间
     */
    @TableField("approve_time")
    private Date approveTime;

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
     * 备注
     */
    @TableField("remark")
    private String remark;


    public static final String CODE = "code";

    public static final String APPROVE_STATUS = "approve_status";

    public static final String BILL_DATE = "bill_date";

    public static final String TYPE = "type";

    public static final String IN_WAREHOUSE_ID = "in_warehouse_id";

    public static final String IN_WAREHOUSE_NAME = "in_warehouse_name";

    public static final String OUT_WAREHOUSE_ID = "out_warehouse_id";

    public static final String OUT_WAREHOUSE_NAME = "out_warehouse_name";

    public static final String IN_ORG_ID = "in_org_id";

    public static final String IN_ORG_NAME = "in_org_name";

    public static final String OUT_ORG_ID = "out_org_id";

    public static final String OUT_ORG_NAME = "out_org_name";

    public static final String APPLY_USER_ID = "apply_user_id";

    public static final String APPLY_USER_NAME = "apply_user_name";

    public static final String APPLY_DATE = "apply_date";

    public static final String TRANSFER_DIRECTION = "transfer_direction";

    public static final String INVALID_STATUS = "invalid_status";

    public static final String INVALID_REMARK = "invalid_remark";

    public static final String APPROVE_TIME = "approve_time";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
