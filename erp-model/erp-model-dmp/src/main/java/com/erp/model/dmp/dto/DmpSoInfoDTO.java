package com.erp.model.dmp.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.common.core.anno.Panno;
import com.common.core.enums.PannoEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Digits;

/**
 * <p>
 * 中台销售订单表请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-06-24
*/
@Data
@NoArgsConstructor
public class DmpSoInfoDTO implements Serializable {




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
        * 作废状态（false未作废，true已作废）
        */
        private Boolean invalidStatus;

        /**
        * 订单状态 waitSubmit.待提交 approveIng.审核中 reject.审核不通过 approve.已审核
        */
        private String orderStatus;

        /**
        * 发货状态 waitDistribution:待配货，inDistribution:配货中,waitShipped:待发货，shipped：已发货，partialShipped：部分发货
        */
        private String deliveryStatus;

        /**
        * 退货状态 orderReturn：已退货，partialReturn部分退，notReturn：未退货
        */
        private String returnStatus;

        /**
        * 平台原始状态
        */
        private String platformOriginalStatus;

        /**
        * 店铺编号
        */
        private String shopId;

        /**
        * 店铺名称
        */
        private String shopName;

        /**
        * 卖家备注
        */
        private String sellRemark;

        /**
        * 买家备注
        */
        private String buyerRemark;

        /**
        * 支付时间
        */
        private LocalDateTime payTime;

        /**
        * 付款状态 （false未付款，true已付款）
        */
        private Boolean payStatus;

        /**
        * 付款方式
        */
        private String payMethod;

        /**
        * 币种编码
        */
        private String currencyCode;

        /**
        * 汇率
        */
        private BigDecimal exchangeRate;

        /**
        * 税率
        */
        private BigDecimal taxRate;

        /**
        * 订单总金额
        */
        private BigDecimal payAmount;

        /**
        * 商品总售价
        */
        private BigDecimal allAmount;

        /**
        * 运费收入
        */
        private BigDecimal shippingAmount;

        /**
        * 平台费
        */
        private BigDecimal platformCost;

        /**
        * 补贴金额
        */
        private BigDecimal subsidyAmount;

        /**
        * 发货时间
        */
        private LocalDateTime deliveryTime;

        /**
        * 物流单号
        */
        private String logisticsCode;

        /**
        * 物流名称
        */
        private String logisticsName;

        /**
        * 拓展字段
        */
        private String extendData;

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
        @Size(max = 64,message = "销售平台原始单号最大长度不能超过64位")
        private String platformCode;

        /**
        * 订单状态 waitSubmit.待提交 approveIng.审核中 reject.审核不通过 approve.已审核
        */
        @NotBlank(message = "订单状态 waitSubmit.待提交 approveIng.审核中 reject.审核不通过 approve.已审核不能为空")
        @Size(max = 25,message = "订单状态 waitSubmit.待提交 approveIng.审核中 reject.审核不通过 approve.已审核最大长度不能超过25位")
        private String orderStatus;

        /**
        * 发货状态 waitDistribution:待配货，inDistribution:配货中,waitShipped:待发货，shipped：已发货，partialShipped：部分发货
        */
        @NotBlank(message = "发货状态 waitDistribution:待配货，inDistribution:配货中,waitShipped:待发货，shipped：已发货，partialShipped：部分发货不能为空")
        @Size(max = 25,message = "发货状态 waitDistribution:待配货，inDistribution:配货中,waitShipped:待发货，shipped：已发货，partialShipped：部分发货最大长度不能超过25位")
        private String deliveryStatus;

        /**
        * 退货状态 orderReturn：已退货，partialReturn部分退，notReturn：未退货
        */
        @NotBlank(message = "退货状态 orderReturn：已退货，partialReturn部分退，notReturn：未退货不能为空")
        @Size(max = 25,message = "退货状态 orderReturn：已退货，partialReturn部分退，notReturn：未退货最大长度不能超过25位")
        private String returnStatus;

        /**
        * 平台原始状态
        */
        @NotBlank(message = "平台原始状态不能为空")
        @Size(max = 25,message = "平台原始状态最大长度不能超过25位")
        private String platformOriginalStatus;

