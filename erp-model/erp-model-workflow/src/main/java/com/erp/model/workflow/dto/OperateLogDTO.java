package com.erp.model.workflow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 操作日志表请求响应实体
 * </p>
 *
 * @author will
 * @since 2025-05-12
*/
@Data
@NoArgsConstructor
public class OperateLogDTO implements Serializable {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddModuleOperateLogDTO {
        private String content;

        private String moduleType;

        private String businessId;

        private String operation;

    }


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

    @Data
    @NoArgsConstructor
    public static class AddDTO {
        /**
         * 模块类型(0备货申请单,1采购申请单,2采购订单)
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
         * 父级id（用于汇总展示日志）
         */
        private String pid;

        /**
         * 操作
         */
        private String operation;

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

        private String searchKey;
    }

}