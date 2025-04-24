package com.erp.model.dmp.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Digits;

/**
 * <p>
 * 亚马逊配送明细表请求响应实体
 * </p>
 *
 * @author Jim
 * @since 2024-12-03
*/
@Data
@NoArgsConstructor
public class DmpAmzSoOutstockDetailDTO implements Serializable {




    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * 平台创建时间
        */
        private LocalDateTime platformCreateTime;

        /**
        * 平台修改时间
        */
        private LocalDateTime platformUpdateTime;

        /**
        * 输入任务id
        */
        private String inputTaskId;

        /**
        * 转换id
        */
        private String convertId;

        /**
        * 下一层级id
        */
        private String nextLevelId;

        /**
        * 唯一字段md5值
        */
        private String uniqueEncrypt;

        /**
        * 数据字段md5值
        */
        private String dataEncrypt;

        /**
        * 单号
        */
        private String amazonOrderId;

        /**
        * 明细ID
        */
        private String amazonOrderItemId;

        /**
        * 地址1
        */
        private String billAddress1;

        /**
        * 地址2
        */
        private String billAddress2;

        /**
        * 地址3
        */
        private String billAddress3;

        /**
        * 城市
        */
        private String billCity;

        /**
        * 城镇
        */
        private String billCountry;

        /**
        * 邮编
        */
        private String billPostalCode;

        /**
        * 州
        */
        private String billState;

        /**
        * 邮件
        */
        private String buyerEmail;

        /**
        * 买家名称
        */
        private String buyerName;

        /**
        * 买家电话
        */
        private String buyerPhoneNumber;

        /**
        * 承运商
        */
        private String carrier;

        /**
        * 币种
        */
        private String currency;

        /**
        * 预估到达日期
        */
        private String estimatedArrivalDate;

        /**
        * 仓库中心ID
        */
        private String fulfillmentCenterId;

        /**
        * 渠道
        */
        private String fulfillmentChannel;

        /**
        * 优惠价格
        */
        private BigDecimal giftWrapPrice;

        /**
        * 优惠税号
        */
        private BigDecimal giftWrapTax;

        /**
        * 明细价格
        */
        private BigDecimal itemPrice;

        /**
        * 明细折扣
        */
        private BigDecimal itemPromotionDiscount;

        /**
        * 明细税号
        */
        private BigDecimal itemTax;

        /**
        * 商家ID
        */
        private String merchantOrderId;

        /**
        * 商家明细ID
        */
        private String merchantOrderItemId;

        /**
        * 支付日期
        */
        private String paymentsDate;

        /**
        * 账号编码
        */
        private String platformShopCode;

        /**
        * 产品名称
        */
        private String productName;

        /**
        * 订购日期
        */
        private String purchaseDate;

        /**
        * 数量
        */
        private String quantityShipped;

        /**
        * 名称
        */
        private String recipientName;

        /**
        * 销售渠道
        */
        private String salesChannel;

        /**
        * 配送地址1
        */
        private String shipAddress1;

        /**
        * 配送地址2
        */
        private String shipAddress2;

        /**
        * 配送地址3
        */
        private String shipAddress3;

        /**
        * 配送城市
        */
        private String shipCity;

        /**
        * 配送镇
        */
        private String shipCountry;

        /**
        * 配送号码
        */
        private String shipPhoneNumber;

        /**
        * 配送邮编
        */
        private String shipPostalCode;

        /**
        * 配送优惠
        */
        private BigDecimal shipPromotionDiscount;

        /**
        * 配送服务等级
        */
        private String shipServiceLevel;

        /**
        * 配送州
        */
        private String shipState;

        /**
        * 配送日期
        */
        private String shipmentDate;

        /**
        * 配送ID
        */
        private String shipmentId;

        /**
        * 配送明细ID
        */
        private String shipmentItemId;

        /**
        * 配送价格
        */
        private BigDecimal shippingPrice;

        /**
        * 配送税号
        */
        private BigDecimal shippingTax;

        /**
        * 平台sku
        */
        private String sku;

