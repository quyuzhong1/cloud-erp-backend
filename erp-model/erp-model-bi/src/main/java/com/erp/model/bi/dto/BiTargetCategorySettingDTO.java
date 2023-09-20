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
 * 分类 目标设置表请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
*/
@Data
@NoArgsConstructor
public class BiTargetCategorySettingDTO implements Serializable {



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
        private List<CommonDTO> categorySettingList;

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
    public static class CommonDTO extends BiTargetYearDTO.MonthDTO{
        /**
         * 店铺id
         */
        @NotBlank(message = "分类名")
        private String categoryId;

        private String categoryName;


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


    @Data
    @NoArgsConstructor
    public static class ListDetailDTO {

        private String id;

        private String mainId;

        private String year;

        private String deptName;


        /**
         * 分类id
         */
        private String categoryId;

        /**
         * 分类名
         */
        private String categoryName;


        /**
         * 月
         */

        private Integer month;


        private BigDecimal value;


        private MetricsEnum metrics;


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
        private MetricsEnum metricsEnum;

        /**
         * 分类不
         */
        @NotBlank(message = "分类不能空")
        private String categoryId;

    }
}