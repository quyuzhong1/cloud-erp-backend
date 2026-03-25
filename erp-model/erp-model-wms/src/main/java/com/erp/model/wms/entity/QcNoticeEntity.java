package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * <p>
 * 质检通知单
 * </p>
 *
 * @author jack
 * @since 2025-04-21
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("qc_notice")
public class QcNoticeEntity extends BaseEntity<QcNoticeEntity> {

    /**
     * 采购订单id
     */
    @TableField("purchase_order_id")
    private String purchaseOrderId;
    /**
     * 采购订单编码
     */
    @TableField("purchase_order_code")
    private String purchaseOrderCode;
    /**
     * 供应商id
     */
    @TableField("supplier_id")
    private String supplierId;
    /**
    * 单据状态
    */
    @TableField("approve_status")
    private ApproveStatusEnum approveStatus;
    /**
    * 审核时间
    */
    @TableField("approve_time")
    private LocalDateTime approveTime;
    /**
    * 最新审核人ID
    */
    @TableField("approve_user_id")
    private String approveUserId;
    /**
    * 最新审核人
    */
    @TableField("approve_user_name")
    private String approveUserName;
    /**
    * 质检通知单号
    */
    @TableField("code")
    private String code;
    /**
    * 质检仓库
    */
    @TableField("qc_warehouse_id")
    private String qcWarehouseId;
    /**
    * 上架仓库
    */
    @TableField("putaway_warehouse_id")
    private String putawayWarehouseId;
    /**
    * 质检类型 stockIn 入库质检   outsideQc 外检质检  insideQc  在库质检   newProductStockIn 新品入库质检  b2bOutsideQc  B2B外检  
    */
    @TableField("qc_type")
    private String qcType;
    /**
     * 质检状态 QcBillStatusEnum
     */
    @TableField("qc_status")
    private String qcStatus;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 质检时效
    */
    @TableField("qc_timeliness")
    private Integer qcTImeliness;
    
    /**
    * 是否作废
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
     * 来源编码
     */
    @TableField("source_code")
    private String sourceCode;

    /**
     * 来源类型
     */
    @TableField("source_type")
    private String sourceType;


    public static final String APPROVE_STATUS = "approve_status";

    public static final String APPROVE_TIME = "approve_time";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String CODE = "code";

    public static final String QC_WAREHOUSE_ID = "qc_warehouse_id";

    public static final String PUTAWAY_WAREHOUSE_ID = "putaway_warehouse_id";

    public static final String QC_TYPE = "qc_type";

    public static final String QC_STATUS = "qc_status";

    public static final String REMARK = "remark";

    public static final String QC_TIMELINESS = "qc_timeliness";

    public static final String INVALID_STATUS = "invalid_status";

    public static final String INVALID_TIME = "invalid_time";

    public static final String INVALID_USER_ID = "invalid_user_id";

    public static final String INVALID_USER_NAME = "invalid_user_name";

    public static final String INVALID_REMARK = "invalid_remark";

    @TableField(exist = false)
    private String qcWarehouseName;
    @TableField(exist = false)
    private String putawayWarehouseName;

    @Override
    public Serializable pkVal() {
        return null;
    }

}