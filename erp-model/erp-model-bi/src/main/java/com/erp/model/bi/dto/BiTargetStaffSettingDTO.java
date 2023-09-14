package com.erp.model.bi.dto;

import java.math.BigDecimal;

import com.common.core.anno.StateEnumValue;
import com.erp.model.bi.enums.MetricsEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Digits;

/**
 * <p>
 * 人员目标设置表请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
 */
@Data
@NoArgsConstructor
public class BiTargetStaffSettingDTO implements Serializable {


    /**
     * 分页详情
     */
    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {

        private String id;

        /**
         * 年
         */
        private String year;

        /**
         * 部门id
         */
        private String deptId;

        /**
         * 详情信息
         */
        private List<CommonDTO> detailList;


    }


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO extends BiTargetYearDTO.ViewDTO {

        List<BiTargetStaffSettingDTO.DetailDTO> detailList;

    }


    /**
     *
     */
    @Data
    @NoArgsConstructor
    public static class DetailDTO {

        /**
         * 考核指标
         */
        private MetricsEnum metrics;


        /**
         * 考核指标名
         */
        private String metricsName;

        /**
         * 人员设置列表
         */
        private List<CommonDTO> staffSettingList;

    }

    /**
     * 新增
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends BiTargetYearDTO.AddDTO {
        @Size(min = 1, message = "目标设置不能为空")
        @NotNull(message = "目标设置不能为空")
        private List<CommonDTO> detailList;

    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends BiTargetYearDTO.UpdateDTO {

        @Size(min = 1, message = "目标设置不能为空")
        @NotNull(message = "目标设置不能为空")
        private List<CommonDTO> detailList;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO extends BiTargetYearDTO.MonthDTO {

        /**
         * 员工id
         */
        @NotBlank(message = "员工不能为空")
        private String staffId;

        private String staffName;

        /**
         * 月
         */
        @NotNull(message = "月不能为空")
        private Integer month;


        private BigDecimal value;

        /**
         * 指标维度
         */
        @NotBlank(message = "指标维度不能为空")
        @StateEnumValue(clazz = MetricsEnum.class, message = "指标维度有误")
        private MetricsEnum metrics;


    }


    @Data
    @NoArgsConstructor
    public static class ListDetailDTO {

        private String id;

        private String mainId;

        private String year;


        /**
         * 员工id
         */
        private String staffId;

        /**
         * 员工id
         */
        private String staffName;


        /**
         * 月
         */

        private Integer month;


        private BigDecimal value;


        private MetricsEnum metrics;


    }


}