package com.erp.model.bi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
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
        @NotBlank(message = "主键id不能为空")
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

    }


}