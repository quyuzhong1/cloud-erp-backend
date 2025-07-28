package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 模板字段表请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-07-24
*/
@Data
@NoArgsConstructor
public class CfgTemplateVariablesDTO implements Serializable {




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
        * 模板类型
        */
        private String templateType;

        /**
        * 分类
        */
        private String type;

        /**
        * 数据库表名
        */
        private String tableName;

        /**
        * 字段名称
        */
        private String name;

        /**
        * 字段
        */
        private String field;

        /**
        * 字段类型
        */
        private String fieldType;

        /**
        * 默认值
        */
        private String defaultValue;

        /**
        * 禁用状态(false:启用,true:禁用)
        */
        private Boolean disabled;

        /**
        * 序号
        */
        private Integer index;


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
        * 模板类型
        */
        @NotBlank(message = "模板类型不能为空")
        @Size(max = 32,message = "模板类型最大长度不能超过32位")
        private String templateType;

        /**
        * 分类
        */
        @NotBlank(message = "分类不能为空")
        @Size(max = 32,message = "分类最大长度不能超过32位")
        private String type;

        /**
        * 数据库表名
        */
        @NotBlank(message = "数据库表名不能为空")
        @Size(max = 32,message = "数据库表名最大长度不能超过32位")
        private String tableName;

        /**
        * 字段名称
        */
        @NotBlank(message = "字段名称不能为空")
        @Size(max = 64,message = "字段名称最大长度不能超过64位")
        private String name;

        /**
        * 字段
        */
        @NotBlank(message = "字段不能为空")
        @Size(max = 64,message = "字段最大长度不能超过64位")
        private String field;

        /**
        * 字段类型
        */
        @NotBlank(message = "字段类型不能为空")
        @Size(max = 32,message = "字段类型最大长度不能超过32位")
        private String fieldType;

        /**
        * 默认值
        */
        @NotBlank(message = "默认值不能为空")
        @Size(max = 32,message = "默认值最大长度不能超过32位")
        private String defaultValue;

        /**
        * 禁用状态(false:启用,true:禁用)
        */
        @NotNull(message = "禁用状态(false:启用,true:禁用)不能为空")
        private Boolean disabled;

        /**
        * 序号
        */
        @NotNull(message = "序号不能为空")
        private Integer index;


    }


    /**
     * 模板字段
     */
    @Data
    @NoArgsConstructor
    public static class VariableGroupDTO {
        /**
         * 主键id
         */
        private String  id;

        /**
         * 模板类型
         */
        private String templateType;

        /**
         * 分类
         */
        private String type;

        /**
         * 分类名称
         */
        private String typeName;
        /**
         * 是否表格扩展
         */
        private Boolean tableExtensions;
        /**
         * 是否默认展开
         */
        private Boolean defaultExpand;
        /**
         * 字段集合
         */
        private List<VariableDTO> variables;

        /**
         * 序号
         */
        private Integer index;
    }


    /**
     * 模板字段
     */
    @Data
    @NoArgsConstructor
    public static class VariableDTO {

        /**
         * 主键id
         */
        private String  id;


        /**
         * 父id
         */
        private String  parentId;


        /**
         * 字段名称
         */
        private String name;

        /**
         * 字段
         */
        private String field;

        /**
         * 字段类型
         */
        private String fieldType;

        /**
         * 默认值
         */
        private String defaultValue;

        /**
         * 序号
         */
        private Integer index;

    }


    /**
     * 模板字段查询参数
     */
    @Data
    @NoArgsConstructor
    public static class TemplateParamDTO {

        /**
         * 模板类型
         */
        @NotBlank(message = "模板类型不能为空")
        private String templateType;

        /**
         * 分类
         */
        private String type;

    }

}