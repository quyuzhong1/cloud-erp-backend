package com.erp.model.dmp.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
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
 * sku自定义成本表请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-01-04
*/
@Data
@NoArgsConstructor
public class DmpSkuCostCustomDTO implements Serializable {




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
        * sku编号
        */
        private String skuNo;

        /**
        * 成本日期
        */
        private LocalDate costDate;

        /**
        * 备注
        */
        private String remark;

        /**
        * 有效状态
        */
        private Boolean status;

        /**
        * 成本价格
        */
        private BigDecimal costPrice;

        /**
        * 最近的采购日期3个月前的日期
        */
        private LocalDate threeMonthsAgoDate;

        /**
        * 最近的采购日期
        */
        private LocalDate latestPurchaseDate;

        /**
        * skuId
        */
        private String skuId;

        /**
        * 不含税成本价格
        */
        private BigDecimal notTaxCostPrice;

        /**
        * 币别
        */
        private String currency;


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
        * 成本日期
        */
        private LocalDate costDate;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;

        /**
        * 有效状态
        */
        @NotNull(message = "有效状态不能为空")
        private Boolean status;

        /**
        * 成本价格
        */
        @NotNull(message = "成本价格不能为空")
        @Digits(integer = 12, fraction = 4, message = "成本价格整数位不能超过12位，小数位不能超过4位")
        private BigDecimal costPrice;

        /**
        * 最近的采购日期3个月前的日期
        */
        private LocalDate threeMonthsAgoDate;

        /**
        * 最近的采购日期
        */
        private LocalDate latestPurchaseDate;

        /**
        * skuId
        */
        @NotBlank(message = "skuId不能为空")
        @Size(max = 19,message = "skuId最大长度不能超过19位")
        private String skuId;

        /**
        * 不含税成本价格
        */
        @NotNull(message = "不含税成本价格不能为空")
        @Digits(integer = 12, fraction = 4, message = "不含税成本价格整数位不能超过12位，小数位不能超过4位")
        private BigDecimal notTaxCostPrice;

        /**
        * 币别
        */
        @NotBlank(message = "币别不能为空")
        @Size(max = 32,message = "币别最大长度不能超过32位")
        private String currency;


    }


}