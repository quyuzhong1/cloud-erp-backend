package com.erp.model.oms.entity;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.entity.BaseEntity;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;


/**
 * <p>
 * B2C销售订单表
 * </p>
 *
 * @author Will
 * @since 2023-08-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("so_b2c")
public class SoB2cEntity extends BaseEntity<SoB2cEntity> {

    /**
     * 单据编码
     */
    @TableField("code")
    private String code;

    /**
     * 订单日期
     */
    @TableField("bill_date")
    private LocalDate billDate;

    /**
     * 审核状态
     */
    @TableField("approve_status")
    private ApproveStatusEnum approveStatus;
    /**
     * 审核时间
     */
    @TableField("approve_time")
    private LocalDateTime approveTime;
    /**
     * 审核人id
     */
    @TableField(value = "approve_user_id")
    private String approveUserId;

    /**
     * 审核人名称
     */
    @TableField(value = "approve_user_name")
    private String approveUserName;
    /**
     * 平台订单号
     */
    @TableField("platform_code")
    private String platformCode;
    /**
     * 销售平台
     */
    @TableField("dict_platform")
    private String dictPlatform;
    /**
     * 店铺
     */
    @TableField("shop_id")
    private String shopId;
    /**
     * 作废状态（false未作废，true已作废）
     */
    @TableField("invalid_status")
    private Boolean invalidStatus;
    /**
     * 作废类型（manual手动作废，automatic自动作废）
     */
    @TableField("invalid_type")
    private String invalidType;
    /**
     * 作废原因
     */
    @TableField("invalid_remark")
    private String invalidRemark;
    /**
     * 订单状态，SoB2cBillStatusEnum枚举
     */
    @TableField("bill_status")
    private String billStatus;
    /**
     * 付款状态（待付款、已付款）
     */
    @TableField("pay_status")
    private String payStatus;
    /**
     * 订单金额
     */
    @TableField("amount")
    private BigDecimal amount;
    /**
     * 币别（原币）
     */
    @TableField("currency")
    private String currency;
    /**
     * 汇率
     */
    @TableField("exchange_rate")
    private BigDecimal exchangeRate;
    /**
     * 运费收入
     */
    @TableField("shipping_fee")
    private BigDecimal shippingFee;
    /**
     * 付款时间
     */
    @TableField("pay_time")
    private LocalDateTime payTime;
    /**
     * 付款金额
     */
    @TableField("pay_amount")
    private BigDecimal payAmount;
    /**
     * 付款方式
     */
    @TableField("dict_pay_method")
    private String dictPayMethod;
    /**
     * 买家备注
     */
    @TableField("buyer_remark")
    private String buyerRemark;
    /**
     * 订单备注
     */
    @TableField("remark")
    private String remark;
    /**
     * 销售组织id
     */
    @TableField("org_id")
    private String orgId;
    /**
     * 销售组织名称
     */
    @TableField("org_name")
    private String orgName;
    /**
     * 是否拦截
     */
    @TableField("is_intercept")
    private Boolean isIntercept;
    /**
     * 拦截备注
     */
    @TableField("intercept_remark")
    private String interceptRemark;
    /**
     * 来源类型
     */
    @TableField("source_type")
    private String sourceType;
    /**
     * 来源id
     */
    @TableField("source_id")
    private String sourceId;
    /**
     * 来源编码
     */
    @TableField("source_code")
    private String sourceCode;
    /**
     * 标签json
     */
    @TableField("label_json")
    private String labelJson;
    /**
     * 异常原因（1、订单规则审核不通过；2、配货规则匹配失败；3、人工审核不通过）
     */
    @TableField
    private String abnormalType;

    /**
     * 平台订单创建时间
     */
    @TableField("platform_order_create_time")
    private LocalDateTime platformOrderCreateTime;

    /**
     * 是否不需要合并
     */
    @TableField("is_not_merge")
    private Boolean isNotMerge;

    /**
     * 金蝶数据id
     */
    @TableField("sync_kingdee_id")
    private String syncKingdeeId;

    /**
     * 是否匹配订单规则
     */
    @TableField("is_match_order_rule")
    private Boolean isMatchOrderRule;

    /**
     * 是否匹配物流规则
     */
    @TableField("is_match_logistics_rule")
    private Boolean isMatchLogisticsRule;

    /**
     * 店铺名称
     */
    @TableField(exist = false)
    private String shopName;

    /**
     * 订单异常标示
     */
    @TableField("sign_order_error")
    private String signOrderError;

    /**
     * 第三方仓发货订单id
     */
    @TableField("shipping_order_no")
    private String shippingOrderNo;

    /**
     * 单据冻结状态
     */
    @TableField("is_frozen")
    private Boolean isFrozen;

    /**
     * 组包状态
     * not 不需要  wait 待组包   already 已经组包
     */
    @TableField("package_status")
    private String packageStatus;

    /**
     * 中转状态
     * not 不需要  wait 待中转   already 已经中转
     */
    @TableField("transfer_status")
    private String transferStatus;

    @TableField("extend_data")
    private String extendData;

    /**
     * 平台订单状态
     */
    @TableField("platform_order_status")
    private String platformOrderStatus;

    /**
     * 平台是否取消
     */
    @TableField("is_cancel")
    private Boolean isCancel;
    /**
     * 是否地址修改
     */
    @TableField("is_change_receiver_address")
    private Boolean isChangeReceiverAddress;

    /**
     * 卖家订单编号
     */
    @TableField("seller_order_code")
    private String sellerOrderCode;

    /**
     * 冻结类型（manual手动冻结，automatic自动冻结）
     */
    @TableField("frozen_type")
    private String frozenType;

    /**
     * 合并--所有子订单的平台订单编号合并，使用逗号隔开
     * 其余情况--为空
     */
    @TableField("merge_platform_code")
    private String mergePlatformCode;

    /**
     * 是否更换发货sku（默认false）
     */
    @TableField("is_change_sku")
    private Boolean isChangeSku;

    /**
     * 是否标记不出库发货（默认false）
     */
    @TableField("is_not_outbound")
    private Boolean isNotOutbound;

    public static final String CODE = "code";

    public static final String APPROVE_STATUS = "approve_status";

    public static final String PLATFORM_CODE = "platform_code";

    public static final String DICT_PLATFORM = "dict_platform";

    public static final String SHOP_ID = "shop_id";

    public static final String INVALID_STATUS = "invalid_status";

    public static final String INVALID_REMARK = "invalid_remark";

    public static final String BILL_STATUS = "bill_status";

    public static final String PAY_STATUS = "pay_status";

    public static final String AMOUNT = "amount";

    public static final String CURRENCY = "currency";

    public static final String EXCHANGE_RATE = "exchange_rate";

    public static final String SHIPPING_FEE = "shipping_fee";

    public static final String PAY_TIME = "pay_time";

    public static final String PAY_AMOUNT = "pay_amount";

    public static final String DICT_PAY_METHOD = "dict_pay_method";

    public static final String BUYER_REMARK = "buyer_remark";

    public static final String REMARK = "remark";

    public static final String ORG_ID = "org_id";

    public static final String ORG_NAME = "org_name";

    public static final String IS_INTERCEPT = "is_intercept";

    public static final String INTERCEPT_REMARK = "intercept_remark";

    public static final String SOURCE_TYPE = "source_type";

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_CODE = "source_code";

    public static final String LABEL_JSON = "label_json";

    public static final String ABNORMAL_TYPE = "abnormal_type";

    public static final String SYNC_KINGDEE_ID = "sync_kingdee_id";


    @Override
    public String toString() {
        return "SoB2cEntity{" +
                ", billDate=" + billDate +
                ", billStatus='" + billStatus + '\'' +
                ", payStatus='" + payStatus + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", exchangeRate=" + exchangeRate +
                ", shippingFee=" + shippingFee +
                ", payTime=" + payTime +
                ", payAmount=" + payAmount +
                ", dictPayMethod='" + dictPayMethod + '\'' +
                ", buyerRemark='" + buyerRemark + '\'' +
                '}';
    }

    /**
     * 是否是平台仓订单
     */
    public Boolean hasPlatformWarehouseOrder() {
        //亚马逊
        if (PlatformDictEnum.AMAZON.getCode().equalsIgnoreCase(this.dictPlatform)) {
            if (StrUtil.isNotBlank(this.labelJson)) {
                SoB2cDTO.LabelDTO labelJsonDTO = JSONUtil.toBean(this.labelJson, SoB2cDTO.LabelDTO.class);
                //FBA
                return "AFN".equalsIgnoreCase(labelJsonDTO.getFulfillmentChannel());
            }
        }
        // 速卖通
        if (PlatformDictEnum.ALI_EXPRESS.getCode().equalsIgnoreCase(this.dictPlatform)) {
            if (StrUtil.isNotBlank(this.labelJson)) {
                SoB2cDTO.LabelDTO labelJsonDTO = JSONUtil.toBean(this.labelJson, SoB2cDTO.LabelDTO.class);
                Boolean isAliexpressPlatformWarehouseOrder = labelJsonDTO.getIsPlatformWarehouseOrder();
                if (Objects.nonNull(isAliexpressPlatformWarehouseOrder)) {
                    return isAliexpressPlatformWarehouseOrder;
                }
                return false;
            }
        }
        // 虾皮
        if (PlatformDictEnum.SHOPEE.getCode().equalsIgnoreCase(this.dictPlatform)) {
            return false;
        }
        //沃尔玛
        if (PlatformDictEnum.WALMART.getCode().equalsIgnoreCase(this.dictPlatform)) {
            if (StrUtil.isNotBlank(this.labelJson)) {
                SoB2cDTO.LabelDTO labelJsonDTO = JSONUtil.toBean(this.labelJson, SoB2cDTO.LabelDTO.class);
                //FBA
                return "WFSFulfilled".equalsIgnoreCase(labelJsonDTO.getShipNodeType()) || "3PLFulfilled".equalsIgnoreCase(labelJsonDTO.getShipNodeType());
            }
        }
        //美客多
        if (PlatformDictEnum.MERCADOLIBRE.getCode().equalsIgnoreCase(this.dictPlatform)) {
            if (StrUtil.isNotBlank(this.labelJson)) {
                SoB2cDTO.LabelDTO labelJsonDTO = JSONUtil.toBean(this.labelJson, SoB2cDTO.LabelDTO.class);
                //平台仓发货
                return "fulfillment".equalsIgnoreCase(labelJsonDTO.getLogisticType());
            }
        }
        return false;
    }

    /**
     * 自发货订单是否已有发货单? true=有，false=无
     * 如果订单状态是(待发货/已发货/部分发货)=已有发货单
     */
    public boolean hasB2cSelfDelivery() {
        return Arrays.asList(
                SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode(),
                SoB2cBillStatusEnum.ENUM_SHIPPED.getCode(),
                SoB2cBillStatusEnum.ENUM_PARTIAL_SHIPPED.getCode()
        ).contains(this.billStatus) && !this.hasPlatformWarehouseOrder() ;
    }

    /**
     * 提交平台的唯一key:{平台代号}_{平台单号}_{店铺ID}
     */
    public String convertSubmitPlatformUniqueKey() {
        return StrUtil.format("{}_{}_{}", this.dictPlatform, this.shopId, this.platformCode);
    }
}