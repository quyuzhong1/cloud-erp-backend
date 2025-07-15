package com.erp.model.workflow.dto;

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
 * 日志字段配置表请求响应实体
 * </p>
 *
 * @author will
 * @since 2025-05-12
*/
@Data
@NoArgsConstructor
public class CfgOperateLogFieldDTO implements Serializable {




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
        * 字段
        */
        private String field;

        /**
        * 字段名称
        */
        private String fieldName;

        /**
        * 类路径
        */
        private String classPath;

        /**
        * 字段类型 0字符串，1是或否，2枚举， 3字典
        */
        private Integer type;

        /**
        * 枚举类型
        */
        private String enumClass;

        /**
        * true|false对应值,竖线分隔
        */
        private String booleanValue;

        /**
        * 当类型3时存储字典类型
        */
        private String value;


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
        * 字段
        */
        @NotBlank(message = "字段不能为空")
        @Size(max = 64,message = "字段最大长度不能超过64位")
        private String field;

        /**
        * 字段名称
        */
        @NotBlank(message = "字段名称不能为空")
        @Size(max = 64,message = "字段名称最大长度不能超过64位")
        private String fieldName;

        /**
        * 类路径
        */
        @NotBlank(message = "类路径不能为空")
        @Size(max = 80,message = "类路径最大长度不能超过80位")
        private String classPath;

        /**
        * 字段类型 0字符串，1是或否，2枚举， 3字典
        */
        @NotNull(message = "字段类型 0字符串，1是或否，2枚举， 3字典不能为空")
        private Integer type;

        /**
        * 枚举类型
        */
        @NotBlank(message = "枚举类型不能为空")
        @Size(max = 80,message = "枚举类型最大长度不能超过80位")
        private String enumClass;

        /**
        * true|false对应值,竖线分隔
        */
        @NotBlank(message = "true|false对应值,竖线分隔不能为空")
        @Size(max = 50,message = "true|false对应值,竖线分隔最大长度不能超过50位")
        private String booleanValue;

        /**
        * 当类型3时存储字典类型
        */
        @NotBlank(message = "当类型3时存储字典类型不能为空")
        @Size(max = 50,message = "当类型3时存储字典类型最大长度不能超过50位")
        private String value;


    }


}