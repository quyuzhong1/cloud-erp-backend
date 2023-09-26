package com.erp.model.bi.dto;

import java.math.BigDecimal;

import com.common.core.anno.StateEnumValue;
import com.erp.model.bi.enums.MetricsEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.checkerframework.checker.units.qual.C;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.*;

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
         * 部门id
         */
        private String deptName;

        /**
         * 创建人
         */
        private String createUserName;

        /**
         * 创建时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;


        private String staffName;

        private String staffId;


        /**
         * 指标维度
         */
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
        private List<CommonDTO> settingList;

    }


    /**
     * 新增
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends BiTargetYearDTO.AddDTO {

        @Size(min = 1, message = "目标设置不能为空")
        @NotNull(message = "目标设置不能为空")
        @Valid
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
        @Valid
        private List<CommonDTO> detailList;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO extends BiTargetYearDTO.MonthDTO {

        /**
         * 员工id 来源 http://172.16.100.11:3002/project/36/interface/api/158
         */
        @NotBlank(message = "员工不能为空")
        private String staffId;

        private String staffName;

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


    /**
     * 导入质
     */
    @Data
    @NoArgsConstructor
    public static class ImportDTO {

        /**
         * o      * 成功返回数据
         */
        private List<DetailDTO> successList;

        /**
         * 错误的url
         */
        private String errorUrl;
    }


    @Data
    @NoArgsConstructor
    public static class RemoveDTO {

        /**
         * id
         */
        @NotBlank(message = "目标不能为空")
        private String id;

        /**
         * 指标
         */
        @NotNull(message = "指标不能为空")
        private MetricsEnum metrics;

        /**
         * 员工id
         */
        @NotBlank(message = "人员不能为空")
        private String staffId;

    }

}