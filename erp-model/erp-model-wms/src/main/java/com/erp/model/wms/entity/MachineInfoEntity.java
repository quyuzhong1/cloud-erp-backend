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
 * 加工单
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("machine_info")
public class MachineInfoEntity extends BaseEntity<MachineInfoEntity> {

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
     * 事务类型
     */
    @TableField("work_type")
    private String workType;

    /**
     * 收货仓库id
     */
    @TableField("warehouse_id")
    private String warehouseId;

    /**
     * 收货仓库名称
     */
    @TableField("warehouse_name")
    private String warehouseName;

    /**
     * 库存组织id
     */
    @TableField("inventory_org_id")
    private String inventoryOrgId;

    /**
     * 库存组织名称
     */
    @TableField("inventory_org_name")
    private String inventoryOrgName;

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
     * 领料人id
     */
    @TableField("receiver_id")
    private String receiverId;

    /**
     * 领料人名称
     */
    @TableField("receiver_name")
    private String receiverName;

    /**
     * 领料组织id
     */
    @TableField("receive_org_id")
    private String receiveOrgId;

    /**
     * 领料组织名称
     */
    @TableField("receive_org_name")
    private String receiveOrgName;

    /**
     * 单据类型
     */
    @TableField("type")
    private String type;

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
     * 同步金蝶状态（默认0无需同步,1待同步,2同步中,3同步成功,4同步失败）
     */
    @TableField("sync_kingdee_status")
    private String syncKingdeeStatus;

    /**
     * 同步金蝶时间
     */
    @TableField("sync_kingdee_time")
    private LocalDateTime syncKingdeeTime;

    /**
     * 同步金蝶id
     */
    @TableField("sync_kingdee_id")
    private String syncKingdeeId;

    /**
     * 同步操作
     */
    @TableField("sync_operate")
    private String syncOperate;


    public static final String CODE = "code";

    public static final String APPROVE_STATUS = "approve_status";

    public static final String BILL_DATE = "bill_date";

    public static final String WORK_TYPE = "work_type";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String WAREHOUSE_NAME = "warehouse_name";

    public static final String INVENTORY_ORG_ID = "inventory_org_id";

    public static final String INVENTORY_ORG_NAME = "inventory_org_name";

    public static final String WAREHOUSE_KEEPER_ID = "warehouse_keeper_id";

    public static final String WAREHOUSE_KEEPER_NAME = "warehouse_keeper_name";

    public static final String RECEIVER_ID = "receiver_id";

    public static final String RECEIVER_NAME = "receiver_name";

    public static final String RECEIVE_ORG_ID = "receive_org_id";

    public static final String RECEIVE_ORG_NAME = "receive_org_name";

    public static final String TYPE = "type";

    public static final String INVALID_STATUS = "invalid_status";

    public static final String INVALID_REMARK = "invalid_remark";

    public static final String APPROVE_TIME = "approve_time";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String SYNC_KINGDEE_STATUS = "sync_kingdee_status";

    public static final String SYNC_KINGDEE_TIME = "sync_kingdee_time";

    public static final String SYNC_KINGDEE_ID = "sync_kingdee_id";

    public static final String SYNC_OPERATE = "sync_operate";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
