package com.erp.model.dmp.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
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
 * 中台销售退款单主表请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-07-12
*/
@Data
@NoArgsConstructor
public class DmpSoRefundInfoDTO implements Serializable {




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
        * 退款时间
        */
        private LocalDateTime refundTime;

        /**
        * 订单来源平台（编码）：Amazon，AliExpress，shopify，...
        */
        private String sourcePlatform;

        /**
        * 来源平台：gyy，kingdee，mabang
        */
        private String sourceSystem;

        /**
        * 单据编号（唯一）
        */
        private String thirdCode;

        /**
        * 销售平台原始单号
        */
        private String platformCode;

        /**
        * 退款原因
        */
        private String reason;

        /**
        * 备注
        */
        private String remark;

        /**
        * 退款状态：1、成功 2、失败 3、作废
        */
        private String status;

        /**
        * 平台原始状态
        */
        private String platformOriginalStatus;

        /**
        * 店铺id
        */
        private String shopId;

        /**
        * 店铺名称
        */
        private String shopName;

        /**
        * 国家二字码
        */
        private String country;

        /**
        * 买家账号
        */
        private String buyerUserId;

        /**
        * 买家姓名
        */
        private String buyerName;

        /**
        * 币别
        */
        private String currencyCode;

        /**
        * 汇率
        */
        private BigDecimal currencyRate;

        /**
        * 退货金额
        */
        private BigDecimal amount;

        /**
        * 运费
        */
        private BigDecimal shippingCost;

        private LocalDate deliveryTime;

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
        * 退款时间
        */
        private LocalDateTime refundTime;

        /**
        * 订单来源平台（编码）：Amazon，AliExpress，shopify，...
        */
        @NotBlank(message = "订单来源平台（编码）：Amazon，AliExpress，shopify，...不能为空")
        @Size(max = 64,message = "订单来源平台（编码）：Amazon，AliExpress，shopify，...最大长度不能超过64位")
        private String sourcePlatform;

        /**
        * 来源平台：gyy，kingdee，mabang
        */
        @NotBlank(message = "来源平台：gyy，kingdee，mabang不能为空")
        @Size(max = 32,message = "来源平台：gyy，kingdee，mabang最大长度不能超过32位")
        private String sourceSystem;

        /**
        * 单据编号（唯一）
        */
        @NotBlank(message = "单据编号（唯一）不能为空")
        @Size(max = 64,message = "单据编号（唯一）最大长度不能超过64位")
        private String thirdCode;

        /**
        * 销售平台原始单号
        */
        @NotBlank(message = "销售平台原始单号不能为空")
        @Size(max = 64,message = "销售平台原始单号最大长度不能超过64位")
        private String platformCode;

        /**
        * 退款原因
        */
        @NotBlank(message = "退款原因不能为空")
        @Size(max = 64,message = "退款原因最大长度不能超过64位")
        private String reason;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;

        /**
        * 退款状态：1、成功 2、失败 3、作废
        */
        @NotBlank(message = "退款状态：1、成功 2、失败 3、作废不能为空")
        @Size(max = 10,message = "退款状态：1、成功 2、失败 3、作废最大长度不能超过10位")
        private String status;

        /**
        * 平台原始状态
        */
        @NotBlank(message = "平台原始状态不能为空")
        @Size(max = 32,message = "平台原始状态最大长度不能超过32位")
        private String platformOriginalStatus;

        /**
        * 店铺id
        */
        @NotBlank(message = "店铺id不能为空")
        @Size(max = 32,message = "店铺id最大长度不能超过32位")
        private String shopId;

        /**
        * 店铺名称
        */
        @NotBlank(message = "店铺名称不能为空")
        @Size(max = 64,message = "店铺名称最大长度不能超过64位")
        private String shopName;

        /**
        * 国家二字码
        */
        @NotBlank(message = "国家二字码不能为空")
        @Size(max = 10,message = "国家二字码最大长度不能超过10位")
        private String country;

        /**
        * 买家账号
        */
        @NotBlank(message = "买家账号不能为空")
        @Size(max = 32,message = "买家账号最大长度不能超过32位")
        private String buyerUserId;

        /**
        * 买家姓名
        */
        @NotBlank(message = "买家姓名不能为空")
        @Size(max = 32,message = "买家姓名最大长度不能超过32位")
        private String buyerName;

        /**
        * 币别
        */
        @NotBlank(message = "币别不能为空")
        @Size(max = 16,message = "币别最大长度不能超过16位")
        private String currencyCode;

        /**
        * 汇率
        */
        @NotNull(message = "汇率不能为空")
        @Digits(integer = 18, fraction = 6, message = "汇率整数位不能超过18位，小数位不能超过6位")
        private BigDecimal currencyRate;

        /**
        * 退货金额
        */
        @NotNull(message = "退货金额不能为空")
        @Digits(integer = 18, fraction = 6, message = "退货金额整数位不能超过18位，小数位不能超过6位")
        private BigDecimal amount;

        /**
        * 运费
        */
        @NotNull(message = "运费不能为空")
        @Digits(integer = 18, fraction = 6, message = "运费整数位不能超过18位，小数位不能超过6位")
        private BigDecimal shippingCost;

        private LocalDate deliveryTime;

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


    }


}