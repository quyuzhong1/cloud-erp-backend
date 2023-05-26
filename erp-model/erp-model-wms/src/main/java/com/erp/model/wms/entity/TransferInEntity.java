package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.entity.BaseEntity;
import com.erp.model.wms.enums.TransferDirectionEnum;
import com.erp.model.wms.enums.TransferTypeEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * <p>
 * 分布式调入单
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("transfer_in")
public class TransferInEntity extends BaseEntity<TransferInEntity> {

    /**
     * 单号
     */
    @TableField("code")
    private String code;

    /**
     * 审核状态 
     */
    @TableField("approve_status")
    private ApproveStatusEnum approveStatus;

    /**
     * 调拨类型
     */
    @TableField("type")
    private TransferTypeEnum transferType;

    /**
     * 调入日期
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
    private TransferDirectionEnum transferDirection;

    /**
     * 备注
     */
    @TableField("approve_user_name")
    private String approveUserName;

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

    public static final String TYPE = "type";

    public static final String BILL_DATE = "bill_date";

    public static final String OUT_WAREHOUSE_ID = "out_warehouse_id";

    public static final String OUT_WAREHOUSE_NAME = "out_warehouse_name";

    public static final String IN_WAREHOUSE_ID = "in_warehouse_id";

    public static final String IN_WAREHOUSE_NAME = "in_warehouse_name";

    public static final String WAREHOUSE_KEEPER_ID = "warehouse_keeper_id";

    public static final String WAREHOUSE_KEEPER_NAME = "warehouse_keeper_name";

    public static final String TRANSFER_DIRECTION = "transfer_direction";

    public static final String REMARK = "remark";

    public static final String INVALID_STATUS = "invalid_status";

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_TYPE = "source_type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
