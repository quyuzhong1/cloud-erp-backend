package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.enums.BillApproveStatusEnum;
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
 * 销售订单信息
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("so_info")
public class SoInfoEntity extends BaseEntity<SoInfoEntity> {

    /**
     * code
     */
    @TableField("code")
    private String code;

    /**
     * 审核状态
     */
    @TableField("approve_status")
    private BillApproveStatusEnum approveStatus;

    /**
     * 最后审核人名称
     */
    @TableField("approve_user_name")
    private String approveUserName;

    /**
     * 最后审核时间
     */
    @TableField(value = "approve_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime approveTime;

    /**
     * 订单类型
     */
    @TableField("order_type")
    private String orderType;

    /**
     * 要货日期
     */
    @TableField("require_date")
    private LocalDate requireDate;


    /**
     * 单据日期
     */
    @TableField("bill_date")
    private LocalDate billDate;

    /**
     * 销售组织id
     */
    @TableField("sales_org_id")
    private String salesOrgId;

    /**
     * 销售组织id
     */
    @TableField("sales_org_name")
    private String salesOrgName;

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
     * 是否收取运费 true 收取
     */
    @TableField("is_collect_shipping_fee")
    private Boolean isCollectShippingFee;

    /**
     * 虚拟仓库Id
     */
    @TableField("virtual_warehouse_id")
    private String virtualWarehouseId;

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
     * 作废状态
     * true 作废
     * false 未作废
     */
    @TableField("invalid_status")
    private Boolean invalidStatus;






    /**
     * 仓库组织名
     */
    @TableField("warehouse_org_name")
    private String warehouseOrgName;

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
     * 收货人id地址
     * 来源 http://172.16.100.11:3002/project/110/interface/api/13561
     * <p>
     * 这个是地址下拉 http://172.16.100.11:3002/project/110/interface/api/13786
     */
    @TableField("receive_address_id")
    private String receiveAddressId;



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
     * 是否含税 true
     */
    @TableField("is_tax")
    private Boolean isTax;

    /**
     * 地址类型
     */
    @TableField("address_type")
    private String addressType;

    /**
     * 同步金蝶id
     */
    @TableField("sync_kingdee_id")
    private String syncKingdeeId;

    /**
     * 收款账号
     */
    @TableField("receive_account")
    private String receiveAccount;

    /**
     * 收款方式
     */
    @TableField("receive_method")
    private String receiveMethod;

    /**
     * 收款日期
     */
    @TableField("receive_date")
    private LocalDate receiveDate;


    /**
     * 收款金额
     */
    @TableField("receive_amount")
    private BigDecimal receiveAmount;

    /**
     * 收款条件
     */
    @TableField("receive_condition")
    private String receiveCondition;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 贸易条款
     */
    @TableField("trade_term")
    private String tradeTerm;

    /**
     * 报关费
     */
    @TableField(value = "customs_fee")
    private BigDecimal customsFee;

    /**
     * 折扣总额
     */
    @TableField(value = "discount_amount")
    private BigDecimal discountAmount;

    /**
     * 总价税合计本位币
     */
    @TableField(value = "all_amount_lc")
    private BigDecimal allAmountLc;

    /**
     * 客户订单号
     */
    @TableField("customer_order_no")
    private String customerOrderNo;

    /**
     * 是否报关
     */
    @TableField("is_declare")
    private Boolean isDeclare;

    /**
     * 是否上传物流面单(默认false)
     */
    @TableField("is_upload_label")
    private Boolean isUploadLabel;

    /**
     * 订单交易子状态
     * 枚举：OrderSubTypeEnum
     */
    @TableField("transaction_sub_type")
    private String transactionSubType = "offlineOrder";

    /**
     * 分区id
     */
    @TableField("partition_id")
    private String partitionId;

    @TableField(exist = false)
    private String customerCountry;

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
     * 订单金额
     */
    @TableField("order_amount")
    private BigDecimal orderAmount;

    /**
     * 平台订单Id
     */
    @TableField("platform_order_id")
    private String platformOrderId;
    /**
     * 平台订单编号
     */
    @TableField("platform_order_code")
    private String platformOrderCode;

    /**
     * 平台更新时间
     */
    @TableField("platform_update_time")
    private LocalDateTime platformUpdateTime;

    /**
     * 平台创建时间
     */
    @TableField("platform_create_time")
    private LocalDateTime platformCreateTime;

    /**
     * 账户抵扣金额
     */
    @TableField("account_deduct_amount")
    private BigDecimal accountDeductAmount;
    /**
     * 返利抵扣金额
     */
    @TableField("rebate_deduct_amount")
    private BigDecimal rebateDeductAmount;
    /**
     * 授信抵扣金额
     */
    @TableField("credit_deduct_amount")
    private BigDecimal creditDeductAmount;

    /**
     * 平台
     */
    @TableField("dict_platform")
    private String dictPlatform;

    public static final String CODE = "code";

    public static final String APPROVE_STATUS = "approve_status";

    public static final String TYPE = "type";

    public static final String REQUIRE_DATE = "require_date";

    public static final String SALES_ORG_ID = "sales_org_id";

    public static final String SALES_DEPT_ID = "sales_dept_id";

    public static final String SELLER_ID = "seller_id";

    public static final String SELLER_NAME = "seller_name";

    public static final String IS_COLLECT_SHIPPING_FEE = "is_collect_shipping_fee";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String WAREHOUSE_ORG_ID = "warehouse_org_id";

    public static final String  BANK_SERVICE_FEE = " bank_service_fee";

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

    public static final String RECEIVE_CONDITION = "receive_condition";

    public static final String RECEIVE_ACCOUNT = "receive_account";

    public static final String RECEIVE_AMOUNT = "receive_amount";

    public static final String RECEIVE_DATE = "receive_date";

    public static final String RECEIVE_METHOD = "receive_method";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

    public void setCustomsFee(BigDecimal customsFee) {
      this.customsFee =  ObjectUtils.isEmpty(customsFee) ? BigDecimal.ZERO : customsFee;
    }
}
