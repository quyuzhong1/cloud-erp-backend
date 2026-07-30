package com.erp.model.dmp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * B2C订单账单明细信息请求响应实体
 * </p>
 *
 * @author Jim
 * @since 2025-03-10
*/
@Data
@NoArgsConstructor
public class DmpSoBillDetailDTO implements Serializable {




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
        * 来源平台
        */
        private String sourcePlatform;

        /**
        * 来源系统
        */
        private String sourceSystem;

        /**
        * 来源平台单号
        */
        private String thirdCode;

        /**
        * 来源系统单号
        */
        private String platformCode;

        /**
        * 来源系统明细ID
        */
        private String thirdDetailId;

        /**
        * 来源平台明细ID
        */
        private String platformDetailId;

        /**
        * 来源ID
        */
        private String sourceId;

        /**
        * ERP店铺ID
        */
        private String shopId;

        /**
        * 账单地址1
        */
        private String address1;

        /**
        * 账单地址2
        */
        private String address2;

        /**
        * 账单地址3
        */
        private String address3;

        /**
        * 账单城市
        */
        private String city;

        /**
        * 账单国家
        */
        private String country;

        /**
        * 账号邮编
        */
        private String postalCode;

        /**
        * 账号州
        */
        private String state;

        /**
        * 买家邮箱
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
        * 账单币种
        */
        private String currency;

        /**
        * 包装费用
        */
        private BigDecimal giftWrapPrice;

        /**
        * 包装费用税
        */
        private BigDecimal giftWrapTax;

        /**
        * 明细价格
        */
        private BigDecimal sellPrice;

        /**
        * 明细折扣价格
        */
        private BigDecimal discountAmount;

        /**
        * 明细税费
        */
        private BigDecimal taxAmount;

        /**
        * 支付时间
        */
        private LocalDateTime payTime;

        /**
        * 产品名称
        */
        private String productName;

        /**
        * 平台创建时间
        */
        private LocalDateTime platformCreateTime;

        /**
        * 数量
        */
        private String qty;

        /**
        * 收件人
        */
        private String recipientName;

        /**
        * 运费价格
        */
        private BigDecimal shippingPrice;

        /**
        * 运费税
        */
        private BigDecimal shippingTax;

        /**
        * 平台sku
        */
        private String platformSku;

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
        * 来源平台
        */
        @NotBlank(message = "来源平台不能为空")
        @Size(max = 50,message = "来源平台最大长度不能超过50位")
        private String sourcePlatform;

        /**
        * 来源系统
        */
        @NotBlank(message = "来源系统不能为空")
        @Size(max = 25,message = "来源系统最大长度不能超过25位")
        private String sourceSystem;

        /**
        * 来源平台单号
        */
        @NotBlank(message = "来源平台单号不能为空")
        @Size(max = 64,message = "来源平台单号最大长度不能超过64位")
        private String thirdCode;

        /**
        * 来源系统单号
        */
        @NotBlank(message = "来源系统单号不能为空")
        @Size(max = 255,message = "来源系统单号最大长度不能超过255位")
        private String platformCode;

        /**
        * 来源系统明细ID
        */
        @NotBlank(message = "来源系统明细ID不能为空")
        @Size(max = 255,message = "来源系统明细ID最大长度不能超过255位")
        private String thirdDetailId;

        /**
        * 来源平台明细ID
        */
        @NotBlank(message = "来源平台明细ID不能为空")
        @Size(max = 64,message = "来源平台明细ID最大长度不能超过64位")
        private String platformDetailId;

        /**
        * 来源ID
        */
        @NotBlank(message = "来源ID不能为空")
        @Size(max = 19,message = "来源ID最大长度不能超过19位")
        private String sourceId;

        /**
        * ERP店铺ID
        */
        @NotBlank(message = "ERP店铺ID不能为空")
        @Size(max = 255,message = "ERP店铺ID最大长度不能超过255位")
        private String shopId;

        /**
        * 账单地址1
        */
        @NotBlank(message = "账单地址1不能为空")
        @Size(max = 255,message = "账单地址1最大长度不能超过255位")
        private String address1;

