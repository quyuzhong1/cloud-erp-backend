package com.erp.model.plm.dto;

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
 * 操作日志表请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2024-12-02
*/
@Data
@NoArgsConstructor
public class OperateLogDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * 操作
         */
        private String operation;

        /**
         * 内容
         */
        private String content;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 创建人
         */
        private String createUserName;
    }

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
        * 类型
        */
        private String moduleType;

        /**
        * 字段名称
        */
        private String fieldName;

        /**
        * 业务id
        */
        private String businessId;

        /**
        * 旧值
        */
        private String oldValue;

        /**
        * 新值
        */
        private String newValue;

        /**
        * 内容
        */
        private String content;

        /**
        * 操作项
        */
        private String operation;

        /**
        * 父级id
        */
        private String pid;


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
        * 类型
        */
        @NotBlank(message = "类型不能为空")
        @Size(max = 64,message = "类型最大长度不能超过64位")
        private String moduleType;

        /**
        * 字段名称
        */
        @NotBlank(message = "字段名称不能为空")
        @Size(max = 64,message = "字段名称最大长度不能超过64位")
        private String fieldName;

        /**
        * 业务id
        */
        @NotBlank(message = "业务id不能为空")
        @Size(max = 19,message = "业务id最大长度不能超过19位")
        private String businessId;

        /**
        * 旧值
        */
        @NotBlank(message = "旧值不能为空")
        private String oldValue;

        /**
        * 新值
        */
        @NotBlank(message = "新值不能为空")
        private String newValue;

        /**
        * 内容
        */
        @NotBlank(message = "内容不能为空")
        private String content;

        /**
        * 操作项
        */
        @NotBlank(message = "操作项不能为空")
        @Size(max = 64,message = "操作项最大长度不能超过64位")
        private String operation;

        /**
        * 父级id
        */
        @NotBlank(message = "父级id不能为空")
        @Size(max = 19,message = "父级id最大长度不能超过19位")
        private String pid;


    }
    @Data
    @NoArgsConstructor
    public static class SearchDTO {

        /**
         * 业务id(对应模块id)
         */
        @NotBlank(message = "业务id不能为空")
        private String businessId;

        /**
         * 父级id(用于综合数据查询)
         */
        private String pid;
    }

}