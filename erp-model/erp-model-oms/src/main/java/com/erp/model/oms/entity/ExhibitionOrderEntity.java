package com.erp.model.oms.entity;

import java.math.BigDecimal;
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
 * 展会订单信息
 * </p>
 *
 * @author jack
 * @since 2025-08-29
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("exhibition_order")
public class ExhibitionOrderEntity extends BaseEntity<ExhibitionOrderEntity> {

    /**
    * 作废状态
    */
    @TableField("invalid_status")
    private Boolean invalidStatus;
    /**
    * code
    */
    @TableField("code")
    private String code;
    /**
    * 展会主题
    */
    @TableField("exhibition_title")
    private String exhibitionTitle;
    /**
    * 审核状态
    */
    @TableField("approve_status")
    private ApproveStatusEnum approveStatus;
    /**
    * 销售组织id
    */
    @TableField("sales_org_id")
    private String salesOrgId;
    /**
    * 销售部门id
    */
    @TableField("sales_dept_id")
    private String salesDeptId;
    /**
    * 销售员id
    */
    @TableField("seller_id")
    private String sellerId;
    /**
    * 销售员
    */
    @TableField("seller_name")
    private String sellerName;
    /**
    * 领用人id
    */
    @TableField("recipient_user_id")
    private String recipientUserId;
    /**
    * 领用人
    */
    @TableField("recipient_user_name")
    private String recipientUserName;
    /**
    * 是否收取运费
    */
    @TableField("is_collect_shipping_fee")
    private Boolean isCollectShippingFee;
    /**
    * 仓库id
    */
    @TableField("warehouse_id")
    private String warehouseId;
    /**
    * 仓库组织id
    */
    @TableField("warehouse_org_id")
    private String warehouseOrgId;
    /**
    * 银行手续费
    */
    @TableField("bank_service_fee")
    private BigDecimal bankServiceFee;
    /**
    * 运费
    */
    @TableField("shipping_fee")
    private BigDecimal shippingFee;
    /**
    * 客户id
    */
    @TableField("customer_id")
    private String customerId;
    /**
    * 收货人
    */
    @TableField("receiver_name")
    private String receiverName;
    /**
    * 联系人电话
    */
    @TableField("tel_number")
    private String telNumber;
    /**
    * 收货地址
    */
    @TableField("receive_address")
    private String receiveAddress;
    /**
    * 交货方式
    */
    @TableField("delivery_mode")
    private String deliveryMode;
    /**
    * 币种
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
    * 地址类型
    */
    @TableField("address_type")
    private String addressType;
    /**
    * 销售组织名
    */
    @TableField("sales_org_name")
    private String salesOrgName;
    /**
    * 仓库组织名称
    */
    @TableField("warehouse_org_name")
    private String warehouseOrgName;
    /**
    * 收货地址id
    */
    @TableField("receive_address_id")
    private String receiveAddressId;
    /**
    * 审核人
    */
    @TableField("approve_user_name")
    private String approveUserName;
    /**
    * 收款条件
    */
    @TableField("receive_condition")
    private String receiveCondition;
    /**
    * 收款账号
    */
    @TableField("receive_account")
    private String receiveAccount;
    /**
    * 收款金额
    */
    @TableField("receive_amount")
    private BigDecimal receiveAmount;
    /**
    * 收款日期
    */
    @TableField("receive_date")
    private LocalDate receiveDate;
    /**
    * 收款方式
    */
    @TableField("receive_method")
    private String receiveMethod;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 单据日期
    */
    @TableField("bill_date")
    private LocalDate billDate;
    /**
    * 贸易条款
    */
    @TableField("trade_term")
    private String tradeTerm;
    /**
    * 审核时间
    */
    @TableField("approve_time")
    private LocalDateTime approveTime;
    /**
    * 折扣总额
    */
    @TableField("discount_amount")
    private BigDecimal discountAmount;
    /**
    * 总价税合计本位币
    */
    @TableField("all_amount_lc")
    private BigDecimal allAmountLc;
    /**
    * 收货国家id
    */
    @TableField("country_id")
    private String countryId;
    /**
    * 收货国家
    */
    @TableField("country_name")
    private String countryName;
    /**
    * 分区id
    */
    @TableField("partition_id")
    private String partitionId;

    /**
     * 作废原因
     */
    @TableField("invalid_remark")
    private String invalidRemark;

    public static final String INVALID_STATUS = "invalid_status";

    public static final String CODE = "code";

    public static final String EXHIBITION_TITLE = "exhibition_title";

    public static final String APPROVE_STATUS = "approve_status";

    public static final String SALES_ORG_ID = "sales_org_id";

    public static final String SALES_DEPT_ID = "sales_dept_id";

    public static final String SELLER_ID = "seller_id";

    public static final String SELLER_NAME = "seller_name";

    public static final String RECIPIENT_USER_ID = "recipient_user_id";

    public static final String RECIPIENT_USER_NAME = "recipient_user_name";

    public static final String IS_COLLECT_SHIPPING_FEE = "is_collect_shipping_fee";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String WAREHOUSE_ORG_ID = "warehouse_org_id";

    public static final String BANK_SERVICE_FEE = "bank_service_fee";

    public static final String SHIPPING_FEE = "shipping_fee";

    public static final String CUSTOMER_ID = "customer_id";

    public static final String RECEIVER_NAME = "receiver_name";

    public static final String TEL_NUMBER = "tel_number";

    public static final String RECEIVE_ADDRESS = "receive_address";

    public static final String DELIVERY_MODE = "delivery_mode";

    public static final String CURRENCY = "currency";

    public static final String CURRENCY_SYMBOL = "currency_symbol";

    public static final String IS_TAX = "is_tax";

    public static final String ADDRESS_TYPE = "address_type";

    public static final String SALES_ORG_NAME = "sales_org_name";

    public static final String WAREHOUSE_ORG_NAME = "warehouse_org_name";

    public static final String RECEIVE_ADDRESS_ID = "receive_address_id";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String RECEIVE_CONDITION = "receive_condition";

    public static final String RECEIVE_ACCOUNT = "receive_account";

    public static final String RECEIVE_AMOUNT = "receive_amount";

    public static final String RECEIVE_DATE = "receive_date";

    public static final String RECEIVE_METHOD = "receive_method";

    public static final String REMARK = "remark";

    public static final String BILL_DATE = "bill_date";

    public static final String TRADE_TERM = "trade_term";

    public static final String APPROVE_TIME = "approve_time";

    public static final String DISCOUNT_AMOUNT = "discount_amount";

    public static final String ALL_AMOUNT_LC = "all_amount_lc";

    public static final String COUNTRY_ID = "country_id";

    public static final String COUNTRY_NAME = "country_name";

    public static final String PARTITION_ID = "partition_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}