package com.erp.model.workflow.dto;

import com.common.business.enums.ApproveTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 流程管理参数类
 *
 * @Author Cloud
 * @Date 2023/4/26 10:16
 **/
public class ProcessManagementDTO {

    @Data
    @NoArgsConstructor
    public static class StartDTO {

        /**
         * 业务类型
         */
        @NotBlank(message = "业务类型不能为空")
        private String businessKey;

        /**
         * 业务发起人
         */
        @NotBlank(message = "发起人不能为空")
        private String userId;

        /**
         * 业务表id
         */
        @NotBlank(message = "业务表id不能为空")
        private String businessId;
        /**
         * 业务单号
         */
        private String businessCode;

        /**
         * 流程参数map
         */
        private Map<String,Object> variablesMap;
    }

    @Data
    @NoArgsConstructor
    public static class StartResultDTO {

        /**
         * 流程定义id
         */
        private String processDefinitionId;

        /**
         * 流程实例id
         */
        private String processInstanceId;

        /**
         * 当前任务id
         */
        private String taskId;
        /**
         * 业务表id
         */
        private String businessId;
        /**
         * 流程开始时间
         */
        private String startTime;
        /**
         * 流程结束时间
         */
        private String endTime;

        public StartResultDTO(String processDefinitionId, String processInstanceId, String taskId, LocalDateTime processStartTime, String businessId) {
            this.processDefinitionId = processDefinitionId;
            this.processInstanceId = processInstanceId;
            this.taskId = taskId;
            this.startTime = processStartTime.toString();
            this.businessId = businessId;
        }
    }

    @Data
    @NoArgsConstructor
    public static class ApproveDTO {

            @NotBlank(message = "业务类型不能为空")
            private String businessKey;

            /**
            * 业务表id
            */
            @NotBlank(message = "业务表id不能为空")
            private String businessId;

            /**
            * 审批人
            */
            @NotBlank(message = "审批人不能为空")
            private String userId;

            /**
            * 审批意见
            */
            private String comment;

            /**
            * 审批结果
            */
            @NotBlank(message = "审批结果不能为空")
            private ApproveTypeEnum approveType;

            /**
            * 流程参数map
            */
            private Map<String,Object> variablesMap;
    }
}
