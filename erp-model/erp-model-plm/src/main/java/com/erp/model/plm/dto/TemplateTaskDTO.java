package com.erp.model.plm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

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
        @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
        private Date planStartTime;

        /**
         * j计划结束时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
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
         * 是否是固定任务 1 是  0  不是
         */
        private Integer isFixed;

        /**
         * 任务描述
         */
        private String description;

        /**
         * 模板表id
         */
        private String templateId;

        /**
         * 交付文档
         */
        @Valid
        private List<DocsDTO> deliveryDocsList;

        /**
         * 前置任务id
         */
        private List<String> preTaskIdList;

        /**
         * 业务流程表id
         */
        private String businessProcessId;

        /**
         * 业务流程名
         */
        private String businessName="";
        /**
         * 审核人集合
         */
        private List<UserInfoDTO> approvalUserIds;

        /**
         * 负责人id
         */
        @NotNull(message = "任务负责人集合不能为空")
        @Size(min = 1,message = "负责人至少有一个")
        private List<String> chargeIds;

        /**
         * 设置里程碑(0否，1是)
         */
        private Integer isMilepost;

        /**
         * 字段配置类型
         */
        private String fieldConfigType="";

        /**
         * 勾选字段后的json 字段
         */
        private String fieldJson="";
}