        /**
        * 物流单号
        */
        private String trackingNumber;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


    }

    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 平台创建时间
        */
        private LocalDateTime platformCreateTime;

        /**
        * 平台修改时间
        */
        private LocalDateTime platformUpdateTime;

        /**
        * 输入任务id
        */
        @NotBlank(message = "输入任务id不能为空")
        @Size(max = 19,message = "输入任务id最大长度不能超过19位")
        private String inputTaskId;

        /**
        * 转换id
        */
        @NotBlank(message = "转换id不能为空")
        @Size(max = 19,message = "转换id最大长度不能超过19位")
        private String convertId;

        /**
        * 下一层级id
        */
        @NotBlank(message = "下一层级id不能为空")
        @Size(max = 19,message = "下一层级id最大长度不能超过19位")
        private String nextLevelId;

        /**
        * 唯一字段md5值
        */
        private String uniqueEncrypt;

        /**
        * 数据字段md5值
        */
        private String dataEncrypt;

        /**
        * 单号
        */
        @NotBlank(message = "单号不能为空")
        @Size(max = 255,message = "单号最大长度不能超过255位")
        private String amazonOrderId;

        /**
        * 明细ID
        */
        @NotBlank(message = "明细ID不能为空")
        @Size(max = 255,message = "明细ID最大长度不能超过255位")
        private String amazonOrderItemId;

        /**
        * 地址1
        */
        @NotBlank(message = "地址1不能为空")
        @Size(max = 255,message = "地址1最大长度不能超过255位")
        private String billAddress1;

        /**
        * 地址2
        */
        @NotBlank(message = "地址2不能为空")
        @Size(max = 255,message = "地址2最大长度不能超过255位")
        private String billAddress2;

        /**
        * 地址3
        */
        @NotBlank(message = "地址3不能为空")
        @Size(max = 255,message = "地址3最大长度不能超过255位")
        private String billAddress3;

        /**
        * 城市
        */
        @NotBlank(message = "城市不能为空")
        @Size(max = 255,message = "城市最大长度不能超过255位")
        private String billCity;

        /**
        * 城镇
        */
        @NotBlank(message = "城镇不能为空")
        @Size(max = 255,message = "城镇最大长度不能超过255位")
        private String billCountry;

        /**
        * 邮编
        */
        @NotBlank(message = "邮编不能为空")
        @Size(max = 255,message = "邮编最大长度不能超过255位")
        private String billPostalCode;

        /**
        * 州
        */
        @NotBlank(message = "州不能为空")
        @Size(max = 255,message = "州最大长度不能超过255位")
        private String billState;

        /**
        * 邮件
        */
        @NotBlank(message = "邮件不能为空")
        @Size(max = 255,message = "邮件最大长度不能超过255位")
        private String buyerEmail;

        /**
        * 买家名称
        */
        @NotBlank(message = "买家名称不能为空")
        @Size(max = 255,message = "买家名称最大长度不能超过255位")
        private String buyerName;

        /**
        * 买家电话
        */
        @NotBlank(message = "买家电话不能为空")
        @Size(max = 255,message = "买家电话最大长度不能超过255位")
        private String buyerPhoneNumber;

        /**
        * 承运商
        */
        @NotBlank(message = "承运商不能为空")
        @Size(max = 255,message = "承运商最大长度不能超过255位")
        private String carrier;

        /**
        * 币种
        */
        @NotBlank(message = "币种不能为空")
        @Size(max = 255,message = "币种最大长度不能超过255位")
        private String currency;

        /**
        * 预估到达日期
        */
        @NotBlank(message = "预估到达日期不能为空")
        @Size(max = 255,message = "预估到达日期最大长度不能超过255位")
        private String estimatedArrivalDate;

        /**
        * 仓库中心ID
        */
        @NotBlank(message = "仓库中心ID不能为空")
        @Size(max = 255,message = "仓库中心ID最大长度不能超过255位")
        private String fulfillmentCenterId;

        /**
        * 渠道
        */
        @NotBlank(message = "渠道不能为空")
        @Size(max = 255,message = "渠道最大长度不能超过255位")
        private String fulfillmentChannel;

        /**
        * 优惠价格
        */
        @NotNull(message = "优惠价格不能为空")
        @Digits(integer = 12, fraction = 4, message = "优惠价格整数位不能超过12位，小数位不能超过4位")
        private BigDecimal giftWrapPrice;

        /**
        * 优惠税号
        */
        @NotNull(message = "优惠税号不能为空")
        @Digits(integer = 12, fraction = 4, message = "优惠税号整数位不能超过12位，小数位不能超过4位")
        private BigDecimal giftWrapTax;

        /**
        * 明细价格
        */
        @NotNull(message = "明细价格不能为空")
        @Digits(integer = 12, fraction = 4, message = "明细价格整数位不能超过12位，小数位不能超过4位")
        private BigDecimal itemPrice;

        /**
        * 明细折扣
        */
        @NotNull(message = "明细折扣不能为空")
        @Digits(integer = 12, fraction = 4, message = "明细折扣整数位不能超过12位，小数位不能超过4位")
        private BigDecimal itemPromotionDiscount;

        /**
        * 明细税号
        */
        @NotNull(message = "明细税号不能为空")
        @Digits(integer = 12, fraction = 4, message = "明细税号整数位不能超过12位，小数位不能超过4位")
        private BigDecimal itemTax;

        /**
        * 商家ID
        */
        @NotBlank(message = "商家ID不能为空")
        @Size(max = 255,message = "商家ID最大长度不能超过255位")
        private String merchantOrderId;

        /**
        * 商家明细ID
        */
        @NotBlank(message = "商家明细ID不能为空")
        @Size(max = 255,message = "商家明细ID最大长度不能超过255位")
        private String merchantOrderItemId;

        /**
        * 支付日期
        */
        @NotBlank(message = "支付日期不能为空")
        @Size(max = 255,message = "支付日期最大长度不能超过255位")
        private String paymentsDate;

        /**
        * 账号编码
        */
        @NotBlank(message = "账号编码不能为空")
        @Size(max = 255,message = "账号编码最大长度不能超过255位")
        private String platformShopCode;

        /**
        * 产品名称
        */
        @NotBlank(message = "产品名称不能为空")
        private String productName;

        /**
        * 订购日期
        */
        @NotBlank(message = "订购日期不能为空")
        @Size(max = 255,message = "订购日期最大长度不能超过255位")
        private String purchaseDate;

        /**
        * 数量
        */
        @NotBlank(message = "数量不能为空")
        @Size(max = 255,message = "数量最大长度不能超过255位")
        private String quantityShipped;

        /**
        * 名称
        */
        @NotBlank(message = "名称不能为空")
        @Size(max = 255,message = "名称最大长度不能超过255位")
        private String recipientName;

        /**
        * 销售渠道
        */
        @NotBlank(message = "销售渠道不能为空")
        @Size(max = 255,message = "销售渠道最大长度不能超过255位")
        private String salesChannel;

        /**
        * 配送地址1
        */
        @NotBlank(message = "配送地址1不能为空")
        @Size(max = 255,message = "配送地址1最大长度不能超过255位")
        private String shipAddress1;

        /**
        * 配送地址2
        */
        @NotBlank(message = "配送地址2不能为空")
        @Size(max = 255,message = "配送地址2最大长度不能超过255位")
        private String shipAddress2;

        /**
        * 配送地址3
        */
        @NotBlank(message = "配送地址3不能为空")
        @Size(max = 255,message = "配送地址3最大长度不能超过255位")
        private String shipAddress3;

        /**
        * 配送城市
        */
        @NotBlank(message = "配送城市不能为空")
        @Size(max = 255,message = "配送城市最大长度不能超过255位")
        private String shipCity;

        /**
        * 配送镇
        */
        @NotBlank(message = "配送镇不能为空")
        @Size(max = 255,message = "配送镇最大长度不能超过255位")
        private String shipCountry;

        /**
        * 配送号码
        */
        @NotBlank(message = "配送号码不能为空")
        @Size(max = 255,message = "配送号码最大长度不能超过255位")
        private String shipPhoneNumber;

        /**
        * 配送邮编
        */
        @NotBlank(message = "配送邮编不能为空")
        @Size(max = 255,message = "配送邮编最大长度不能超过255位")
        private String shipPostalCode;

        /**
        * 配送优惠
        */
        @NotNull(message = "配送优惠不能为空")
        @Digits(integer = 12, fraction = 4, message = "配送优惠整数位不能超过12位，小数位不能超过4位")
        private BigDecimal shipPromotionDiscount;

        /**
        * 配送服务等级
        */
        @NotBlank(message = "配送服务等级不能为空")
        @Size(max = 255,message = "配送服务等级最大长度不能超过255位")
        private String shipServiceLevel;

        /**
        * 配送州
        */
        @NotBlank(message = "配送州不能为空")
        @Size(max = 255,message = "配送州最大长度不能超过255位")
        private String shipState;

        /**
        * 配送日期
        */
        @NotBlank(message = "配送日期不能为空")
        @Size(max = 255,message = "配送日期最大长度不能超过255位")
        private String shipmentDate;

        /**
        * 配送ID
        */
        @NotBlank(message = "配送ID不能为空")
        @Size(max = 255,message = "配送ID最大长度不能超过255位")
        private String shipmentId;

        /**
        * 配送明细ID
        */
        @NotBlank(message = "配送明细ID不能为空")
        @Size(max = 255,message = "配送明细ID最大长度不能超过255位")
        private String shipmentItemId;

        /**
        * 配送价格
        */
        @NotNull(message = "配送价格不能为空")
        @Digits(integer = 12, fraction = 4, message = "配送价格整数位不能超过12位，小数位不能超过4位")
        private BigDecimal shippingPrice;

        /**
        * 配送税号
        */
        @NotNull(message = "配送税号不能为空")
        @Digits(integer = 12, fraction = 4, message = "配送税号整数位不能超过12位，小数位不能超过4位")
        private BigDecimal shippingTax;

        /**
        * 平台sku
        */
        @NotBlank(message = "平台sku不能为空")
        @Size(max = 255,message = "平台sku最大长度不能超过255位")
        private String sku;

        /**
        * 物流单号
        */
        @NotBlank(message = "物流单号不能为空")
        @Size(max = 255,message = "物流单号最大长度不能超过255位")
        private String trackingNumber;


    }


}