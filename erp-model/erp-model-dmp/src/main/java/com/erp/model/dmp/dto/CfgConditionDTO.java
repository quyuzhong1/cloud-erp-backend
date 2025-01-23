package com.erp.model.dmp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 条件配置表请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2025-01-20
*/
@Data
@NoArgsConstructor
public class CfgConditionDTO implements Serializable {




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
        * 条件字段 对应dict_rule_condition key
        */
        private String conditionField;

        /**
        * 逻辑关系 对应 dict_rule_condition key 多个逗号分割
        */
        private String logic;

        /**
        * 空间 如时间戳 输入框之类
        */
        private String controls;

        /**
        * 对应api url
        */
        private String apiUrl;

        /**
        * 条件字段名
        */
        private String conditionFieldName;

        /**
        * 请求方式 GET POST 等
        */
        private String requestMethod;

        /**
        * 请求参数 JSON 格式
        */
        private String param;

        /**
        * 对应下拉的绑定的字段
        */
        private String label;

        /**
        * 对应下拉的显示中文的名 的字段
        */
        private String value;

        /**
        * 排序
        */
        private Integer index;

        /**
        * 前端要求 用来做输入值的传入
        */
        private String searchKey;

        /**
        * 前端要求用来做回显
        */
        private String remoteLabel;

        /**
        * 值类型
        */
        private String valueType;

        /**
        * 规则备注（removeSpace 去空格）
        */
        private String remark;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


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

        /**
        * 条件字段 对应dict_rule_condition key
        */
        @NotBlank(message = "条件字段 对应dict_rule_condition key不能为空")
        @Size(max = 30,message = "条件字段 对应dict_rule_condition key最大长度不能超过30位")
        private String conditionField;

        /**
        * 逻辑关系 对应 dict_rule_condition key 多个逗号分割
        */
        @NotBlank(message = "逻辑关系 对应 dict_rule_condition key 多个逗号分割不能为空")
        @Size(max = 100,message = "逻辑关系 对应 dict_rule_condition key 多个逗号分割最大长度不能超过100位")
        private String logic;

        /**
        * 空间 如时间戳 输入框之类
        */
        @NotBlank(message = "空间 如时间戳 输入框之类不能为空")
        @Size(max = 50,message = "空间 如时间戳 输入框之类最大长度不能超过50位")
        private String controls;

        /**
        * 对应api url
        */
        @NotBlank(message = "对应api url不能为空")
        @Size(max = 255,message = "对应api url最大长度不能超过255位")
        private String apiUrl;

        /**
        * 条件字段名
        */
        @NotBlank(message = "条件字段名不能为空")
        @Size(max = 30,message = "条件字段名最大长度不能超过30位")
        private String conditionFieldName;

        /**
        * 请求方式 GET POST 等
        */
        @NotBlank(message = "请求方式 GET POST 等不能为空")
        @Size(max = 10,message = "请求方式 GET POST 等最大长度不能超过10位")
        private String requestMethod;

        /**
        * 请求参数 JSON 格式
        */
        @NotBlank(message = "请求参数 JSON 格式不能为空")
        @Size(max = 255,message = "请求参数 JSON 格式最大长度不能超过255位")
        private String param;

        /**
        * 对应下拉的绑定的字段
        */
        @NotBlank(message = "对应下拉的绑定的字段不能为空")
        @Size(max = 30,message = "对应下拉的绑定的字段最大长度不能超过30位")
        private String label;

        /**
        * 对应下拉的显示中文的名 的字段
        */
        @NotBlank(message = "对应下拉的显示中文的名 的字段不能为空")
        @Size(max = 30,message = "对应下拉的显示中文的名 的字段最大长度不能超过30位")
        private String value;

        /**
        * 排序
        */
        @NotNull(message = "排序不能为空")
        private Integer index;

        /**
        * 前端要求 用来做输入值的传入
        */
        @NotBlank(message = "前端要求 用来做输入值的传入不能为空")
        @Size(max = 30,message = "前端要求 用来做输入值的传入最大长度不能超过30位")
        private String searchKey;

        /**
        * 前端要求用来做回显
        */
        @NotBlank(message = "前端要求用来做回显不能为空")
        @Size(max = 30,message = "前端要求用来做回显最大长度不能超过30位")
        private String remoteLabel;

        /**
        * 值类型
        */
        @NotBlank(message = "值类型不能为空")
        @Size(max = 30,message = "值类型最大长度不能超过30位")
        private String valueType;

        /**
        * 规则备注（removeSpace 去空格）
        */
        @NotBlank(message = "规则备注（removeSpace 去空格）不能为空")
        @Size(max = 255,message = "规则备注（removeSpace 去空格）最大长度不能超过255位")
        private String remark;


    }


}