package com.erp.model.mrp.dto;

import java.time.LocalDate;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 试算销量公式（规则设置）请求响应实体
 * </p>
 *
 * @author liaohui
 * @since 2024-11-11
*/
@Data
@NoArgsConstructor
public class CfgRuleSalesFormulaCalcDTO implements Serializable {




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
        * 试算配置id
        */
        private String cfgRuleCalcId;

        /**
        * 固定值
        */
        private Integer fixedValue;

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
        * 试算配置id
        */
        @NotBlank(message = "试算配置id不能为空")
        @Size(max = 255,message = "试算配置id最大长度不能超过255位")
        private String cfgRuleCalcId;

        /**
        * 固定值
        */
        @NotNull(message = "固定值不能为空")
        private Integer fixedValue;

        /**
        * 百分比json
        */
        @NotBlank(message = "百分比json不能为空")
        private String percentJson;


    }


}