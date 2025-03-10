package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;


/**
 * <p>
 * B2C订单账单明细信息
 * </p>
 *
 * @author Jim
 * @since 2025-03-10
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_so_bill_detail")
public class DmpSoBillDetailEntity extends BaseEntity<DmpSoBillDetailEntity> {

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
    * 来源平台
    */
    @TableField("source_platform")
    private String sourcePlatform;
    /**
    * 来源系统
    */
    @TableField("source_system")
    private String sourceSystem;
    /**
    * 来源平台单号
    */
    @TableField("third_code")
    private String thirdCode;
    /**
    * 来源系统单号
    */
    @TableField("platform_code")
    private String platformCode;
    /**
    * 来源系统明细ID
    */
    @TableField("third_detail_id")
    private String thirdDetailId;
    /**
    * 来源平台明细ID
    */
    @TableField("platform_detail_id")
    private String platformDetailId;
    /**
    * 来源ID
    */
    @TableField("source_id")
    private String sourceId;
    /**
    * ERP店铺ID
    */
    @TableField("shop_id")
    private String shopId;
    /**
    * 账单地址1
    */
    @TableField("address1")
    private String address1;
    /**
    * 账单地址2
    */
    @TableField("address2")
    private String address2;
    /**
    * 账单地址3
    */
    @TableField("address3")
    private String address3;
    /**
    * 账单城市
    */
    @TableField("city")
    private String city;
    /**
    * 账单国家
    */
    @TableField("country")
    private String country;
    /**
    * 账号邮编
    */
    @TableField("postal_code")
    private String postalCode;
    /**
    * 账号州
    */
    @TableField("state")
    private String state;
    /**
    * 买家邮箱
    */
    @TableField("buyer_email")
    private String buyerEmail;
    /**
    * 买家名称
    */
    @TableField("buyer_name")
    private String buyerName;
    /**
    * 买家电话
    */
    @TableField("buyer_phone_number")
    private String buyerPhoneNumber;
    /**
    * 账单币种
    */
    @TableField("currency")
    private String currency;
    /**
    * 包装费用
    */
    @TableField("gift_wrap_price")
    private BigDecimal giftWrapPrice;
    /**
    * 包装费用税
    */
    @TableField("gift_wrap_tax")
    private BigDecimal giftWrapTax;
    /**
    * 明细价格
    */
    @TableField("sell_price")
    private BigDecimal sellPrice;
    /**
    * 明细折扣价格
    */
    @TableField("discount_amount")
    private BigDecimal discountAmount;
    /**
    * 明细税费
    */
    @TableField("tax_amount")
    private BigDecimal taxAmount;
    /**
    * 支付时间
    */
    @TableField("pay_time")
    private LocalDateTime payTime;
    /**
    * 产品名称
    */
    @TableField("product_name")
    private String productName;
    /**
    * 平台创建时间
    */
    @TableField("platform_create_time")
    private LocalDateTime platformCreateTime;
    /**
    * 数量
    */
    @TableField("qty")
    private String qty;
    /**
    * 收件人
    */
    @TableField("recipient_name")
    private String recipientName;
    /**
    * 运费价格
    */
    @TableField("shipping_price")
    private BigDecimal shippingPrice;
    /**
    * 运费税
    */
    @TableField("shipping_tax")
    private BigDecimal shippingTax;
    /**
    * 平台sku
    */
    @TableField("platform_sku")
    private String platformSku;
    /**
    * 物流单号
    */
    @TableField("tracking_number")
    private String trackingNumber;


    public static final String INPUT_TASK_ID = "input_task_id";

    public static final String CONVERT_ID = "convert_id";

    public static final String NEXT_LEVEL_ID = "next_level_id";

    public static final String UNIQUE_ENCRYPT = "unique_encrypt";

    public static final String DATA_ENCRYPT = "data_encrypt";

    public static final String SOURCE_PLATFORM = "source_platform";

    public static final String SOURCE_SYSTEM = "source_system";

    public static final String THIRD_CODE = "third_code";

    public static final String PLATFORM_CODE = "platform_code";

    public static final String THIRD_DETAIL_ID = "third_detail_id";

    public static final String PLATFORM_DETAIL_ID = "platform_detail_id";

    public static final String SOURCE_ID = "source_id";

    public static final String SHOP_ID = "shop_id";

    public static final String ADDRESS1 = "address1";

    public static final String ADDRESS2 = "address2";

    public static final String ADDRESS3 = "address3";

    public static final String CITY = "city";

    public static final String COUNTRY = "country";

    public static final String POSTAL_CODE = "postal_code";

    public static final String STATE = "state";

    public static final String BUYER_EMAIL = "buyer_email";

    public static final String BUYER_NAME = "buyer_name";

    public static final String BUYER_PHONE_NUMBER = "buyer_phone_number";

    public static final String CURRENCY = "currency";

    public static final String GIFT_WRAP_PRICE = "gift_wrap_price";

    public static final String GIFT_WRAP_TAX = "gift_wrap_tax";

    public static final String SELL_PRICE = "sell_price";

    public static final String DISCOUNT_AMOUNT = "discount_amount";

    public static final String TAX_AMOUNT = "tax_amount";

    public static final String PAY_TIME = "pay_time";

    public static final String PRODUCT_NAME = "product_name";

    public static final String PLATFORM_CREATE_TIME = "platform_create_time";

    public static final String QTY = "qty";

    public static final String RECIPIENT_NAME = "recipient_name";

    public static final String SHIPPING_PRICE = "shipping_price";

    public static final String SHIPPING_TAX = "shipping_tax";

    public static final String PLATFORM_SKU = "platform_sku";

    public static final String TRACKING_NUMBER = "tracking_number";

    @Override
    public Serializable pkVal() {
        return null;
    }

}