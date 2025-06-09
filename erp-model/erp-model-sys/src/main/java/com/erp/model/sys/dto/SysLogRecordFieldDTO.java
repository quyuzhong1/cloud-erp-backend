package com.erp.model.sys.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * SYS系统日志字段保存配置表请求响应实体
 * </p>
 *
 * @author Jim
 * @since 2023-08-29
 */
@Data
@NoArgsConstructor
public class SysLogRecordFieldDTO implements Serializable {


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
         * 字段类型 0字符串，1是或否，2枚举，3字典，4人员
         */
        private Integer type;

        /**
         * 枚举类型(用于枚举值转换,需要枚举整个路径)
         */
        private String enumClass;

        /**
         * true|false对应值,竖线分隔
         */
        private String booleanValue;

        /**
         * 创建人id
         */
        private String createUserId;

        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 修改人id
         */
        private String updateUserId;

        /**
         * 修改人名称
         */
        private String updateUserName;

        /**
         * 更新时间
         */
        private LocalDateTime updateTime;

        /**
         * 乐观锁版本号
         */
        private Integer version;

        /**
         * 逻辑删除字段
         */
        private Boolean isDeleted;


    }

    /**
     * 新增
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


    }

    /**
     * 修改
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
         * 主键id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;

    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListDTO {
        /**
         * 类路径数组
         */
        @NotNull(message = "classPathList不能为空")
        private List<String> classPathList;
    }


    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 字段
         */
        @NotBlank(message = "字段不能为空")
        @Size(max = 64, message = "字段最大长度不能超过64位")
        private String field;

        /**
         * 字段名称
         */
        @NotBlank(message = "字段名称不能为空")
        @Size(max = 64, message = "字段名称最大长度不能超过64位")
        private String fieldName;

        /**
         * 类路径
         */
        @NotBlank(message = "类路径不能为空")
        @Size(max = 64, message = "类路径最大长度不能超过64位")
        private String classPath;

    }


}