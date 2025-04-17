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
 * 中台配货单主表请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2025-04-17
*/
@Data
@NoArgsConstructor
public class DmpSoDeliveryDTO implements Serializable {




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
        * 订单来源平台（编码）：Amazon，AliExpress，shopify，...
        */
        private String sourcePlatform;

        /**
        * 来源平台：gyy，kingdee，mabang
        */
        private String sourceSystem;

        /**
        * 第三方配货id
        */
        private String thirdDeliveryId;

        /**
        * 第三方配货单据编号
        */
        private String thirdDeliveryCode;

        /**
        * 第三方创建时间
        */
        private LocalDateTime thirdCreateTime;

        /**
        * 第三方更新时间
        */
        private LocalDateTime thirdUpdateTime;

        /**
        * 支付时间
        */
        private LocalDateTime payTime;

        /**
        * 发货状态
        */
        private String deliveryStatus;

        /**
        * 交易类型
        */
        private String transactionType;

        /**
        * 交易子类型
        */
        private String transactionSubType;

        /**
        * 商品总成交金额（合计）
        */
        private BigDecimal allAmount;

        /**
        * 优惠抵扣金额|佣金（合计）
        */
        private BigDecimal totalDiscountAmount;

        /**
        * 取消商品总金额（合计）
        */
        private BigDecimal totalCancelAmount;

        /**
        * 支付金额
        */
        private BigDecimal payAmount;

        /**
        * 运费收入
        */
        private BigDecimal shippingAmount;

        /**
        * 税金
        */
        private BigDecimal totalTaxAmount;

        /**
        * 商品总数量 （合计）
        */
        private Integer totalQty;

        /**
        * 取消商品数量 （合计）
        */
        private Integer cancelQty;

        /**
        * 订单应发数量（合计）
        */
        private Integer shippingQty;

        /**
        * 销售组织编码
        */
        private String salesCompanyCode;

        /**
        * 收款组织编码
        */
        private String receivingCompanyCode;

        /**
        * 组织名称
        */
        private String organizationName;

        /**
        * 组织编码
        */
        private String organizationCode;

        /**
        * 平台名称
        */
        private String platformName;

        /**
        * 子平台编码
        */
        private String subplatformNo;

        /**
        * 子平台名称
        */
        private String subplatformName;

        /**
        * 店铺名称
        */
        private String shopName;

        /**
        * 店铺编码
        */
        private String shopNo;

        /**
        * 平台销售订单号
        */
        private String platformCode;

        /**
        * 数据来源
        */
        private String dataSource;

        /**
        * 备注
        */
        private String remark;

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
        * 第三方配货id
        */
        @NotBlank(message = "第三方配货id不能为空")
        @Size(max = 64,message = "第三方配货id最大长度不能超过64位")
        private String thirdDeliveryId;

        /**
        * 第三方配货单据编号
        */
        @NotBlank(message = "第三方配货单据编号不能为空")
        @Size(max = 64,message = "第三方配货单据编号最大长度不能超过64位")
        private String thirdDeliveryCode;

        /**
        * 第三方创建时间
        */
        private LocalDateTime thirdCreateTime;

        /**
        * 第三方更新时间
        */
        private LocalDateTime thirdUpdateTime;

        /**
        * 支付时间
        */
        private LocalDateTime payTime;

        /**
        * 发货状态
        */
        @NotBlank(message = "发货状态不能为空")
        @Size(max = 25,message = "发货状态最大长度不能超过25位")
        private String deliveryStatus;

        /**
        * 交易类型
        */
        @NotBlank(message = "交易类型不能为空")
        @Size(max = 64,message = "交易类型最大长度不能超过64位")
        private String transactionType;

        /**
        * 交易子类型
        */
        @NotBlank(message = "交易子类型不能为空")
        @Size(max = 64,message = "交易子类型最大长度不能超过64位")
        private String transactionSubType;

        /**
        * 商品总成交金额（合计）
        */
        @NotNull(message = "商品总成交金额（合计）不能为空")
        @Digits(integer = 12, fraction = 4, message = "商品总成交金额（合计）整数位不能超过12位，小数位不能超过4位")
        private BigDecimal allAmount;

