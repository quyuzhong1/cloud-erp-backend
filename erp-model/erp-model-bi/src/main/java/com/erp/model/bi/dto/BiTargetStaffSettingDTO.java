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
         * 主表id 对应 target_year 表id
         */
        private String mainId;

        /**
         * 员工id
         */
        private String staffId;

        /**
         * 员工名
         */
        private String staffName;

        /**
         * 月
         */
        private Integer month;

        /**
         * 对应值
         */
        private BigDecimal value;

        /**
         * 指标维度
         */
        private String metrics;


    }

    /**
     * 新增
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends BiTargetYearDTO.AddDTO {
        @Size(min = 1,message = "目标设置不能为空")
        @NotNull(message = "目标设置不能为空")
        private List<CommonDTO> detailList;

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



        private String id;


        /**
         * 员工id
         */
        @NotBlank(message = "员工不能为空")
        private String staffId;


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
         * 月
         */
        @NotNull(message = "月不能为空")
        private Integer month;


        private BigDecimal value;


        private MetricsEnum metrics;


    }


}