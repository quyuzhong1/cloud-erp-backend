package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
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


    @TableField("approve_user_name")
    private String approveUserName;

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
     * 同步金蝶状态（默认0无需同步,1待同步,2同步中,3同步成功,4同步失败）
     */
    @TableField("sync_kingdee_status")
    private String syncKingdeeStatus;

    /**
     * 同步金蝶时间
     */
    @TableField("sync_kingdee_time")
    private LocalDateTime syncKingdeeTime;

    /**
     * 同步金蝶id
     */
    @TableField("sync_kingdee_id")
    private String syncKingdeeId;

    /**
     * 同步操作
     */
    @TableField("sync_operate")
    private String syncOperate;





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

    @Override
    public Serializable pkVal() {
        return null;
    }

}
