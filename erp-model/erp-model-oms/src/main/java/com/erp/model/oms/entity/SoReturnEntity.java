package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author Luo_WG
 * @since 2023-05-10
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("so_return")
public class SoReturnEntity extends BaseEntity<SoReturnEntity> {

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
    @TableField("sales_dept_id")
    private String salesDeptId;

    /**
     * 销售部门名称
     */
    @TableField("sales_dept_name")
    private String salesDeptName;

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
     * 退货日期
     */
    @TableField("bill_date")
    private LocalDate billDate;

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
     * 收货人
     */
    @TableField("receiver_name")
    private String receiverName;

    /**
     * 联系电话
     */
    @TableField("tel_number")
    private String telNumber;

    /**
     * 收货地址
     */
    @TableField("receive_address")
    private String receiveAddress;

    /**
     * 交货方式  deliverGoods（发货）selfExtraction（自提）
     */
    @TableField("delivery_mode_dict")
    private String deliveryModeDict;

    /**
     * 币别
     */
    @TableField("currency")
    private String currency;

    /**
     * 币种符号
     */
    @TableField("currency_symbol")
    private String currencySymbol;

    /**
     * 是否含税
     */
    @TableField("is_tax")
    private Boolean isTax;

    /**
     * 地址类型：forwarder 货代地址  deliver 发货地址 company 公司地址
     */
    @TableField("address_type_dict")
    private String addressTypeDict;

    /**
     * 作废状态
     */
    @TableField("invalid_status")
    private Boolean invalidStatus;

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
     * 库存组织
     */
    @TableField("inventory_org_id")
    private String inventoryOrgId;

    /**
     * 库存组织名称
     */
    @TableField("inventory_org_name")
    private String inventoryOrgName;

    /**
     * 同步金蝶id
     */
    @TableField("sync_kingdee_id")
    private String syncKingdeeId;

    /**
     * 汇率
     */
    @TableField("exchange_rate")
    private BigDecimal exchangeRate;

    

    public static final String APPROVE_STATUS = "approve_status";

    public static final String CODE = "code";

    public static final String TYPE = "type";

    public static final String SALES_ORG_ID = "sales_org_id";

    public static final String SALES_ORG_NAME = "sales_org_name";

    public static final String DEPT_ID = "dept_id";

    public static final String DEPT_NAME = "dept_name";

    public static final String SELLER_ID = "seller_id";

    public static final String SELLER_NAME = "seller_name";

    public static final String BILL_DATE = "bill_date";

    public static final String CUSTOMER_ID = "customer_id";

    public static final String CUSTOMER_NAME = "customer_name";

    public static final String RECEIVER_NAME = "receiver_name";

    public static final String TEL_NUMBER = "tel_number";

    public static final String RECEIVE_ADDRESS = "receive_address";

    public static final String DELIVERY_MODE_DICT = "delivery_mode_dict";

    public static final String CURRENCY = "currency";

    public static final String CURRENCY_SYMBOL = "currency_symbol";

    public static final String IS_TAX = "is_tax";

    public static final String ADDRESS_TYPE_DICT = "address_type_dict";

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
