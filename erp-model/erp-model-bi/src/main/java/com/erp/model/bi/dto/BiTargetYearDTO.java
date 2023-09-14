package com.erp.model.bi.dto;

import com.common.core.anno.StateEnumValue;
import com.erp.model.bi.enums.MetricsEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 年度目标表请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
 */
@Data
@NoArgsConstructor
public class BiTargetYearDTO implements Serializable {


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 年
         */
        private String year;

        private List<String> metricsList;

        /**
         * 货币
         */
        private String currency;

        /**
         * 货币符号
         */
        private String currencySymbol;

        /**
         * 部门id
         */
        private String deptId;

        /**
         * 部门名称
         */
        private String deptName;


    }

    /**
     * 月
     */
    @Data
    @NoArgsConstructor
    public static class MonthDTO {


        /**
         * 指标维度
         */
        @NotBlank(message = "指标维度不能为空")
        @StateEnumValue(clazz = MetricsEnum.class, message = "指标维度有误")
        private MetricsEnum metrics;


        /**
         * 一月值
         */
        private BigDecimal january;

        /**
         * 二月值
         */
        private BigDecimal february;

        /**
         * 三月
         */
        private BigDecimal march;


        /**
         * 四月值
         */
        private BigDecimal april;

        /**
         * 五月值
         */
        private BigDecimal may;


        /**
         * 六月值
         */
        private BigDecimal june;


        /**
         * 七月值
         */
        private BigDecimal july;


        /**
         * 八月值
         */
        private BigDecimal august;


        /**
         * 九月值
         */
        private BigDecimal september;


        /**
         * 十月值
         */
        private BigDecimal october;

        /**
         * 十一月值
         */
        private BigDecimal november;

        /**
         * 十二月值
         */
        private BigDecimal december;


    }

    /**
     * 新增
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO {
        /**
         * 年
         */
        @NotBlank(message = "考核年度不能为空")
        @Size(max = 10, message = "年最大长度不能超过10位")
        private String year;

        /**
         *
         */
        @NotBlank(message = "货币不能为空")
        private String currency;


        /**
         * 部门id
         */
        @NotBlank(message = "部门不能为空")
        private String deptId;

        @NotNull(message = "考核指标不能为空")
        @Size(min = 1,message = "考核指标不能为空")
        private List<String> metricsList;

    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO {

        /**
         * 主键id
         */
        @NotBlank(message = "目标不能为空")
        private String id;


        /**
         * 年
         */
        @NotBlank(message = "考核年度不能为空")
        @Size(max = 10, message = "年最大长度不能超过10位")
        private String year;

        /**
         *
         */
        @NotBlank(message = "货币不能为空")
        private String currency;


        /**
         * 部门id
         */
        @NotBlank(message = "部门不能为空")
        private String deptId;

        @Size(min = 1,message = "考核指标不能为空")
        private List<String> metricsList;
    }


}