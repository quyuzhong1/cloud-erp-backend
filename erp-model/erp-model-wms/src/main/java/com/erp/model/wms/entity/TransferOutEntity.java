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
 * 分布式调出单
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("transfer_out")
public class TransferOutEntity extends BaseEntity<TransferOutEntity> {

    /**
     * 单号
     */
    @TableField("code")
    private String code;

    /**
     * 审核状态 
     */
    @TableField("approve_status")
    private String approveStatus;

    /**
     * 调拨类型
     */
    @TableField("type")
    private String type;

    /**
     * 调出日期
     */
    @TableField("bill_date")
    private LocalDate billDate;

    /**
     * 调出仓库id
     */
    @TableField("out_warehouse_id")
    private String outWarehouseId;

    /**
     * 调出仓库名
     */
    @TableField("out_warehouse_name")
    private String outWarehouseName;

    /**
     * 调入仓库id
     */
    @TableField("in_warehouse_id")
    private String inWarehouseId;

    /**
     * 调入仓库
     */
    @TableField("in_warehouse_name")
    private String inWarehouseName;

    /**
     * 仓管员id
     */
    @TableField("warehouse_keeper_id")
    private String warehouseKeeperId;

    /**
     * 仓管员
     */
    @TableField("warehouse_keeper_name")
    private String warehouseKeeperName;

    /**
     * 调拨方向
     */
    @TableField("transfer_direction")
    private String transferDirection;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 作废状态 true 作废
     */
    @TableField("invalid_status")
    private Boolean invalidStatus;

    /**
     * 作废原因
     */
    @TableField("invalid_remark")
    private String invalidRemark;

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
     * 调入组织id
     */
    @TableField("in_org_id")
    private String inOrgId;

    /**
     * 调入组织名称
     */
    @TableField("in_org_name")
    private String inOrgName;


    /**
     * 调出组织id
     */
    @TableField("out_org_id")
    private String outOrgId;

    /**
     * 调出组织名称
     */
    @TableField("out_org_name")
    private String outOrgName;

    /**
     * 来源单号
     */
    @TableField("source_code")
    private String sourceCode;

    /**
     * 在途归属
     */
    @TableField("transit_owner")
    private String transitOwner;

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

    

    public static final String APPROVE_STATUS = "approve_status";

    

    public static final String BILL_DATE = "bill_date";

    public static final String OUT_WAREHOUSE_ID = "out_warehouse_id";

    public static final String OUT_WAREHOUSE_NAME = "out_warehouse_name";

    public static final String IN_WAREHOUSE_ID = "in_warehouse_id";

    public static final String IN_WAREHOUSE_NAME = "in_warehouse_name";

    public static final String WAREHOUSE_KEEPER_ID = "warehouse_keeper_id";

    public static final String WAREHOUSE_KEEPER_NAME = "warehouse_keeper_name";

    public static final String TRANSFER_DIRECTION = "transfer_direction";

    

    public static final String INVALID_STATUS = "invalid_status";

    public static final String IN_ORG_ID = "in_org_id";

    public static final String IN_ORG_NAME = "in_org_name";

    public static final String OUT_ORG_ID = "out_org_id";

    public static final String OUT_ORG_NAME = "out_org_name";

    public static final String SOURCE_CODE = "source_code";

    public static final String TRANSIT_OWNER = "transit_owner";

    public static final String INVALID_REMARK = "invalid_remark";



    @Override
    public Serializable pkVal() {
        return null;
    }

}
