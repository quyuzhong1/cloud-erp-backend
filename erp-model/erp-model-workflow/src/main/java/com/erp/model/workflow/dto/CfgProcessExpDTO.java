package com.erp.model.workflow.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * <p>
 * 流程设置审核条件请求响应实体
 * </p>
 *
 * @author hcg
 * @since 2025-05-12
*/
@Data
@NoArgsConstructor
public class CfgProcessExpDTO implements Serializable {




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
        * 流程设置ID
        */
        private String ruleId;

        /**
        * 条件包含-左括号
        */
        private String leftBracket;

        /**
        * 选择条件字段
        */
        private String field;

        /**
         * 条件字段名
         */
        private String fieldName;

        /**
        * 条件符号
        */
        private String compare;

        /**
        * 条件值
        */
        private String value;

        /**
        * 条件包含-右括号
        */
        private String rightBracket;

        /**
        * 多条件逻辑关系
        */
        private String logic;

        /**
        * 序号
        */
        private String index;

        /**
        * 值对应名称
        */
        private String name;

        /**
         * 值类型
         */
        private String valueType;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddOrUpdateDTO extends CommonDTO {

        /**
         * 主键id
         */
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 流程设置ID
        */
        @Size(max = 100,message = "流程设置ID最大长度不能超过100位")
        private String ruleId;

        /**
        * 条件包含-左括号
        */
        @Size(max = 100,message = "条件包含最大长度不能超过100位")
        private String leftBracket;

        /**
        * 选择条件字段
        */
        @Size(max = 30,message = "选择条件字段最大长度不能超过30位")
        private String field;

        /**
         * 条件字段名
         */
        @Size(max = 30,message = "选择条件字段名最大长度不能超过30位")
        private String fieldName;

        /**
        * 条件符号
        */
        @Size(max = 100,message = "条件符号最大长度不能超过100位")
        private String compare;

        /**
        * 条件值
        */
        @Size(max = 255,message = "条件值最大长度不能超过100位")
        private String value;

        /**
        * 条件包含-右括号
        */
        @Size(max = 100,message = "条件值最大长度不能超过30位")
        private String rightBracket;

        /**
        * 多条件逻辑关系
        */
        @NotNull(message = "多条件逻辑关系不能为空")
        private String logic;

        /**
        * 序号
        */
        @NotBlank(message = "序号不能为空")
        @Size(max = 100,message = "序号最大长度不能超过100位")
        private String index;

        /**
        * 值对应名称
        */
        @Size(max = 255,message = "值对应名称最大长度不能超过100位")
        private String name;

        /**
         * 值类型
         */
        @NotBlank(message = "值类型不能为空")
        private String valueType;
    }



}