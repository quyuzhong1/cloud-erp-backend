package com.erp.model.scm.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/16 11:05
 */
@Data
@NoArgsConstructor
public class ModuleOperateLogDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class listDTO {
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
    public static class addDTO {
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
    public static class searchDTO {
        /**
         * 模块类型 (枚举ModuleOperateLogTypeEnum,0备货申请单,1采购申请单,采购订单)
         */
        @NotNull(message = "模块不能为空")
        private List<String> moduleType;

        /**
         * 业务id(对应模块id)
         */
        private String businessId;

        /**
         * 父级id(用于综合数据查询)
         */
        private String pid;
    }

}
