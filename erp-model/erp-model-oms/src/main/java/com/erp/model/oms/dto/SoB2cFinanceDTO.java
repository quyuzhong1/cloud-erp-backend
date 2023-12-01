package com.erp.model.oms.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * B2C销售订单财务信息表请求响应实体
 * </p>
 *
 * @author will
 * @since 2023-09-08
*/
@Data
@NoArgsConstructor
public class SoB2cFinanceDTO implements Serializable {




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
        * 币别
        */
        private String currency;

        /**
        * 运费收入
        */
        private BigDecimal shippingCost;

        /**
        * 商品成本
        */
        private BigDecimal itemCost;

        /**
        * 物流成本
        */
        private BigDecimal logisticsCost;

        /**
        * 平台费
        */
        private BigDecimal platformCost;

        /**
        * 转账费
        */
        private BigDecimal transferCost;

        /**
        * 包装辅料费
        */
        private BigDecimal accessoriesCost;

        /**
        * VAT税费
        */
        private BigDecimal vatCost;


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
        @Size(max = 19,message = "主表id最大长度不能超过19位")
        private String mainId;

        /**
        * 币别
        */
        @Size(max = 32,message = "币别最大长度不能超过32位")
        private String currency;

        /**
        * 运费收入
        */
        @Digits(integer = 12, fraction = 4, message = "运费收入整数位不能超过12位，小数位不能超过4位")
        private BigDecimal shippingCost;

        /**
        * 商品成本
        */
        @Digits(integer = 12, fraction = 4, message = "商品成本整数位不能超过12位，小数位不能超过4位")
        private BigDecimal itemCost;

        /**
        * 物流成本
        */
        @Digits(integer = 12, fraction = 4, message = "物流成本整数位不能超过12位，小数位不能超过4位")
        private BigDecimal logisticsCost;

        /**
        * 平台费
        */
        @Digits(integer = 12, fraction = 4, message = "平台费整数位不能超过12位，小数位不能超过4位")
        private BigDecimal platformCost;

        /**
        * 转账费
        */
        @Digits(integer = 12, fraction = 4, message = "转账费整数位不能超过12位，小数位不能超过4位")
        private BigDecimal transferCost;

        /**
        * 包装辅料费
        */
        @Digits(integer = 12, fraction = 4, message = "包装辅料费整数位不能超过12位，小数位不能超过4位")
        private BigDecimal accessoriesCost;

        /**
        * VAT税费
        */
        @Digits(integer = 12, fraction = 4, message = "VAT税费整数位不能超过12位，小数位不能超过4位")
        private BigDecimal vatCost;


        /**
         * 平台费率
         */
        @Digits(integer = 12, fraction = 4, message = "VAT税费整数位不能超过12位，小数位不能超过4位")
        private BigDecimal platformRate;

        /**
         * vat 费率
         */
        @Digits(integer = 12, fraction = 4, message = "VAT税费整数位不能超过12位，小数位不能超过4位")
        private BigDecimal vatRate;

        /**
         * 转账费率
         */
        @Digits(integer = 12, fraction = 4, message = "VAT税费整数位不能超过12位，小数位不能超过4位")
        private BigDecimal transferRate;


        /**
         * 平台费类型
         */
        private String platformCostType;

        /**
         * 转账费类型
         */
        private String transferCostType;

        /**
         * VAT税费类型
         */
        private String vatCostType;
    }


}