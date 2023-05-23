package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.erp.model.wms.enums.QcBillStatusEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 质检单表
 * </p>
 *
 * @author lambda
 * @since 2023-04-14
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("qc_info")
public class QcInfoEntity extends BaseEntity<QcInfoEntity> {

    /**
     * 质检单号
     */
    @TableField("code")
    private String code;

    /**
     * 质检部门id
     */
    @TableField("qc_dept_id")
    private String qcDeptId;

    /**
     * 质检部门name
     */
    @TableField("qc_dept_name")
    private String qcDeptName;

    /**
     * 质检人
     */
    @TableField("qc_user_id")
    private String qcUserId;

    /**
     * 质检人名
     */
    @TableField("qc_user_name")
    private String qcUserName;

    /**
     * 采购订单id
     */
    @TableField("purchase_order_id")
    private String purchaseOrderId;

    /**
     * 采购订单编号
     */
    @TableField("purchase_order_code")
    private String purchaseOrderCode;

    /**
     * 质检状态
     */
    @TableField("qc_status")
    private QcBillStatusEnum qcStatus;

    /**
     * 质检日期
     */
    @TableField("qc_date")
    private LocalDate qcDate;

    /**
     * 质检结束时间
     */
    @TableField("qc_finish_time")
    private LocalDateTime qcFinishTime;

    /**
     * 质检来源
     */
    @TableField("source_type")
    private String sourceType;

    /**
     * 来源id
     */
    @TableField("source_id")
    private String sourceId;

    /**
     * 供应商id
     */
    @TableField("supplier_id")
    private String supplierId;

    /**
     * 仓库id
     */
    @TableField("warehouse_id")
    private String warehouseId;

    /**
     * 来源详情id
     */
    @TableField("source_detail_id")
    private String sourceDetailId;


    public static final String CODE = "code";

    public static final String QC_DEPT_ID = "qc_dept_id";

    public static final String QC_DEPT_NAME = "qc_dept_name";

    public static final String QC_USER_ID = "qc_user_id";

    public static final String QC_USER_NAME = "qc_user_name";

    public static final String PURCHASE_ORDER_ID = "purchase_order_id";

    public static final String PURCHASE_ORDER_CODE = "purchase_order_code";

    public static final String QC_STATUS = "qc_status";

    public static final String QC_DATE = "qc_date";

    public static final String SOURCE_TYPE = "source_type";

    public static final String SOURCE_ID = "source_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
