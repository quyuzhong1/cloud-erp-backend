package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 期初库存表
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-05-10
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("init_stock")
public class InitStockEntity extends BaseEntity<InitStockEntity> {

    /**
     * 单据编号
     */
    @TableField("code")
    private String code;

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
     * 业务类型
     */
    @TableField("dict_trade_type")
    private String dictTradeType;

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
     * 仓库名称
     */
    @TableField("warehouse_name")
    private String warehouseName;

    /**
     * 库存状态
     */
    @TableField("inventory_status")
    private String inventoryStatus;

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
    private Date approveTime;

    /**
     * 作废原因
     */
    @TableField("invalid_remark")
    private String invalidRemark;

    /**
     * 作废状态 true已作废，false未作废
     */
    @TableField("invalid_status")
    private Boolean invalidStatus;


    public static final String CODE = "code";

    public static final String BILL_DATE = "bill_date";

    public static final String APPROVE_STATUS = "approve_status";

    public static final String DICT_TRADE_TYPE = "dict_trade_type";

    public static final String ORG_ID = "org_id";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String INVENTORY_STATUS = "inventory_status";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String APPROVE_TIME = "approve_time";

    public static final String INVALID_REMARK = "invalid_remark";

    public static final String INVALID_STATUS = "invalid_status";

    public static final String WAREHOUSE_NAME = "warehouse_name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
