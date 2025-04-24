package com.erp.model.tms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 报关单明细请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2024-03-27
*/
@Data
@NoArgsConstructor
public class TmsDeclareBillDetailDTO implements Serializable {




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
        * 主记录Id
        */
        private String mainId;

        /**
        * sku id
        */
        private String skuId;

        /**
        * sku no
        */
        private String skuNo;

        /**
        * 数量
        */
        private Integer qty;

        /**
        * 单价
        */
        private BigDecimal price;

        /**
        * 中国海关编码
        */
        private String customsCode;

        /**
        * 报关中文名
        */
        private String declareChineseName;

        /**
        * 申报要素
        */
        private String declareElement;

        /**
        * 报关单位
        */
        private String declareUnit;

        /**
        * 报关币别
        */
        private String declareCurrency;

        /**
        * 报关币别符号
        */
        private String declareCurrencySymbol;

        /**
        * 原产国
        */
        private String sourceCountry;

        /**
        * 境内货源地
        */
        private String sourceCargo;

        /**
        * 征免
        */
        private String exemption;


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
        * 主记录Id
        */
        @NotBlank(message = "主记录Id不能为空")
        @Size(max = 50,message = "主记录Id最大长度不能超过50位")
        private String mainId;

        /**
        * sku id
        */
        @NotBlank(message = "sku id不能为空")
        @Size(max = 19,message = "sku id最大长度不能超过19位")
        private String skuId;

        /**
        * 数量
        */
        @NotNull(message = "数量不能为空")
        private Integer qty;

        /**
        * 单价
        */
        @NotNull(message = "单价不能为空")
        @Digits(integer = 12, fraction = 4, message = "单价整数位不能超过12位，小数位不能超过4位")
        private BigDecimal price;

        /**
        * 中国海关编码
        */
        private String customsCode;

        /**
        * 报关中文名
        */
        private String declareChineseName;

        /**
        * 申报要素
        */
        private String declareElement;

        /**
        * 报关单位
        */
        @NotBlank(message = "报关单位不能为空")
        @Size(max = 50,message = "报关单位最大长度不能超过50位")
        private String declareUnit;

        /**
        * 报关币别
        */
        @NotBlank(message = "报关币别不能为空")
        @Size(max = 30,message = "报关币别最大长度不能超过30位")
        private String declareCurrency;

        /**
        * 报关币别符号
        */
        @NotBlank(message = "报关币别符号不能为空")
        @Size(max = 30,message = "报关币别符号最大长度不能超过30位")
        private String declareCurrencySymbol;

        /**
        * 原产国
        */
        @NotBlank(message = "原产国不能为空")
        @Size(max = 50,message = "原产国最大长度不能超过50位")
        private String sourceCountry;

        /**
        * 境内货源地
        */
        @NotBlank(message = "境内货源地不能为空")
        @Size(max = 50,message = "境内货源地最大长度不能超过50位")
        private String sourceCargo;

        /**
        * 征免
        */
        @NotBlank(message = "征免不能为空")
        @Size(max = 50,message = "征免最大长度不能超过50位")
        private String exemption;


    }


}