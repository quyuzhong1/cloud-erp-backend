package com.erp.model.mrp.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
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
 * 销量公式（规则设置）请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-08-23
*/
@Data
@NoArgsConstructor
public class CfgRuleSalesFormulaDTO implements Serializable {




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
        * 销量类型：default=默认，dynamic=动态、fixed=固定
        */
        private String type;

        /**
        * 销量默认类型：dynamic=动态、fixed=固定
        */
        private String defaultType;

        /**
        * 排序字段
        */
        private Integer index;

        /**
        * 优先级字段
        */
        private Integer priority;

        /**
        * 名称
        */
        private String name;

        /**
        * 开始日期
        */
        private LocalDate startDate;

        /**
        * 结束日期
        */
        private LocalDate endDate;

        /**
        * 销量id(cfg_rule_sales_qty)
        */
        private String salesQtyId;

        /**
        * 固定值
        */
        private BigDecimal fixedValue;

        /**
        * 百分比json
        */
        private String percentJson;


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
        * 销量类型：default=默认，dynamic=动态、fixed=固定
        */
        @NotBlank(message = "销量类型：default=默认，dynamic=动态、fixed=固定不能为空")
        @Size(max = 32,message = "销量类型：default=默认，dynamic=动态、fixed=固定最大长度不能超过32位")
        private String type;

        /**
        * 销量默认类型：dynamic=动态、fixed=固定
        */
        @NotBlank(message = "销量默认类型：dynamic=动态、fixed=固定不能为空")
        @Size(max = 32,message = "销量默认类型：dynamic=动态、fixed=固定最大长度不能超过32位")
        private String defaultType;

        /**
        * 排序字段
        */
        @NotNull(message = "排序字段不能为空")
        private Integer index;

        /**
        * 优先级字段
        */
        @NotNull(message = "优先级字段不能为空")
        private Integer priority;

        /**
        * 名称
        */
        @NotBlank(message = "名称不能为空")
        @Size(max = 64,message = "名称最大长度不能超过64位")
        private String name;

        /**
        * 开始日期
        */
        private LocalDate startDate;

        /**
        * 结束日期
        */
        private LocalDate endDate;

        /**
        * 销量id(cfg_rule_sales_qty)
        */
        @NotBlank(message = "销量id(cfg_rule_sales_qty)不能为空")
        @Size(max = 255,message = "销量id(cfg_rule_sales_qty)最大长度不能超过255位")
        private String salesQtyId;

        /**
        * 固定值
        */
        @NotNull(message = "固定值不能为空")
        @Digits(integer = 12, fraction = 4, message = "固定值整数位不能超过12位，小数位不能超过4位")
        private BigDecimal fixedValue;

        /**
        * 百分比json
        */
        @NotBlank(message = "百分比json不能为空")
        private String percentJson;


    }


}