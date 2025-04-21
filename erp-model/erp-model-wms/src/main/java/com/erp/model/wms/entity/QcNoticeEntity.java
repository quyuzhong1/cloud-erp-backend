package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


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
    * 备注
    */
    @TableField("remark")
    private String remark;


    public static final String APPROVE_STATUS = "approve_status";

    public static final String APPROVE_TIME = "approve_time";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String CODE = "code";

    public static final String QC_WAREHOUSE_ID = "qc_warehouse_id";

    public static final String PUTAWAY_WAREHOUSE_ID = "putaway_warehouse_id";

    public static final String QC_TYPE = "qc_type";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}