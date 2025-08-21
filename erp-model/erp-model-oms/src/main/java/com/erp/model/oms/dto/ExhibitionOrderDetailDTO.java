package com.erp.model.oms.dto;

import java.math.BigDecimal;
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
 * 展会订单详情请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-08-20
*/
@Data
@NoArgsConstructor
public class ExhibitionOrderDetailDTO implements Serializable {




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
        * 主表id
        */
        private String mainId;

        /**
        * skuid
        */
        private String skuId;

        /**
        * sku no
        */
        private String skuNo;

        /**
        * 销售数量
        */
        private Integer qty;

        /**
        * 单价
        */
        private BigDecimal price;

        /**
        * 税率
        */
        private BigDecimal taxRate;

        /**
        * 销售金额
        */
        private BigDecimal amount;

        /**
        * 币种
        */
        private String currency;

        /**
        * 币种符号
        */
        private String currencySymbol;

        /**
        * 是否赠品
        */
        private Boolean isGift;

        /**
        * 备注
        */
        private String remark;

        /**
        * 采购单价
        */
        private BigDecimal purchasePrice;

        /**
        * 销售总成本
        */
        private BigDecimal saleCost;

        /**
        * 销售毛利
        */
        private BigDecimal saleProfit;

        /**
        * 销售毛利率
        */
        private BigDecimal saleProfitRate;

        /**
        * 含税的销售金额折后
        */
        private BigDecimal taxAmount;

        /**
        * 销售金额本位币
        */
        private BigDecimal amountLocalCurrency;

        /**
        * 价税合计本位币
        */
        private BigDecimal allAmountLocalCurrency;

        /**
        * 折扣额
        */
        private BigDecimal discountAmount;

        /**
        * 含税的销售金额折扣前
        */
        private BigDecimal taxAmountBefore;

        /**
        * 销售金额计算汇率
        */
        private BigDecimal exchangeRate;

        /**
        * 税额
        */
        private BigDecimal tax;

        /**
        * bom版本
        */
        private String bomVersion;

        /**
        * 成本来源
        */
        private String costSource;

        /**
        * 含税单价
        */
        private BigDecimal taxPrice;


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
        * 主表id
        */
        @NotBlank(message = "主表id不能为空")
        @Size(max = 19,message = "主表id最大长度不能超过19位")
        private String mainId;

        /**
        * skuid
        */
        @NotBlank(message = "skuid不能为空")
        @Size(max = 19,message = "skuid最大长度不能超过19位")
        private String skuId;

        /**
        * 销售数量
        */
        @NotNull(message = "销售数量不能为空")
        private Integer qty;

        /**
        * 单价
        */
        @NotNull(message = "单价不能为空")
        @Digits(integer = 12, fraction = 4, message = "单价整数位不能超过12位，小数位不能超过4位")
        private BigDecimal price;

        /**
        * 税率
        */
        @NotNull(message = "税率不能为空")
        @Digits(integer = 12, fraction = 4, message = "税率整数位不能超过12位，小数位不能超过4位")
        private BigDecimal taxRate;

        /**
        * 销售金额
        */
        @NotNull(message = "销售金额不能为空")
        @Digits(integer = 12, fraction = 4, message = "销售金额整数位不能超过12位，小数位不能超过4位")
        private BigDecimal amount;

        /**
        * 币种
        */
        @NotBlank(message = "币种不能为空")
        @Size(max = 20,message = "币种最大长度不能超过20位")
        private String currency;

        /**
        * 币种符号
        */
        @NotBlank(message = "币种符号不能为空")
        @Size(max = 10,message = "币种符号最大长度不能超过10位")
        private String currencySymbol;

        /**
        * 是否赠品
        */
        @NotNull(message = "是否赠品不能为空")
        private Boolean isGift;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 250,message = "备注最大长度不能超过250位")
        private String remark;

        /**
        * 采购单价
        */
        @NotNull(message = "采购单价不能为空")
        @Digits(integer = 12, fraction = 4, message = "采购单价整数位不能超过12位，小数位不能超过4位")
        private BigDecimal purchasePrice;

        /**
        * 销售总成本
        */
        @NotNull(message = "销售总成本不能为空")
        @Digits(integer = 12, fraction = 4, message = "销售总成本整数位不能超过12位，小数位不能超过4位")
        private BigDecimal saleCost;

        /**
        * 销售毛利
        */
        @NotNull(message = "销售毛利不能为空")
        @Digits(integer = 12, fraction = 4, message = "销售毛利整数位不能超过12位，小数位不能超过4位")
        private BigDecimal saleProfit;

        /**
        * 销售毛利率
        */
        @NotNull(message = "销售毛利率不能为空")
        @Digits(integer = 12, fraction = 4, message = "销售毛利率整数位不能超过12位，小数位不能超过4位")
        private BigDecimal saleProfitRate;

        /**
        * 含税的销售金额折后
        */
        @NotNull(message = "含税的销售金额折后不能为空")
        @Digits(integer = 12, fraction = 4, message = "含税的销售金额折后整数位不能超过12位，小数位不能超过4位")
        private BigDecimal taxAmount;

        /**
        * 销售金额本位币
        */
        @NotNull(message = "销售金额本位币不能为空")
        @Digits(integer = 12, fraction = 4, message = "销售金额本位币整数位不能超过12位，小数位不能超过4位")
        private BigDecimal amountLocalCurrency;

        /**
        * 价税合计本位币
        */
        @NotNull(message = "价税合计本位币不能为空")
        @Digits(integer = 12, fraction = 4, message = "价税合计本位币整数位不能超过12位，小数位不能超过4位")
        private BigDecimal allAmountLocalCurrency;

        /**
        * 折扣额
        */
        @NotNull(message = "折扣额不能为空")
        @Digits(integer = 12, fraction = 4, message = "折扣额整数位不能超过12位，小数位不能超过4位")
        private BigDecimal discountAmount;

        /**
        * 含税的销售金额折扣前
        */
        @NotNull(message = "含税的销售金额折扣前不能为空")
        @Digits(integer = 16, fraction = 4, message = "含税的销售金额折扣前整数位不能超过16位，小数位不能超过4位")
        private BigDecimal taxAmountBefore;

        /**
        * 销售金额计算汇率
        */
        @NotNull(message = "销售金额计算汇率不能为空")
        @Digits(integer = 8, fraction = 8, message = "销售金额计算汇率整数位不能超过8位，小数位不能超过8位")
        private BigDecimal exchangeRate;

        /**
        * 税额
        */
        @NotNull(message = "税额不能为空")
        @Digits(integer = 12, fraction = 4, message = "税额整数位不能超过12位，小数位不能超过4位")
        private BigDecimal tax;

        /**
        * bom版本
        */
        @NotBlank(message = "bom版本不能为空")
        @Size(max = 255,message = "bom版本最大长度不能超过255位")
        private String bomVersion;

        /**
        * 成本来源
        */
        @NotBlank(message = "成本来源不能为空")
        @Size(max = 30,message = "成本来源最大长度不能超过30位")
        private String costSource;

        /**
        * 含税单价
        */
        @NotNull(message = "含税单价不能为空")
        @Digits(integer = 12, fraction = 4, message = "含税单价整数位不能超过12位，小数位不能超过4位")
        private BigDecimal taxPrice;


    }


}