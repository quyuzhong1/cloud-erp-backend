package com.erp.model.mrp.dto;

import com.erp.model.mrp.entity.CfgRuleSalesFormulaCalcEntity;
import com.erp.model.mrp.enums.CfgRuleSalesFormulaDefaultTypeEnum;
import com.erp.model.mrp.enums.CfgRuleSalesFormulaTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

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
         * 时间
         */
        private List<LocalDate> dateList;

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

        /**
         * 百分比json
         */
        private CfgRuleSalesFormulaDTO.PercentJsonDTO percentJsonDTO;

    }
    /**
     * 默认日销量DTO
     */
    @Data
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class DefaultUpdateDTO {

        /**
         * 销量默认类型：dynamic=动态、fixed=固定
         */
        @NotBlank(message = "销量默认类型：dynamic=动态、fixed=固定不能为空")
        @Size(max = 32, message = "销量默认类型：dynamic=动态、fixed=固定最大长度不能超过32位")
        private String defaultType;

        /**
         * 固定值
         */
        @Digits(integer = 12, fraction = 4, message = "固定值整数位不能超过12位，小数位不能超过4位")
        private Integer fixedValue;

        /**
         * 百分比json
         */
        private CfgRuleSalesFormulaDTO.PercentJsonDTO percentJsonDTO;
    }

    /**
     * 动态日销量DTO
     */
    @Data
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class DynamicUpdateDTO {

        /**
         * 名称
         */
        @NotBlank(message = "名称不能为空")
        @Size(max = 10, message = "名称最大长度不能超过10位")
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
        private CfgRuleSalesFormulaDTO.PercentJsonDTO percentJsonDTO;
    }

    /**
     * 固定日销量DTO
     */
    @Data
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class FixedUpdateDTO {

        /**
         * 名称
         */
        @NotBlank(message = "名称不能为空")
        @Size(max = 10, message = "名称最大长度不能超过10位")
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
        @Min(value = 0, message = "固定值最小值为0")
        @Max(value = 999999999, message = "固定值最大值为999999999")
        private Integer fixedValue;

    }

    /**
     * 导出详情
     */
    @Data
    @NoArgsConstructor
    public static class ExportDTO {

        /**
         * 销量类型：default=默认，dynamic=动态、fixed=固定
         */
        private String type;
        /**
         * 销量类型：default=默认，dynamic=动态、fixed=固定
         */
        private String typeName;

        /**
         * 销量默认类型：dynamic=动态、fixed=固定
         */
        private String defaultType;
        /**
         * 销量默认类型：dynamic=动态、fixed=固定
         */
        private String defaultTypeName;

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
         * 固定值
         */
        private Integer fixedValue;
        /**
         * 百分比json
         */
        private String percentJson;
        /**
         * 百分比json
         */
        private CfgRuleSalesFormulaDTO.PercentJsonDTO percentJsonDTO;

        public static ExportDTO buildExportDTO(CfgRuleSalesFormulaCalcEntity entity) {
            ExportDTO dto = new ExportDTO();
            dto.setType(entity.getType());
            dto.setTypeName(CfgRuleSalesFormulaTypeEnum.getName(entity.getType()));
            dto.setDefaultType(entity.getDefaultType());
            dto.setDefaultTypeName(CfgRuleSalesFormulaDefaultTypeEnum.getName(entity.getDefaultType()));
            dto.setName(entity.getName());
            dto.setStartDate(entity.getStartDate());
            dto.setEndDate(entity.getEndDate());
            dto.setFixedValue(entity.getFixedValue());
            dto.setPercentJson(dto.getPercentJson());
            dto.setPercentJsonDTO(dto.getPercentJsonDTO());
            return dto;
        }

    }

}