package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;


/**
 * <p>
 * 委外退料单
 * </p>
 *
 * @author zdy
 * @since 2024-09-15
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("subcontract_return")
public class SubcontractReturnEntity extends BaseEntity<SubcontractReturnEntity> {

    /**
    * 委外退料单号
    */
    @TableField("code")
    private String code;
    /**
    * 单据审核状态
    */
    @TableField("approve_status")
    private ApproveStatusEnum approveStatus;
    /**
    * 退料类型
    */
    @TableField("type")
    private String type;
    /**
    * 退料日期
    */
    @TableField("bill_date")
    private LocalDate billDate;
    /**
    * 来源id
    */
    @TableField("source_id")
    private String sourceId;
    /**
    * 来源编号
    */
    @TableField("source_code")
    private String sourceCode;
    /**
    * 来源类型
    */
    @TableField("source_type")
    private String sourceType;
    /**
    * 作废状态（false未作废，true已作废）
    */
    @TableField("invalid_status")
    private Boolean invalidStatus;
    /**
    * 作废时间
    */
    @TableField("invalid_time")
    private LocalDateTime invalidTime;
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
    * 金蝶数据id
    */
    @TableField("sync_kingdee_id")
    private String syncKingdeeId;
    /**
    * 供应商id
    */
    @TableField("supplier_id")
    private String supplierId;
    /**
    * 供应商名称
    */
    @TableField("supplier_name")
    private String supplierName;
    /**
    * 委外订单id
    */
    @TableField("subcontract_order_id")
    private String subcontractOrderId;
    /**
    * 委外订单编号
    */
    @TableField("subcontract_order_code")
    private String subcontractOrderCode;


    

    public static final String APPROVE_STATUS = "approve_status";

    

    public static final String BILL_DATE = "bill_date";

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_CODE = "source_code";

    public static final String SOURCE_TYPE = "source_type";

    public static final String INVALID_STATUS = "invalid_status";

    public static final String INVALID_TIME = "invalid_time";

    public static final String INVALID_REMARK = "invalid_remark";

    public static final String APPROVE_TIME = "approve_time";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String SYNC_KINGDEE_ID = "sync_kingdee_id";

    public static final String SUPPLIER_ID = "supplier_id";

    public static final String SUPPLIER_NAME = "supplier_name";

    public static final String SUBCONTRACT_ORDER_ID = "subcontract_order_id";

    public static final String SUBCONTRACT_ORDER_CODE = "subcontract_order_code";

    @Override
    public Serializable pkVal() {
        return null;
    }

}