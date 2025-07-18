package com.erp.model.scm.entity;

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
 * 采购订单表
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("purchase_order")
public class PurchaseOrderEntity extends BaseEntity<PurchaseOrderEntity> {

    /**
     * 单据类型
     */
    private String type;

    /**
     * 审核状态
     */
    @TableField("approve_status")
    private String approveStatus;

    /**
     * 单据编号
     */
    @TableField("code")
    private String code;

    /**
     * 采购日期
     */
    @TableField("purchase_date")
    private LocalDate purchaseDate;

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
     * 采购组织id
     */
    @TableField("purchase_org_id")
    private String purchaseOrgId;

    /**
     * 采购组织名称
     */
    @TableField("purchase_org_name")
    private String purchaseOrgName;

    /**
     * 采购部门id
     */
    @TableField("purchase_dept_id")
    private String purchaseDeptId;

    /**
     * 采购部门名称
     */
    @TableField("purchase_dept_name")
    private String purchaseDeptName;

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
     * 作废原因
     */
    @TableField("invalid_remark")
    private String invalidRemark;

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
     * 收料组织id
     */
    @TableField("receive_org_id")
    private String receiveOrgId;

    /**
     * 收料组织名称
     */
    @TableField("receive_org_name")
    private String receiveOrgName;


    /**
     * 金蝶数据id
     */
    @TableField("sync_kingdee_id")
    private String syncKingdeeId;


    /**
     * 来源主键id
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

    /**
     * 委外订单类型（child子级，parent父级）
     */
    @TableField("subcontract_type")
    private String subcontractType;
    /**
     * 供应商账户id
     */
    @TableField("supplier_account_id")
    private String supplierAccountId;

    /**
     * 合同盖章状态 ContractStampStatusEnum
     */
    @TableField("contract_stamp_status")
    private String contractStampStatus;

    @Override
    public Serializable pkVal() {
        return null;
    }

}
