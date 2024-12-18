package com.erp.model.dmp.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 亚马逊配送明细表
 * </p>
 *
 * @author Jim
 * @since 2024-12-03
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("dmp_amz_so_outstock_detail")
public class DmpAmzSoOutstockDetailEntity extends BaseEntity<DmpAmzSoOutstockDetailEntity> {

    /**
    * 平台创建时间
    */
    @TableField("platform_create_time")
    private LocalDateTime platformCreateTime;
    /**
    * 平台修改时间
    */
    @TableField("platform_update_time")
    private LocalDateTime platformUpdateTime;
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
    * 单号
    */
    @TableField("amazon_order_id")
    private String amazonOrderId;
    /**
    * 明细ID
    */
    @TableField("amazon_order_item_id")
    private String amazonOrderItemId;
    /**
    * 地址1
    */
    @TableField("bill_address1")
    private String billAddress1;
    /**
    * 地址2
    */
    @TableField("bill_address2")
    private String billAddress2;
    /**
    * 地址3
    */
    @TableField("bill_address3")
    private String billAddress3;
    /**
    * 城市
    */
    @TableField("bill_city")
    private String billCity;
    /**
    * 城镇
    */
    @TableField("bill_country")
    private String billCountry;
    /**
    * 邮编
    */
    @TableField("bill_postal_code")
    private String billPostalCode;
    /**
    * 州
    */
    @TableField("bill_state")
    private String billState;
    /**
    * 邮件
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
    * 承运商
    */
    @TableField("carrier")
    private String carrier;
    /**
    * 币种
    */
    @TableField("currency")
    private String currency;
    /**
    * 预估到达日期
    */
    @TableField("estimated_arrival_date")
    private String estimatedArrivalDate;
    /**
    * 仓库中心ID
    */
    @TableField("fulfillment_center_id")
    private String fulfillmentCenterId;
    /**
    * 渠道
    */
    @TableField("fulfillment_channel")
    private String fulfillmentChannel;
    /**
    * 优惠价格
    */
    @TableField("gift_wrap_price")
    private BigDecimal giftWrapPrice;
    /**
    * 优惠税号
    */
    @TableField("gift_wrap_tax")
    private BigDecimal giftWrapTax;
    /**
    * 明细价格
    */
    @TableField("item_price")
    private BigDecimal itemPrice;
    /**
    * 明细折扣
    */
    @TableField("item_promotion_discount")
    private BigDecimal itemPromotionDiscount;
    /**
    * 明细税号
    */
    @TableField("item_tax")
    private BigDecimal itemTax;
    /**
    * 商家ID
    */
    @TableField("merchant_order_id")
    private String merchantOrderId;
    /**
    * 商家明细ID
    */
    @TableField("merchant_order_item_id")
    private String merchantOrderItemId;
    /**
    * 支付日期
    */
    @TableField("payments_date")
    private String paymentsDate;
    /**
    * 账号编码
    */
    @TableField("platform_shop_code")
    private String platformShopCode;
    /**
    * 产品名称
    */
    @TableField("product_name")
    private String productName;
    /**
    * 订购日期
    */
    @TableField("purchase_date")
    private String purchaseDate;
    /**
    * 数量
    */
    @TableField("quantity_shipped")
    private String quantityShipped;
    /**
    * 名称
    */
    @TableField("recipient_name")
    private String recipientName;
    /**
    * 销售渠道
    */
    @TableField("sales_channel")
    private String salesChannel;
    /**
    * 配送地址1
    */
    @TableField("ship_address1")
    private String shipAddress1;
    /**
    * 配送地址2
    */
    @TableField("ship_address2")
    private String shipAddress2;
    /**
    * 配送地址3
    */
    @TableField("ship_address3")
    private String shipAddress3;
    /**
    * 配送城市
    */
    @TableField("ship_city")
    private String shipCity;
    /**
    * 配送镇
    */
    @TableField("ship_country")
    private String shipCountry;
    /**
    * 配送号码
    */
    @TableField("ship_phone_number")
    private String shipPhoneNumber;
    /**
    * 配送邮编
    */
    @TableField("ship_postal_code")
    private String shipPostalCode;
    /**
    * 配送优惠
    */
    @TableField("ship_promotion_discount")
    private BigDecimal shipPromotionDiscount;
    /**
    * 配送服务等级
    */
    @TableField("ship_service_level")
    private String shipServiceLevel;
    /**
    * 配送州
    */
    @TableField("ship_state")
    private String shipState;
    /**
    * 配送日期
    */
    @TableField("shipment_date")
    private String shipmentDate;
    /**
    * 配送ID
    */
    @TableField("shipment_id")
    private String shipmentId;
    /**
    * 配送明细ID
    */
    @TableField("shipment_item_id")
    private String shipmentItemId;
    /**
    * 配送价格
    */
    @TableField("shipping_price")
    private BigDecimal shippingPrice;
    /**
    * 配送税号
    */
    @TableField("shipping_tax")
    private BigDecimal shippingTax;
    /**
    * 平台sku
    */
    @TableField("sku")
    private String sku;
    /**
    * 物流单号
    */
    @TableField("tracking_number")
    private String trackingNumber;
    /**
     * 请求店铺ID
     */
    @TableField("request_shop_id")
    private String requestShopId;


