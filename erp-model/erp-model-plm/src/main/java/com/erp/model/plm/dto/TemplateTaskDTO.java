package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Will
 * @version 1.0
 * @description: 模板任务DTO
 * @date 2022/11/14 9:20
 */
@Data
@NoArgsConstructor
public class TemplateTaskDTO implements Serializable {

        /**
         * 任务id
         */
        private String id;

        /**
         * 任务名
         */
        @NotBlank(message = "任务名称不能为空")
        private String name;

        /**
         * 父级id
         */
        private String pid;

        /**
         * 任务类型 0 一般任务 1：审核任务
         */
        @NotNull(message = "任务类型不能为空")
        private Integer type;

        /**
         * 负责人id
         */
        private String chargeId;

        /**
         * 负责人名
         */
        private String chargeName;

        /**
         * 计划开始时间
         */
        private Date planStartTime;

        /**·
         * 审核人id 多个以逗号分割
         */
        private String approvalUserId;

        /**
         * j计划结束时间
         */
        private Date planEndTime;

        /**
         * 任务优先级 1 低级 2 中级 3 高级
         */
        private Integer priority;

        /**
         * 任务阶段id
         */
        private String phaseId;

        /**
         * 任务阶段名称
         */
        private String phaseName;

        /**
         * 是否是系统任务 1 是  0  不是
         */
        private Integer isFixed;

        /**
         * 任务描述
         */
        private String description;

        /**
         * 创建时间
         */
        private Date createTime;

        /**
         * 更新时间
         */
        private Date updateTime;

        /**
         * 引用系统任务的id
         */
        private String quoteSysTaskId;

        /**
         * 模板表id
         */
        private String templateId;

        /**
         * 任务属性 1： 立项任务  2：项目任务
         */
        private Integer property;

       /**
        * 流程表id
        */
        private String businessProcessId;
}
