package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;

import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author LUO_WG
 * @since 2023-05-10
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("so_return_receive")
public class SoReturnReceiveEntity extends BaseEntity<SoReturnReceiveEntity> {

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
     * 单据类型
     */
    @TableField("type")
    private String type;

    /**
     * 销售组织id
     */
    @TableField("sales_org_id")
    private String salesOrgId;

    /**
     * 销售组织名称
     */
    @TableField("sales_org_name")
    private String salesOrgName;

    /**
     * 销售部门id
     */
    @TableField("dept_id")
    private String deptId;

    /**
     * 销售部门名称
     */
    @TableField("dept_name")
    private String deptName;

    /**
     * 销售员id
     */
    @TableField("seller_id")
    private String sellerId;

    /**
     * 销售员名称
     */
    @TableField("seller_name")
    private String sellerName;

    /**
     * 客户id
     */
    @TableField("customer_id")
    private String customerId;

    /**
     * 客户名称
     */
    @TableField("customer_name")
    private String customerName;

    /**
     * 退货日期
     */
    @TableField("return_date")
    private Date returnDate;

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
     * 退货日期
     */
    @TableField("bill_date")
    private Date billDate;

    /**
     * 作废状态
     */
    @TableField("invalid_status")
    private String invalidStatus;

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
    private Date approveTime;

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


    public static final String APPROVE_STATUS = "approve_status";

    public static final String CODE = "code";

    public static final String TYPE = "type";

    public static final String SALES_ORG_ID = "sales_org_id";

    public static final String SALES_ORG_NAME = "sales_org_name";

    public static final String DEPT_ID = "dept_id";

    public static final String DEPT_NAME = "dept_name";

    public static final String SELLER_ID = "seller_id";

    public static final String SELLER_NAME = "seller_name";

    public static final String CUSTOMER_ID = "customer_id";

    public static final String CUSTOMER_NAME = "customer_name";

    public static final String RETURN_DATE = "return_date";

    public static final String INVENTORY_ORG_ID = "inventory_org_id";

    public static final String INVENTORY_ORG_NAME = "inventory_org_name";

    public static final String WAREHOUSE_KEEPER_ID = "warehouse_keeper_id";

    public static final String WAREHOUSE_KEEPER_NAME = "warehouse_keeper_name";

    public static final String BILL_DATE = "bill_date";

    public static final String INVALID_STATUS = "invalid_status";

    public static final String INVALID_REMARK = "invalid_remark";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String APPROVE_TIME = "approve_time";

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_CODE = "source_code";

    public static final String SOURCE_TYPE = "source_type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