        /**
        * 账单地址2
        */
        @NotBlank(message = "账单地址2不能为空")
        @Size(max = 255,message = "账单地址2最大长度不能超过255位")
        private String address2;

        /**
        * 账单地址3
        */
        @NotBlank(message = "账单地址3不能为空")
        @Size(max = 255,message = "账单地址3最大长度不能超过255位")
        private String address3;

        /**
        * 账单城市
        */
        @NotBlank(message = "账单城市不能为空")
        @Size(max = 255,message = "账单城市最大长度不能超过255位")
        private String city;

        /**
        * 账单国家
        */
        @NotBlank(message = "账单国家不能为空")
        @Size(max = 255,message = "账单国家最大长度不能超过255位")
        private String country;

        /**
        * 账号邮编
        */
        @NotBlank(message = "账号邮编不能为空")
        @Size(max = 255,message = "账号邮编最大长度不能超过255位")
        private String postalCode;

        /**
        * 账号州
        */
        @NotBlank(message = "账号州不能为空")
        @Size(max = 255,message = "账号州最大长度不能超过255位")
        private String state;

        /**
        * 买家邮箱
        */
        @NotBlank(message = "买家邮箱不能为空")
        @Size(max = 255,message = "买家邮箱最大长度不能超过255位")
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
        * 账单币种
        */
        @NotBlank(message = "账单币种不能为空")
        @Size(max = 255,message = "账单币种最大长度不能超过255位")
        private String currency;

        /**
        * 包装费用
        */
        @NotNull(message = "包装费用不能为空")
        @Digits(integer = 18, fraction = 6, message = "包装费用整数位不能超过18位，小数位不能超过6位")
        private BigDecimal giftWrapPrice;

        /**
        * 包装费用税
        */
        @NotNull(message = "包装费用税不能为空")
        @Digits(integer = 18, fraction = 6, message = "包装费用税整数位不能超过18位，小数位不能超过6位")
        private BigDecimal giftWrapTax;

        /**
        * 明细价格
        */
        @NotNull(message = "明细价格不能为空")
        @Digits(integer = 18, fraction = 6, message = "明细价格整数位不能超过18位，小数位不能超过6位")
        private BigDecimal sellPrice;

        /**
        * 明细折扣价格
        */
        @NotNull(message = "明细折扣价格不能为空")
        @Digits(integer = 18, fraction = 6, message = "明细折扣价格整数位不能超过18位，小数位不能超过6位")
        private BigDecimal discountAmount;

        /**
        * 明细税费
        */
        @NotNull(message = "明细税费不能为空")
        @Digits(integer = 18, fraction = 6, message = "明细税费整数位不能超过18位，小数位不能超过6位")
        private BigDecimal taxAmount;

        /**
        * 支付时间
        */
        private LocalDateTime payTime;

        /**
        * 产品名称
        */
        @NotBlank(message = "产品名称不能为空")
        private String productName;

        /**
        * 平台创建时间
        */
        private LocalDateTime platformCreateTime;

        /**
        * 数量
        */
        @NotBlank(message = "数量不能为空")
        @Size(max = 255,message = "数量最大长度不能超过255位")
        private String qty;

        /**
        * 收件人
        */
        @NotBlank(message = "收件人不能为空")
        @Size(max = 255,message = "收件人最大长度不能超过255位")
        private String recipientName;

        /**
        * 运费价格
        */
        @NotNull(message = "运费价格不能为空")
        @Digits(integer = 18, fraction = 6, message = "运费价格整数位不能超过18位，小数位不能超过6位")
        private BigDecimal shippingPrice;

        /**
        * 运费税
        */
        @NotNull(message = "运费税不能为空")
        @Digits(integer = 18, fraction = 6, message = "运费税整数位不能超过18位，小数位不能超过6位")
        private BigDecimal shippingTax;

        /**
        * 平台sku
        */
        @NotBlank(message = "平台sku不能为空")
        @Size(max = 255,message = "平台sku最大长度不能超过255位")
        private String platformSku;

        /**
        * 物流单号
        */
        @NotBlank(message = "物流单号不能为空")
        @Size(max = 255,message = "物流单号最大长度不能超过255位")
        private String trackingNumber;


    }


}