        /**
        * 店铺编号
        */
        @NotBlank(message = "店铺编号不能为空")
        @Size(max = 64,message = "店铺编号最大长度不能超过64位")
        private String shopId;

        /**
        * 店铺名称
        */
        @NotBlank(message = "店铺名称不能为空")
        @Size(max = 64,message = "店铺名称最大长度不能超过64位")
        private String shopName;

        /**
        * 卖家备注
        */
        @NotBlank(message = "卖家备注不能为空")
        @Size(max = 500,message = "卖家备注最大长度不能超过500位")
        private String sellRemark;

        /**
        * 买家备注
        */
        @NotBlank(message = "买家备注不能为空")
        @Size(max = 500,message = "买家备注最大长度不能超过500位")
        private String buyerRemark;

        /**
        * 支付时间
        */
        private LocalDateTime payTime;

        /**
        * 付款状态 （false未付款，true已付款）
        */
        @NotNull(message = "付款状态 （false未付款，true已付款）不能为空")
        private Boolean payStatus;

        /**
        * 付款方式
        */
        @NotBlank(message = "付款方式不能为空")
        @Size(max = 64,message = "付款方式最大长度不能超过64位")
        private String payMethod;

        /**
        * 币种编码
        */
        @NotBlank(message = "币种编码不能为空")
        @Size(max = 25,message = "币种编码最大长度不能超过25位")
        private String currencyCode;

        /**
        * 汇率
        */
        @NotNull(message = "汇率不能为空")
        @Digits(integer = 12, fraction = 4, message = "汇率整数位不能超过12位，小数位不能超过4位")
        private BigDecimal exchangeRate;

        /**
        * 税率
        */
        @NotNull(message = "税率不能为空")
        @Digits(integer = 12, fraction = 4, message = "税率整数位不能超过12位，小数位不能超过4位")
        private BigDecimal taxRate;

        /**
        * 订单总金额
        */
        @NotNull(message = "订单总金额不能为空")
        @Digits(integer = 12, fraction = 4, message = "订单总金额整数位不能超过12位，小数位不能超过4位")
        private BigDecimal payAmount;

        /**
        * 商品总售价
        */
        @NotNull(message = "商品总售价不能为空")
        @Digits(integer = 12, fraction = 4, message = "商品总售价整数位不能超过12位，小数位不能超过4位")
        private BigDecimal allAmount;

        /**
        * 运费收入
        */
        @NotNull(message = "运费收入不能为空")
        @Digits(integer = 12, fraction = 4, message = "运费收入整数位不能超过12位，小数位不能超过4位")
        private BigDecimal shippingAmount;

        /**
        * 平台费
        */
        @NotNull(message = "平台费不能为空")
        @Digits(integer = 12, fraction = 4, message = "平台费整数位不能超过12位，小数位不能超过4位")
        private BigDecimal platformCost;

        /**
        * 补贴金额
        */
        @NotNull(message = "补贴金额不能为空")
        @Digits(integer = 12, fraction = 4, message = "补贴金额整数位不能超过12位，小数位不能超过4位")
        private BigDecimal subsidyAmount;

        /**
        * 发货时间
        */
        private LocalDateTime deliveryTime;

        /**
        * 物流单号
        */
        @NotBlank(message = "物流单号不能为空")
        @Size(max = 64,message = "物流单号最大长度不能超过64位")
        private String logisticsCode;

        /**
        * 物流名称
        */
        @NotBlank(message = "物流名称不能为空")
        @Size(max = 255,message = "物流名称最大长度不能超过255位")
        private String logisticsName;

        /**
        * 拓展字段
        */
        @NotBlank(message = "拓展字段不能为空")
        private String extendData;

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


    /**
     * 添加管易订单入参
     */
    @Data
    @NoArgsConstructor
    public static class addGyyOrderDTO{

        /**
         * 开始时间
         */
        @Panno(findType = PannoEnum.GTE,field = "createtime")
        private String startTime;

        /**
         * 结算时间
         */
        @Panno(findType = PannoEnum.LTE,field = "createtime")
        private String endTime;

    }
}