package com.erp.model.oms.dto;

import java.math.BigDecimal;
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
 * B2C销售订单申报产品信息表请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2024-05-09
*/
@Data
@NoArgsConstructor
public class SoB2cDeclareProductDTO implements Serializable {




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
        * 销售订单明细id
        */
        private String soDetailId;

        /**
        * 销售订单id
        */
        private String soId;

        /**
        * 产品sku编号
        */
        private String skuNo;

        /**
        * 申报数量
        */
        private Integer qty;

        /**
        * 中文报关名称
        */
        private String declareCn;

        /**
        * 英文报关名称
        */
        private String declareEn;

        /**
        * 目的国申报价
        */
        private BigDecimal toDeclarePrice;

        /**
        * 目的国申报币种
        */
        private String toCurrency;

        /**
        * skuId
        */
        private String skuId;

        /**
        * 目的国申报币种符号
        */
        private String toCurrencySymbol;

        /**
        * 申报重量（取sku毛重）
        */
        private BigDecimal weight;

        /**
        * 目的国海关编码
        */
        private String toCustomsCode;


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
        * 销售订单明细id
        */
        @NotBlank(message = "销售订单明细id不能为空")
        @Size(max = 19,message = "销售订单明细id最大长度不能超过19位")
        private String soDetailId;

        /**
        * 销售订单id
        */
        @NotBlank(message = "销售订单id不能为空")
        @Size(max = 19,message = "销售订单id最大长度不能超过19位")
        private String soId;

        /**
        * 申报数量
        */
        @NotNull(message = "申报数量不能为空")
        private Integer qty;

        /**
        * 中文报关名称
        */
        @NotBlank(message = "中文报关名称不能为空")
        @Size(max = 255,message = "中文报关名称最大长度不能超过255位")
        private String declareCn;

        /**
        * 英文报关名称
        */
        @NotBlank(message = "英文报关名称不能为空")
        @Size(max = 255,message = "英文报关名称最大长度不能超过255位")
        private String declareEn;

        /**
        * 目的国申报价
        */
        @NotNull(message = "目的国申报价不能为空")
        @Digits(integer = 12, fraction = 4, message = "目的国申报价整数位不能超过12位，小数位不能超过4位")
        private BigDecimal toDeclarePrice;

        /**
        * 目的国申报币种
        */
        @NotBlank(message = "目的国申报币种不能为空")
        @Size(max = 32,message = "目的国申报币种最大长度不能超过32位")
        private String toCurrency;

        /**
        * skuId
        */
        @NotBlank(message = "skuId不能为空")
        @Size(max = 1,message = "skuId最大长度不能超过1位")
        private String skuId;

        /**
        * 目的国申报币种符号
        */
        @NotBlank(message = "目的国申报币种符号不能为空")
        @Size(max = 32,message = "目的国申报币种符号最大长度不能超过32位")
        private String toCurrencySymbol;

        /**
        * 申报重量（取sku毛重）
        */
        @NotNull(message = "申报重量（取sku毛重）不能为空")
        @Digits(integer = 12, fraction = 4, message = "申报重量（取sku毛重）整数位不能超过12位，小数位不能超过4位")
        private BigDecimal weight;

        /**
        * 目的国海关编码
        */
        @NotBlank(message = "目的国海关编码不能为空")
        @Size(max = 64,message = "目的国海关编码最大长度不能超过64位")
        private String toCustomsCode;


    }


}