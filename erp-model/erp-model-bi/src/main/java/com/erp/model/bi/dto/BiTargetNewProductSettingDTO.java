package com.erp.model.bi.dto;

import java.math.BigDecimal;

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
 * 新品目标设置表请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
*/
@Data
@NoArgsConstructor
public class BiTargetNewProductSettingDTO implements Serializable {





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
         * 详情信息
         */
        private List<CommonDTO> detailList;


    }




    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO extends BiTargetYearDTO.ViewDTO{
        List<DetailDTO> detailList;
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
        private List<CommonDTO> newProductSettingList;

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
         * 一月占比
         */
        private BigDecimal januaryRate;

        /**
         * 二月占比
         */
        private BigDecimal februaryRate;

        /**
         * 三月占比
         */
        private BigDecimal marchRate;


        /**
         * 四月占比
         */
        private BigDecimal aprilRate;

        /**
         * 五月占比
         */
        private BigDecimal mayRate;


        /**
         * 六月占比
         */
        private BigDecimal juneRate;


        /**
         * 七月占比
         */
        private BigDecimal julyRate;


        /**
         * 八月占比
         */
        private BigDecimal augustRate;


        /**
         * 九月占比
         */
        private BigDecimal septemberRate;


        /**
         * 十月占比
         */
        private BigDecimal octoberRate;

        /**
         * 十一月占比
         */
        private BigDecimal novemberRate;

        /**
         * 十二月占比
         */
        private BigDecimal decemberRate;


    }


    /**
     * 导入质
     */
    @Data
    @NoArgsConstructor
    public static class ImportDTO {

        /**
         o      * 成功返回数据
         */
        private List<DetailDTO> successList;

        /**
         * 错误的url
         */
        private String errorUrl;
    }

    /**
     * 部门目标
     */
    @Data
    @NoArgsConstructor
    public static class DeptTargetDTO {
        /**
         * 部门id
         */
        private String deptId;
        /**
         * 部门名称
         */
        private String deptName;
        /**
         * 月
         */
        private Integer month;
        /**
         * 目标值
         */
        private BigDecimal value;
        /**
         * 维度
         */
        private String metrics;
    }

    /**
     * 部门目标
     */
    @Data
    @NoArgsConstructor
    public static class UserTargetDTO {
        /**
         * 部门id
         */
        private String userId;
        /**
         * 部门名称
         */
        private String userName;
        /**
         * 月
         */
        private Integer month;
        /**
         * 目标值
         */
        private BigDecimal value;
        /**
         * 维度
         */
        private String metrics;
    }

    /**
     * 目标查询参数
     */
    @Data
    @NoArgsConstructor
    public static class TargetParamDTO {
        /**
         * 年
         */
        private String year;
        /**
         * 月
         */
        private Integer month;
        /**
         * 维度
         */
        private List<String> metricsList;
        /**
         * 部门id
         */
        private List<String> deptIdList;
        /**
         * 用户id
         */
        private List<String> userIdList;
    }

}