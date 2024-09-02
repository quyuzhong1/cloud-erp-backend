package com.erp.model.mrp.dto;

import cn.hutool.core.util.ObjectUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
         * 时间
         */
        private List<LocalDate> dateList;

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

        /**
         * 百分比json
         */
        private PercentJsonDTO percentJsonDTO;
    }
    /**
     * 默认日销量DTO
     */
    @Data
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class  DefaultUpdateDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 销量默认类型：dynamic=动态、fixed=固定
         */
        @NotBlank(message = "销量默认类型：dynamic=动态、fixed=固定不能为空")
        @Size(max = 32,message = "销量默认类型：dynamic=动态、fixed=固定最大长度不能超过32位")
        private String defaultType;

        /**
         * 固定值
         */
        @Digits(integer = 12, fraction = 4, message = "固定值整数位不能超过12位，小数位不能超过4位")
        private BigDecimal fixedValue;

        /**
         * 百分比json
         */
        private PercentJsonDTO percentJsonDTO;
    }

    /**
     * 动态日销量DTO
     */
    @Data
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class DynamicUpdateDTO {

        /**
         * 主键id
         */
        private String id;
        /**
         * 名称
         */
        @NotBlank(message = "名称不能为空")
        @Size(max = 10,message = "名称最大长度不能超过10位")
        private String name;

        /**
         * 时间段
         */
        @NotEmpty(message = "时间段不能为空")
        private List<LocalDate> dateList;

        /**
         * 百分比json
         */
        @NotNull(message = "动态日销量占比不能为空")
        private PercentJsonDTO percentJsonDTO;
    }

    /**
     * 固定日销量DTO
     */
    @Data
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class FixedUpdateDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 名称
         */
        @NotBlank(message = "名称不能为空")
        @Size(max = 10,message = "名称最大长度不能超过10位")
        private String name;

        /**
         * 时间段
         */
        @NotEmpty(message = "时间段不能为空")
        private List<LocalDate> dateList;

        /**
         * 固定值
         */
        @NotNull(message = "固定值不能为空")
        @Digits(integer = 12, fraction = 4, message = "固定值整数位不能超过12位，小数位不能超过4位")
        private BigDecimal fixedValue;

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
    @Accessors(chain = true)
    public static class CommonDTO {

        /**
        * 销量类型：default=默认，dynamic=动态、fixed=固定
        */
        @Size(max = 32,message = "销量类型：default=默认，dynamic=动态、fixed=固定最大长度不能超过32位")
        private String type;

        /**
        * 销量默认类型：dynamic=动态、fixed=固定
        */
        @Size(max = 32,message = "销量默认类型：dynamic=动态、fixed=固定最大长度不能超过32位")
        private String defaultType;

        /**
        * 优先级字段
        */
        private Integer priority;

        /**
        * 名称
        */
        @Size(max = 10,message = "名称最大长度不能超过10位")
        private String name;

        /**
         * 时间段
         */
        private List<LocalDate> dateList;

        /**
        * 固定值
        */
        @Digits(integer = 12, fraction = 4, message = "固定值整数位不能超过12位，小数位不能超过4位")
        private BigDecimal fixedValue;

        /**
        * 百分比json
        */
        private PercentJsonDTO percentJsonDTO;

    }

    /**
     * 百分比JSON
     */
    @Data
    @NoArgsConstructor
    public static class PercentJsonDTO {
        /**
         * 三天日均
         */
        private BigDecimal threeDaysRatio;
        /**
         * 七天日均
         */
        private BigDecimal sevenDaysRatio;
        /**
         * 十四天日均
         */
        private BigDecimal fourteenDaysRatio;
        /**
         * 三十天日均
         */
        private BigDecimal thirtyDaysRatio;
        /**
         * 六十天日均
         */
        private BigDecimal sixtyDaysRatio;
        /**
         * 九十天日均
         */
        private BigDecimal ninetyDaysRatio;
        /**
         * 一百八十天日均
         */
        private BigDecimal oneHandredEightyDaysRatio;
        /**
         * 二百七十天日均
         */
        private BigDecimal twoHandredSeventyDaysRatio;
        /**
         * 三百六十天日均
         */
        private BigDecimal threeHandredSixtyDaysRatio;

        /**
         * 总百分比
         */
        public BigDecimal getTotalRatio () {
            BigDecimal totalRatio = ObjectUtil.isEmpty(threeDaysRatio) ? BigDecimal.ZERO : threeDaysRatio
                    .add(ObjectUtil.isEmpty(sevenDaysRatio) ? BigDecimal.ZERO : sevenDaysRatio)
                    .add(ObjectUtil.isEmpty(fourteenDaysRatio) ? BigDecimal.ZERO : fourteenDaysRatio)
                    .add(ObjectUtil.isEmpty(thirtyDaysRatio) ? BigDecimal.ZERO : thirtyDaysRatio)
                    .add(ObjectUtil.isEmpty(sixtyDaysRatio) ? BigDecimal.ZERO : sixtyDaysRatio)
                    .add(ObjectUtil.isEmpty(ninetyDaysRatio) ? BigDecimal.ZERO : ninetyDaysRatio)
                    .add(ObjectUtil.isEmpty(oneHandredEightyDaysRatio) ? BigDecimal.ZERO : oneHandredEightyDaysRatio)
                    .add(ObjectUtil.isEmpty(twoHandredSeventyDaysRatio) ? BigDecimal.ZERO : twoHandredSeventyDaysRatio)
                    .add(ObjectUtil.isEmpty(threeHandredSixtyDaysRatio) ? BigDecimal.ZERO : threeHandredSixtyDaysRatio);
            return totalRatio;
        }
    }

}