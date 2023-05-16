package com.erp.model.workflow.dto;

import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.erp.model.workflow.enums.DictBasicEnum;
import com.erp.model.workflow.enums.ProcessStatusEnum;
import com.erp.model.workflow.enums.RejectTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
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
         * 业务名称 用于流程图显示   任务审核: 产品名称+任务名称  其他申请单: 单据编号
         */
        @NotBlank(message = "业务名称不能为空")
        private String businessName;

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
            @NotNull(message = "审批结果不能为空")
            private ApproveTypeEnum approveType;

            /**
            * 流程参数map
            */
            private Map<String,Object> variablesMap;

            public String getComment() {
                return StrUtil.isNotBlank(comment) ? comment : approveType.getName();
            }

    }

    @Data
    @NoArgsConstructor
    public static class BackDTO extends ApproveDTO {
        /**
         * 驳回的目标节点 ID
         */
        @NotBlank(message = "驳回的目标节点不能为空")
        private String activityId;
    }

    @Data
    @NoArgsConstructor
    public static class ManagementTaskDTO {

        /**
         * 流程管理ID
         */
        private String managementId;

        /**
         * 流程实例ID
         */
        private String processInstanceId;

        /**
         * 流程定义ID
         */
        private String processDefinitionId;

        /**
         * 业务ID
         */
        private String businessId;

        /**
         * 业务编码
         */
        private String businessCode;

        /**
         * 当前节点ID
         */
        private String curActivityId;

        /**
         * 流程状态
         */
        private ProcessStatusEnum processStatus;

        /**
         * 开始时间
         */
        private LocalDateTime startTime;

        /**
         * 结束时间
         */
        private LocalDateTime endTime;
        /**
         * 业务名称
         */
        private String businessName;

        /**
         * 审核状态 approveStatus
         */
        private ApproveStatusEnum approveStatus;

        /**
         * 业务类型KEY
         */
        private String businessKey;

        /**
         * 流程引擎流程实例ID
         */
        private String actProcessDefinitionId;

        /**
         * 流程引擎任务ID
         */
        private String taskManagementId;

        /**
         * 当前节点名称
         */
        private String curActivityName;

        /**
         * 任务ID
         */
        private String taskId;

        /**
         * 当前审批人ID
         */
        private String curApproveId;

        /**
         * 任务状态
         */
        private ApproveStatusEnum taskStatus;

        /**
         * 超时预警状态
         */
        private Integer timeoutWarnStatus;

        /**
         * 超时预警时间
         */
        private Integer timeoutWarnInterval;

        /**
         * 超时时间
         */
        private Integer timeoutInterval;

        /**
         * 超时处理方式
         */
        private String timeoutHandleType;

        /**
         * 审批时间
         */
        private LocalDateTime approveTime;

        /**
         * 备注
         */
        private String remark;
        /**
         * 上一节点ID
         */
        private String preActivityId;

        /**
         * 执行id
         */
        private String executionId;

        /**
         * 申请人id
         */
        private String approveId;
    }

    @Data
    @NoArgsConstructor
    public static class TransferDTO {
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
        @NotBlank(message = "转办申请人")
        private String sourceUserId;
        /**
         * 转办目标人
         */
        @NotBlank(message = "转办目标人")
        private String targetUserId;

        /**
         * 备注
         */
        private String remark;
    }

    @Data
    @NoArgsConstructor
    public static class HistoryActivityDTO {
        /**
         * 业务类型key
         */
        @NotBlank(message = "业务类型不能为空")
        private String businessKey;

        /**
         * 业务表id
         */
        @NotBlank(message = "业务表id不能为空")
        private String businessId;

    }

    @Data
    @NoArgsConstructor
    public static class HistoryActivityResultDTO {
        /**
         * 流程定义id
         */
        private String processDefinitionId;
        /**
         * 流程实例id
         */
        private String processInstanceId;
        /**
         * 流程名称
         */
        private String processName;
        /**
         * 业务id
         */
        private String businessId;
        /**
         * 业务编码
         */
        private String businessCode;
        /**
         * 业务名称
         */
        private String businessName;
        /**
         * 节点id
         */
        private String activityId;
        /**
         * 节点名称
         */
        private String activityName;

        /**
         * 节点状态
         */
        private String activityStatus;

        /**
         *  orSignature 或签  jointSignature 会签
         * 节点类型
         */
        private DictBasicEnum activityType;

        public HistoryActivityResultDTO(ManagementTaskDTO task) {
            this.processDefinitionId = task.getProcessDefinitionId();
            this.processInstanceId = task.getProcessInstanceId();
            this.processName = task.getBusinessName();
            this.businessId = task.getBusinessId();
            this.businessCode = task.getBusinessCode();
            this.businessName = task.getBusinessName();
            this.activityId = task.getCurActivityId();
            this.activityName = task.getCurActivityName();
            this.activityStatus = task.getApproveStatus().getName();

        }
    }

    @Data
    @NoArgsConstructor
    public static class RevokeDTO {

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
        @NotBlank(message = "撤回人不能为空")
        private String userId;

        /**
         * 备注
         */
        private String remark;
    }

    @Data
    @NoArgsConstructor
    public static class SearchDTO{

        private List<String> businessNames;

        private List<ProcessStatusEnum> processStatus;

        private Integer processVersion;

    }
    @Data
    @NoArgsConstructor
    public static class PagingResultDTO {

        /**
         * 流程实例ID
         */
        private String processInstanceId;

        /**
         * 流程定义ID
         */
        private String processDefinitionId;
        /**
         * 流程定义版本
         */
        private Integer processVersion;
        /**
         * 流程名称
         */
        private String processName;

        /**
         * 业务ID
         */
        private String businessId;

        /**
         * 业务编码
         */
        private String businessCode;

        /**
         * 业务名称
         */
        private String businessName;

        /**
         * 审批人名称
         */
        private String curApproveName;

        /**
         * 流程状态
         */
        private ProcessStatusEnum processStatus;

        /**
         * 开始时间
         */
        private LocalDateTime startTime;

        /**
         * 结束时间
         */
        private LocalDateTime endTime;

        /**
         * 流程引擎流程实例ID
         */
        private String actProcessDefinitionId;

        /**
         * 详情地址
         */
        private String detailUrl;

        /**
         * 项目名称
         */
        private String sysClassify;

    }

    @Data
    @NoArgsConstructor
    public static class ProgressDTO {

        @NotBlank(message = "流程实例id不能为空")
        private String processInstanceId;
    }
}
