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

/**
 * <p>
 * 直接调拨单主表
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("transfer_info")
public class TransferInfoEntity extends BaseEntity<TransferInfoEntity> {

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
    private LocalDate billDate;

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
     * 仓管员id
     */
    @TableField("warehouse_keeper_id")
    private String warehouseKeeperId;

    /**
     * 仓管员名称
     */
    @TableField("warehouse_keeper_name")
    private String warehouseKeeperName;

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
    private LocalDateTime approveTime;

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

    public static final String WAREHOUSE_KEEPER_ID = "warehouse_keeper_id";

    public static final String WAREHOUSE_KEEPER_NAME = "warehouse_keeper_name";

    public static final String TRANSFER_DIRECTION = "transfer_direction";

    public static final String INVALID_STATUS = "invalid_status";

    public static final String INVALID_REMARK = "invalid_remark";

    public static final String APPROVE_TIME = "approve_time";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String REMARK = "remark";

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_TYPE = "source_type";


    @Override
    public Serializable pkVal() {
        return null;
    }

}