    public static final String PLATFORM_CREATE_TIME = "platform_create_time";

    public static final String PLATFORM_UPDATE_TIME = "platform_update_time";

    public static final String INPUT_TASK_ID = "input_task_id";

    public static final String CONVERT_ID = "convert_id";

    public static final String NEXT_LEVEL_ID = "next_level_id";

    public static final String UNIQUE_ENCRYPT = "unique_encrypt";

    public static final String DATA_ENCRYPT = "data_encrypt";

    public static final String AMAZON_ORDER_ID = "amazon_order_id";

    public static final String AMAZON_ORDER_ITEM_ID = "amazon_order_item_id";

    public static final String BILL_ADDRESS1 = "bill_address1";

    public static final String BILL_ADDRESS2 = "bill_address2";

    public static final String BILL_ADDRESS3 = "bill_address3";

    public static final String BILL_CITY = "bill_city";

    public static final String BILL_COUNTRY = "bill_country";

    public static final String BILL_POSTAL_CODE = "bill_postal_code";

    public static final String BILL_STATE = "bill_state";

    public static final String BUYER_EMAIL = "buyer_email";

    public static final String BUYER_NAME = "buyer_name";

    public static final String BUYER_PHONE_NUMBER = "buyer_phone_number";

    public static final String CARRIER = "carrier";

    public static final String CURRENCY = "currency";

    public static final String ESTIMATED_ARRIVAL_DATE = "estimated_arrival_date";

    public static final String FULFILLMENT_CENTER_ID = "fulfillment_center_id";

    public static final String FULFILLMENT_CHANNEL = "fulfillment_channel";

    public static final String GIFT_WRAP_PRICE = "gift_wrap_price";

    public static final String GIFT_WRAP_TAX = "gift_wrap_tax";

    public static final String ITEM_PRICE = "item_price";

    public static final String ITEM_PROMOTION_DISCOUNT = "item_promotion_discount";

    public static final String ITEM_TAX = "item_tax";

    public static final String MERCHANT_ORDER_ID = "merchant_order_Id";

    public static final String MERCHANT_ORDER_ITEM_ID = "merchant_order_Item_Id";

    public static final String PAYMENTS_DATE = "payments_date";

    public static final String PLATFORM_SHOP_CODE = "platform_shop_code";

    public static final String PRODUCT_NAME = "product_name";

    public static final String PURCHASE_DATE = "purchase_date";

    public static final String QUANTITY_SHIPPED = "quantity_shipped";

    public static final String RECIPIENT_NAME = "recipient_name";

    public static final String SALES_CHANNEL = "sales_channel";

    public static final String SHIP_ADDRESS1 = "ship_address1";

    public static final String SHIP_ADDRESS2 = "ship_address2";

    public static final String SHIP_ADDRESS3 = "ship_address3";

    public static final String SHIP_CITY = "ship_city";

    public static final String SHIP_COUNTRY = "ship_country";

    public static final String SHIP_PHONE_NUMBER = "ship_phone_number";

    public static final String SHIP_POSTAL_CODE = "ship_postal_code";

    public static final String SHIP_PROMOTION_DISCOUNT = "ship_promotion_discount";

    public static final String SHIP_SERVICE_LEVEL = "ship_service_level";

    public static final String SHIP_STATE = "ship_state";

    public static final String SHIPMENT_DATE = "shipment_date";

    public static final String SHIPMENT_ID = "shipment_id";

    public static final String SHIPMENT_ITEM_ID = "shipment_item_id";

    public static final String SHIPPING_PRICE = "shipping_price";

    public static final String SHIPPING_TAX = "shipping_tax";

    public static final String SKU = "sku";

    public static final String TRACKING_NUMBER = "tracking_number";

    @Override
    public Serializable pkVal() {
        return null;
    }

}