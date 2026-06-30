package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 归档配置表请求响应实体
 * </p>
 */
@Data
@NoArgsConstructor
public class CfgTableArchiveDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class SearchParamDTO {

        /**
         * 业务名称
         */
        private String bizName;

        /**
         * 系统代码
         */
        private String systemCode;

        /**
         * 表名
         */
        private String tableName;
    }

    @Data
    @NoArgsConstructor
    public static class ListDTO {

        private String id;

        /**
         * 业务名称
         */
        private String bizName;

        /**
         * 系统代码
         */
        private String systemCode;

        /**
         * 表名
         */
        private String tableName;

        /**
         * 时间字段
         */
        private String timeField;

        /**
         * 保留天数
         */
        private Integer retentionDay;

        /**
         * 一次限制条数
         */
        private Integer limitCount;

        /**
         * 扩展sql语句
         */
        private String extSql;

        private String createUserName;

        private LocalDateTime createTime;

        private String updateUserName;

        private LocalDateTime updateTime;
    }

    @Data
    @NoArgsConstructor
    public static class AddDTO {

        @NotBlank(message = "业务名称不能为空")
        private String bizName;

        @NotBlank(message = "系统代码不能为空")
        private String systemCode;

        @NotBlank(message = "表名不能为空")
        private String tableName;

        @NotBlank(message = "时间字段不能为空")
        private String timeField;

        @NotNull(message = "保留天数不能为空")
        private Integer retentionDay;

        @NotNull(message = "一次限制条数不能为空")
        private Integer limitCount;

        private String extSql;
    }

    @Data
    @NoArgsConstructor
    public static class UpdateDTO {

        @NotBlank(message = "主键id不能为空")
        private String id;

        private String bizName;

        private String systemCode;

        private String tableName;

        private String timeField;

        private Integer retentionDay;

        private Integer limitCount;

        private String extSql;
    }
}
