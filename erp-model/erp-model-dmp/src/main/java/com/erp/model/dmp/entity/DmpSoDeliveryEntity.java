package com.erp.model.dmp.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 中台配货单主表
 * </p>
 *
 * @author shukai
 * @since 2025-04-17
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_so_delivery")
public class DmpSoDeliveryEntity extends BaseEntity<DmpSoDeliveryEntity> {

    /**
    * 订单来源平台（编码）：Amazon，AliExpress，shopify，...
    */
    @TableField("source_platform")
    private String sourcePlatform;
    /**
    * 来源平台：gyy，kingdee，mabang
    */
    @TableField("source_system")
    private String sourceSystem;
    /**
    * 第三方配货id
    */
    @TableField("third_delivery_id")
    private String thirdDeliveryId;
    /**
    * 第三方配货单据编号
    */
    @TableField("third_delivery_code")
    private String thirdDeliveryCode;
    /**
    * 第三方创建时间
    */
    @TableField("third_create_time")
    private LocalDateTime thirdCreateTime;
    /**
    * 第三方更新时间
    */
    @TableField("third_update_time")
    private LocalDateTime thirdUpdateTime;
    /**
    * 支付时间
    */
    @TableField("pay_time")
    private LocalDateTime payTime;
    /**
    * 发货状态
    */
    @TableField("delivery_status")
    private String deliveryStatus;
    /**
    * 交易类型
    */
    @TableField("transaction_type")
    private String transactionType;
    /**
    * 交易子类型
    */
    @TableField("transaction_sub_type")
    private String transactionSubType;
    /**
    * 商品总成交金额（合计）
    */
    @TableField("all_amount")
    private BigDecimal allAmount;
    /**
    * 优惠抵扣金额|佣金（合计）
    */
    @TableField("total_discount_amount")
    private BigDecimal totalDiscountAmount;
    /**
    * 取消商品总金额（合计）
    */
    @TableField("total_cancel_amount")
    private BigDecimal totalCancelAmount;
    /**
    * 支付金额
    */
    @TableField("pay_amount")
    private BigDecimal payAmount;
    /**
    * 运费收入
    */
    @TableField("shipping_amount")
    private BigDecimal shippingAmount;
    /**
    * 税金
    */
    @TableField("total_tax_amount")
    private BigDecimal totalTaxAmount;
    /**
    * 商品总数量 （合计）
    */
    @TableField("total_qty")
    private Integer totalQty;
    /**
    * 取消商品数量 （合计）
    */
    @TableField("cancel_qty")
    private Integer cancelQty;
    /**
    * 订单应发数量（合计）
    */
    @TableField("shipping_qty")
    private Integer shippingQty;
    /**
    * 销售组织编码
    */
    @TableField("sales_company_code")
    private String salesCompanyCode;
    /**
    * 收款组织编码
    */
    @TableField("receiving_company_code")
    private String receivingCompanyCode;
    /**
    * 组织名称
    */
    @TableField("organization_name")
    private String organizationName;
    /**
    * 组织编码
    */
    @TableField("organization_code")
    private String organizationCode;
    /**
    * 平台名称
    */
    @TableField("platform_name")
    private String platformName;
    /**
    * 子平台编码
    */
    @TableField("subplatform_no")
    private String subplatformNo;
    /**
    * 子平台名称
    */
    @TableField("subplatform_name")
    private String subplatformName;
    /**
    * 店铺名称
    */
    @TableField("shop_name")
    private String shopName;
    /**
    * 店铺编码
    */
    @TableField("shop_no")
    private String shopNo;
    /**
    * 平台销售订单号
    */
    @TableField("platform_code")
    private String platformCode;
    /**
    * 数据来源
    */
    @TableField("data_source")
    private String dataSource;
    /**
     * 来源类型
     */
    @TableField("source_type")
    private String sourceType;
    /**
    * 唯一字段md5值
    */
    @TableField("unique_encrypt")
    private String uniqueEncrypt;
    /**
    * 数据字段md5值
    */
    @TableField("data_encrypt")
    private String dataEncrypt;
    /**
    * 输入任务id
    */
    @TableField("input_task_id")
    private String inputTaskId;
    /**
    * 转换id
    */
    @TableField("convert_id")
    private String convertId;
    /**
    * 下一层级id
    */
    @TableField("next_level_id")
    private String nextLevelId;


    public static final String SOURCE_PLATFORM = "source_platform";

    public static final String SOURCE_SYSTEM = "source_system";

    public static final String THIRD_DELIVERY_ID = "third_delivery_id";

    public static final String THIRD_DELIVERY_CODE = "third_delivery_code";

    public static final String THIRD_CREATE_TIME = "third_create_time";

    public static final String THIRD_UPDATE_TIME = "third_update_time";

    public static final String PAY_TIME = "pay_time";

    public static final String DELIVERY_STATUS = "delivery_status";

    public static final String TRANSACTION_TYPE = "transaction_type";

    public static final String TRANSACTION_SUB_TYPE = "transaction_sub_type";

    public static final String ALL_AMOUNT = "all_amount";

    public static final String TOTAL_DISCOUNT_AMOUNT = "total_discount_amount";

    public static final String TOTAL_CANCEL_AMOUNT = "total_cancel_amount";

    public static final String PAY_AMOUNT = "pay_amount";

    public static final String SHIPPING_AMOUNT = "shipping_amount";

    public static final String TOTAL_TAX_AMOUNT = "total_tax_amount";

    public static final String TOTAL_QTY = "total_qty";

    public static final String CANCEL_QTY = "cancel_qty";

    public static final String SHIPPING_QTY = "shipping_qty";

    public static final String SALES_COMPANY_CODE = "sales_company_code";

    public static final String RECEIVING_COMPANY_CODE = "receiving_company_code";

    public static final String ORGANIZATION_NAME = "organization_name";

    public static final String ORGANIZATION_CODE = "organization_code";

    public static final String PLATFORM_NAME = "platform_name";

    public static final String SUBPLATFORM_NO = "subplatform_no";

    public static final String SUBPLATFORM_NAME = "subplatform_name";

    public static final String SHOP_NAME = "shop_name";

    public static final String SHOP_NO = "shop_no";

    public static final String PLATFORM_CODE = "platform_code";

    public static final String DATA_SOURCE = "data_source";

    public static final String REMARK = "remark";

    public static final String UNIQUE_ENCRYPT = "unique_encrypt";

    public static final String DATA_ENCRYPT = "data_encrypt";

    public static final String INPUT_TASK_ID = "input_task_id";

    public static final String CONVERT_ID = "convert_id";

    public static final String NEXT_LEVEL_ID = "next_level_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}