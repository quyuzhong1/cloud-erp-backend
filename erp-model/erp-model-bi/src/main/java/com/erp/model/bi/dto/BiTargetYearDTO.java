package com.erp.model.bi.dto;

import com.common.business.dto.base.SortDTO;
import com.common.core.anno.StateEnumValue;
import com.erp.model.bi.enums.MetricsEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
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
     * 分页参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {

        private String name;

        /**
         * 年
         */
        private Integer year;

        /**
         * 考核指标
         */
        @NotBlank(message = "考核指标不能为空")
        private String metrics;

        /**
         * 部门id
         */
        private String deptId;


    }

    @Data
    @NoArgsConstructor
    public static class PagingTotalDTO {

        /**
         * 一月值
         */
        private BigDecimal januaryTotal;

        /**
         * 二月值
         */
        private BigDecimal februaryTotal;

        /**
         * 三月
         */
        private BigDecimal marchTotal;


        /**
         * 四月值
         */
        private BigDecimal aprilTotal;

        /**
         * 五月值
         */
        private BigDecimal mayTotal;


        /**
         * 六月值
         */
        private BigDecimal juneTotal;


        /**
         * 七月值
         */
        private BigDecimal julyTotal;


        /**
         * 八月值
         */
        private BigDecimal augustTotal;


        /**
         * 九月值
         */
        private BigDecimal septemberTotal;


        /**
         * 十月值
         */
        private BigDecimal octoberTotal;

        /**
         * 十一月值
         */
        private BigDecimal novemberTotal;

        /**
         * 十二月值
         */
        private BigDecimal decemberTotal;

    }

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
     * 月值
     */
    @Data
    @NoArgsConstructor
    public static class MonthValueDTO {

        private Integer month;

        private BigDecimal value;

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
        @NotNull(message = "指标维度不能为空")
        @StateEnumValue(clazz = MetricsEnum.class, message = "指标维度有误")
        private MetricsEnum metrics;

        private String metricsName;


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
        @NotNull(message = "考核年度不能为空")
        private Integer year;

        /**
         *来源 http://172.16.100.11:3002/project/74/interface/api/23668
         */
        @NotBlank(message = "货币不能为空")
        private String currency;


        /**
         * 部门id
         */
        @NotBlank(message = "部门不能为空")
        private String deptId;

        @NotNull(message = "考核指标不能为空")
        @Size(min = 1, message = "考核指标不能为空")
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
        @NotNull(message = "考核年度不能为空")
        private Integer year;

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

        @Size(min = 1, message = "考核指标不能为空")
        private List<String> metricsList;
    }


    /**
     * 目标完成情况
     */
    @Data
    @NoArgsConstructor
    public static class TargetMetricsFinishDTO {

        /**
         * 指标维度
         */
        private String metrics;

        /**
         * 指标名
         */
        private String metricsName;

        /**
         * 目标值
         */
        private Object metricsValue;

        /**
         * 完成值
         */
        private Object finishValue;

        /**
         * 完成率
         */
        private BigDecimal finishRate;

    }


    /**
     * 年月目标设置值
     */
    @Data
    @NoArgsConstructor
    public static class YearMonthValueDTO {

        private Integer year;

        private Integer month;

        /**
         * 指标维度
         */
        private String metrics;

        /**
         * 指标名
         */
        private String metricsName;

        /**
         * 目标值
         */
        private BigDecimal metricsValue;


    }


    /**
     * 销售模板的参数
     */
    @Data
    @NoArgsConstructor
    public static class SearchDTO extends BiFilterDTO {

        /**
         * 日期年月
         */
        private String yearMonth;

        /**
         * 指标
         */
        @NotNull(message = "指标维度不能为空")
        @StateEnumValue(clazz = MetricsEnum.class, message = "指标维度有误")
        private MetricsEnum metrics;
    }
}