        /**
        * 优惠抵扣金额|佣金（合计）
        */
        @NotNull(message = "优惠抵扣金额|佣金（合计）不能为空")
        @Digits(integer = 12, fraction = 4, message = "优惠抵扣金额|佣金（合计）整数位不能超过12位，小数位不能超过4位")
        private BigDecimal totalDiscountAmount;

        /**
        * 取消商品总金额（合计）
        */
        @NotNull(message = "取消商品总金额（合计）不能为空")
        @Digits(integer = 12, fraction = 4, message = "取消商品总金额（合计）整数位不能超过12位，小数位不能超过4位")
        private BigDecimal totalCancelAmount;

        /**
        * 支付金额
        */
        @NotNull(message = "支付金额不能为空")
        @Digits(integer = 12, fraction = 4, message = "支付金额整数位不能超过12位，小数位不能超过4位")
        private BigDecimal payAmount;

        /**
        * 运费收入
        */
        @NotNull(message = "运费收入不能为空")
        @Digits(integer = 12, fraction = 4, message = "运费收入整数位不能超过12位，小数位不能超过4位")
        private BigDecimal shippingAmount;

        /**
        * 税金
        */
        @NotNull(message = "税金不能为空")
        @Digits(integer = 12, fraction = 4, message = "税金整数位不能超过12位，小数位不能超过4位")
        private BigDecimal totalTaxAmount;

        /**
        * 商品总数量 （合计）
        */
        @NotNull(message = "商品总数量 （合计）不能为空")
        private Integer totalQty;

        /**
        * 取消商品数量 （合计）
        */
        @NotNull(message = "取消商品数量 （合计）不能为空")
        private Integer cancelQty;

        /**
        * 订单应发数量（合计）
        */
        @NotNull(message = "订单应发数量（合计）不能为空")
        private Integer shippingQty;

        /**
        * 销售组织编码
        */
        @NotBlank(message = "销售组织编码不能为空")
        @Size(max = 255,message = "销售组织编码最大长度不能超过255位")
        private String salesCompanyCode;

        /**
        * 收款组织编码
        */
        @NotBlank(message = "收款组织编码不能为空")
        @Size(max = 255,message = "收款组织编码最大长度不能超过255位")
        private String receivingCompanyCode;

        /**
        * 组织名称
        */
        @NotBlank(message = "组织名称不能为空")
        @Size(max = 255,message = "组织名称最大长度不能超过255位")
        private String organizationName;

        /**
        * 组织编码
        */
        @NotBlank(message = "组织编码不能为空")
        @Size(max = 255,message = "组织编码最大长度不能超过255位")
        private String organizationCode;

        /**
        * 平台名称
        */
        @NotBlank(message = "平台名称不能为空")
        @Size(max = 255,message = "平台名称最大长度不能超过255位")
        private String platformName;

        /**
        * 子平台编码
        */
        @NotBlank(message = "子平台编码不能为空")
        @Size(max = 255,message = "子平台编码最大长度不能超过255位")
        private String subplatformNo;

        /**
        * 子平台名称
        */
        @NotBlank(message = "子平台名称不能为空")
        @Size(max = 255,message = "子平台名称最大长度不能超过255位")
        private String subplatformName;

        /**
        * 店铺名称
        */
        @NotBlank(message = "店铺名称不能为空")
        @Size(max = 255,message = "店铺名称最大长度不能超过255位")
        private String shopName;

        /**
        * 店铺编码
        */
        @NotBlank(message = "店铺编码不能为空")
        @Size(max = 255,message = "店铺编码最大长度不能超过255位")
        private String shopNo;

        /**
        * 平台销售订单号
        */
        @NotBlank(message = "平台销售订单号不能为空")
        @Size(max = 255,message = "平台销售订单号最大长度不能超过255位")
        private String platformCode;

        /**
        * 数据来源
        */
        @NotBlank(message = "数据来源不能为空")
        @Size(max = 50,message = "数据来源最大长度不能超过50位")
        private String dataSource;

        /**
        * 备注
        */
        private String remark;

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