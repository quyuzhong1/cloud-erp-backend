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
 * 中台原始销售订单表请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-11-25
*/
@Data
@NoArgsConstructor
public class DmpSoOriginalInfoDTO implements Serializable {




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
        * 订单来源平台（编码）：Amazon，AliExpress，shopify，...
        */
        private String sourcePlatform;

        /**
        * 来源平台：gyy，kingdee，mabang
        */
        private String sourceSystem;

        /**
        * 第三方单据编号
        */
        private String thirdCode;

        /**
        * 销售平台原始单号
        */
        private String platformCode;

        /**
        * 店铺编号
        */
        private String shopNo;

        /**
        * 平台状态
        */
        private String tradeStatus;

        /**
        * 支付状态
        */
        private String payStatus;

        /**
        * 下单时间
        */
        private LocalDateTime tradeTime;

        /**
        * 支付时间
        */
        private LocalDateTime payTime;

        /**
        * 买家留言
        */
        private String buyerMessage;

        /**
        * 客服备注
        */
        private String remark;

        /**
        * 邮箱
        */
        private String buyerEmail;

        /**
        * 买家姓名
        */
        private String buyerName;

        /**
        * 收件人姓名
        */
        private String receiverName;

        /**
        * 收件人国家
        */
        private String receiverCountry;

        /**
        * 收件人省份
        */
        private String receiverProvince;

        /**
        * 市
        */
        private String receiverCity;

        /**
        * 区
        */
        private String receiverDistrict;

        /**
        * 收件人地址
        */
        private String receiverAddress;

        /**
        * 收件人手机
        */
        private String receiverMobile;

        /**
        * 收件人电话
        */
        private String receiverTelno;

        /**
        * 优惠
        */
        private BigDecimal discount;

        /**
        * 买家已付金额
        */
        private BigDecimal paid;

        /**
        * 币种
        */
        private String currency;

        /**
        * 退款金额
        */
        private BigDecimal refundAmount;

        /**
        * 订单来源
        */
        private String tradeFrom;

        /**
        * 唯一字段md5值
        */
        private String uniqueEncrypt;

        /**
        * 数据字段md5值
        */
        private String dataEncrypt;

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
        * 订单来源平台（编码）：Amazon，AliExpress，shopify，...
        */
        @NotBlank(message = "订单来源平台（编码）：Amazon，AliExpress，shopify，...不能为空")
        @Size(max = 50,message = "订单来源平台（编码）：Amazon，AliExpress，shopify，...最大长度不能超过50位")
        private String sourcePlatform;

        /**
        * 来源平台：gyy，kingdee，mabang
        */
        @NotBlank(message = "来源平台：gyy，kingdee，mabang不能为空")
        @Size(max = 25,message = "来源平台：gyy，kingdee，mabang最大长度不能超过25位")
        private String sourceSystem;

        /**
        * 第三方单据编号
        */
        @NotBlank(message = "第三方单据编号不能为空")
        @Size(max = 64,message = "第三方单据编号最大长度不能超过64位")
        private String thirdCode;

        /**
        * 销售平台原始单号
        */
        @NotBlank(message = "销售平台原始单号不能为空")
        @Size(max = 255,message = "销售平台原始单号最大长度不能超过255位")
        private String platformCode;

        /**
        * 店铺编号
        */
        @NotBlank(message = "店铺编号不能为空")
        @Size(max = 64,message = "店铺编号最大长度不能超过64位")
        private String shopNo;

        /**
        * 平台状态
        */
        @NotBlank(message = "平台状态不能为空")
        @Size(max = 64,message = "平台状态最大长度不能超过64位")
        private String tradeStatus;

        /**
        * 支付状态
        */
        @NotBlank(message = "支付状态不能为空")
        @Size(max = 64,message = "支付状态最大长度不能超过64位")
        private String payStatus;

        /**
        * 下单时间
        */
        private LocalDateTime tradeTime;

        /**
        * 支付时间
        */
        private LocalDateTime payTime;

        /**
        * 买家留言
        */
        @NotBlank(message = "买家留言不能为空")
        @Size(max = 1024,message = "买家留言最大长度不能超过1,024位")
        private String buyerMessage;

        /**
        * 客服备注
        */
        @NotBlank(message = "客服备注不能为空")
        @Size(max = 1024,message = "客服备注最大长度不能超过1,024位")
        private String remark;

        /**
        * 邮箱
        */
        @NotBlank(message = "邮箱不能为空")
        @Size(max = 100,message = "邮箱最大长度不能超过100位")
        private String buyerEmail;

        /**
        * 买家姓名
        */
        @NotBlank(message = "买家姓名不能为空")
        @Size(max = 100,message = "买家姓名最大长度不能超过100位")
        private String buyerName;

        /**
        * 收件人姓名
        */
        @NotBlank(message = "收件人姓名不能为空")
        @Size(max = 100,message = "收件人姓名最大长度不能超过100位")
        private String receiverName;

        /**
        * 收件人国家
        */
        @NotBlank(message = "收件人国家不能为空")
        @Size(max = 6,message = "收件人国家最大长度不能超过6位")
        private String receiverCountry;

        /**
        * 收件人省份
        */
        @NotBlank(message = "收件人省份不能为空")
        @Size(max = 11,message = "收件人省份最大长度不能超过11位")
        private String receiverProvince;

        /**
        * 市
        */
        @NotBlank(message = "市不能为空")
        @Size(max = 11,message = "市最大长度不能超过11位")
        private String receiverCity;

        /**
        * 区
        */
        @NotBlank(message = "区不能为空")
        @Size(max = 11,message = "区最大长度不能超过11位")
        private String receiverDistrict;

        /**
        * 收件人地址
        */
        @NotBlank(message = "收件人地址不能为空")
        @Size(max = 255,message = "收件人地址最大长度不能超过255位")
        private String receiverAddress;

        /**
        * 收件人手机
        */
        @NotBlank(message = "收件人手机不能为空")
        @Size(max = 40,message = "收件人手机最大长度不能超过40位")
        private String receiverMobile;

        /**
        * 收件人电话
        */
        @NotBlank(message = "收件人电话不能为空")
        @Size(max = 40,message = "收件人电话最大长度不能超过40位")
        private String receiverTelno;

        /**
        * 优惠
        */
        @NotNull(message = "优惠不能为空")
        @Digits(integer = 12, fraction = 4, message = "优惠整数位不能超过12位，小数位不能超过4位")
        private BigDecimal discount;

        /**
        * 买家已付金额
        */
        @NotNull(message = "买家已付金额不能为空")
        @Digits(integer = 12, fraction = 4, message = "买家已付金额整数位不能超过12位，小数位不能超过4位")
        private BigDecimal paid;

        /**
        * 币种
        */
        @NotBlank(message = "币种不能为空")
        @Size(max = 64,message = "币种最大长度不能超过64位")
        private String currency;

        /**
        * 退款金额
        */
        @NotNull(message = "退款金额不能为空")
        @Digits(integer = 12, fraction = 4, message = "退款金额整数位不能超过12位，小数位不能超过4位")
        private BigDecimal refundAmount;

        /**
        * 订单来源
        */
        @NotBlank(message = "订单来源不能为空")
        @Size(max = 64,message = "订单来源最大长度不能超过64位")
        private String tradeFrom;

        /**
        * 唯一字段md5值
        */
        private String uniqueEncrypt;

        /**
        * 数据字段md5值
        */
        private String dataEncrypt;

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


    }


}