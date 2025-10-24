package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 
 * </p>
 *
 * @author wtr
 * @since 2025-10-16
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("asset_purchase_order")
public class AssetPurchaseOrderEntity extends BaseEntity<AssetPurchaseOrderEntity> {

    /**
    * 资产采购单号
    */
    @TableField("code")
    private String code;
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
    @TableField("approve_user_time")
    private LocalDateTime approveUserTime;
    /**
    * 单据状态
    */
    @TableField("approve_status")
    private String approveStatus;
    /**
    * 合同盖章状态：waitSubmit=待申请,approveIng=已申请,approve=已完成,reject=未完成
    */
    @TableField("contract_stamp_status")
    private String contractStampStatus;
    /**
    * 单据类型
    */
    @TableField("order_type")
    private String orderType;
    /**
    * 来源订单id
    */
    @TableField("source_id")
    private String sourceId;
    /**
    * 来源订单号
    */
    @TableField("source_code")
    private String sourceCode;
    /**
    * 来源订单类型
    */
    @TableField("source_type")
    private String sourceType;
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
    * 作废时间
    */
    @TableField("invalid_time")
    private LocalDateTime invalidTime;
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
     * 同步金蝶id
     */
    @TableField("sync_kingdee_id")
    private String syncKingdeeId;


    public static final String CODE = "code";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String APPROVE_USER_TIME = "approve_user_time";

    public static final String APPROVE_STATUS = "approve_status";

    public static final String CONTRACT_STAMP_STATUS = "contract_stamp_status";

    public static final String ORDER_TYPE = "order_type";

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_CODE = "source_code";

    public static final String SOURCE_TYPE = "source_type";

    public static final String PURCHASE_DATE = "purchase_date";

    public static final String PURCHASE_USER_ID = "purchase_user_id";

    public static final String PURCHASE_USER_NAME = "purchase_user_name";

    public static final String PURCHASE_DEPT_ID = "purchase_dept_id";

    public static final String PURCHASE_DEPT_NAME = "purchase_dept_name";

    public static final String PURCHASE_ORG_ID = "purchase_org_id";

    public static final String PURCHASE_ORG_NAME = "purchase_org_name";

    public static final String INVALID_STATUS = "invalid_status";

    public static final String INVALID_TIME = "invalid_time";

    public static final String INVALID_REMARK = "invalid_remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}