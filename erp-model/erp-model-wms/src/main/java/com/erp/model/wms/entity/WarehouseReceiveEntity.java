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
 * 采购退货单
 * </p>
 *
 * @author LUO_WG
 * @since 2023-04-06
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("po_receive")
public class WarehouseReceiveEntity extends BaseEntity<WarehouseReceiveEntity> {

    /**
     * 审核状态 waitSubmit待提交，approveIng审核中，reject审核不通过，approve已审核
     */
    @TableField("approve_status")
    private String approveStatus;

    /**
     * 单据编号
     */
    @TableField("code")
    private String code;

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
     * 收货人id
     */
    @TableField("receive_user_id")
    private String receiveUserId;

    /**
     * 收货人名称
     */
    @TableField("receive_user_name")
    private String receiveUserName;

    /**
     * 收货人部门id
     */
    @TableField("receive_dept_id")
    private String receiveDeptId;

    /**
     * 收货人部门名称
     */
    @TableField("receive_dept_name")
    private String receiveDeptName;

    /**
     * 收货人组织id
     */
    @TableField("receive_org_id")
    private String receiveOrgId;

    /**
     * 收货人组织名称
     */
    @TableField("receive_org_name")
    private String receiveOrgName;

    /**
     * 收货日期
     */
    @TableField("bill_date")
    private LocalDate billDate;

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
     * 作废描述
     */
    @TableField("invalid_remark")
    private String invalidRemark;

    /**
     * 审核人id
     */
    @TableField("approve_user_id")
    private String approveUserId;

    /**
     * 审核人名称
     */
    @TableField("approve_user_name")
    private String approveUserName;

    /**
     * 审核时间
     */
    @TableField("approve_time")
    private LocalDateTime approveTime;

    /**
     * 交货仓库id
     */
    @TableField("delivery_warehouse_id")
    private String deliveryWarehouseId;

    /**
     * 交货仓库名称
     */
    @TableField("delivery_warehouse_name")
    private String deliveryWarehouseName;

    /**
     * 采购员id
     */
    @TableField("purchase_user_id")
    private String purchaseUserId;

    /**
     * 采购员名称
     */
    @TableField("purchase_user_name")
    private String purchaseUserName;

    /**
     * 是否是委外
     */
    @TableField("is_subcontract")
    private Boolean isSubcontract;

    @Override
    public Serializable pkVal() {
        return null;
    }

}
