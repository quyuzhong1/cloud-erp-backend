package com.erp.model.oms.dto;

import java.math.BigDecimal;

import com.erp.model.wms.dto.SampleBorrowDetailDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import javax.validation.constraints.*;

/**
 * <p>
 * 展会订单详情请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-08-29
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
        * 台账id
        */
        private String sampleLedgerId;

        /**
        * skuid
        */
        private String skuId;

        /**
        * sku no
        */
        private String skuNo;
        /**
        * 产品名称
        */
        private String productName;

        /**
         * SPU ID
         */
        private String spuId;

        /**
         * SPU编号
         */
        private String spuNo;

        /**
         * SPU名称
         */
        private String spuName;

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
        /**
         * 单位
         */
        private String unit;

        /**
         * 历史最高
         */
        private BigDecimal maxPrice;

        /**
         * 历史最低
         */
        private BigDecimal minPrice;

        /**
         * 平均价格
         */
        private BigDecimal avgPrice;

        /**
         * 销售单价(本位币)
         */
        private BigDecimal priceLc;

        /**
         * 含税单价(本位币)
         */
        private BigDecimal taxPriceLc;

        /**
         * 使用方
         */
        private String useUserId;

        /**
         * 使用方名称
         */
        private String useUserName;

        /**
         * 可销售数量
         */
        private Integer availableQty = 0;

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
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {
        /**
         * 台账id
         */
        @NotBlank(message = "台账id不能为空")
        private String sampleLedgerId;

        /**
        * 主表id
        */
        private String mainId;

        /**
        * skuid
        */
        @NotBlank(message = "SKU不能为空")
        private String skuId;

        /**
        * 销售数量
        */
        @NotNull(message = "销售数量不能为空")
        @Min(value = 1,message = "销售数量不能小于1")
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
        * 币种
        */
        private String currency;

        /**
        * 是否赠品
        */
        @NotNull(message = "是否赠品不能为空")
        private Boolean isGift;

        /**
        * 备注
        */
        @Size(max = 250,message = "备注最大长度不能超过250位")
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
        * 含税单价
        */
        private BigDecimal taxPrice;


    }


    /**
     *
     */
    @Data
    @NoArgsConstructor
    public static class SkuQtyDetailDTO {

        private String id;
        private String detailId;
        private String sampleLedgerId;
        private String skuId;
        private String skuNo;
        private Integer qty;
        private String approveStatus;
    }


    @Data
    @NoArgsConstructor
    public static class ImportDTO {
        /**
         * 成功返回数据
         */
        private List<SkuDTO> successList;

        /**
         * 错误url
         */
        private String errorUrl;
    }


    /**
     * SKU 信息
     */
    @Data
    @NoArgsConstructor
    public static class SkuDTO{
        /**
         * 台账id
         */
        private String sampleLedgerId;
        /**
         * 使用方id
         */
        private String useUserId;

        /**
         * 使用方名称
         */
        private String useUserName;

        /**
         * sku id
         */
        private String skuId;

        /**
         * sku no
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 销售数量
         */
        private Integer qty;

        /**
         * 可销售数量
         */
        private Integer availableQty;

        /**
         * 单价
         */
        private BigDecimal price;


        /**
         * 税率
         */
        private BigDecimal taxRate;


        /**
         * 含税单价
         */
        private BigDecimal taxPrice;

        /**
         * 单位
         */
        private String unit;

        /**
         * 销售金额
         */
        private BigDecimal amount;

        /**
         * 价税销售金额
         */
        private BigDecimal taxAmount;

        /**
         * 历史最高
         */
        private BigDecimal maxPrice;

        /**
         * 历史最低
         */
        private BigDecimal minPrice;

        /**
         * 平均价格
         */
        private BigDecimal avgPrice;


        /**
         * 是否赠品
         */
        private Boolean isGift;

        /**
         * 备注
         */
        private String remark;

    }


}