package com.erp.model.workflow.dto;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONObject;
import com.common.business.annotation.Dict;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.erp.model.workflow.enums.DictBasicEnum;
import com.erp.model.workflow.enums.ProcessStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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

    private ProcessManagementDTO() {
    }

    /**
     * 状态统计
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabListDTO {

        /**
         * 类型
         */
        private String tabFlag;

        /**
         * 类型
         */
        private String tabFlagName;

        /**
         * 数量
         */
        private Integer count;

    }

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
        @NotNull(message = "流程参数Map不能为空")
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
         * 业务名称 用于流程图显示   任务审核: 产品名称+任务名称  其他申请单: 单据编号
         */
        private String businessName;
        /**
         * 流程开始时间
         */
        private String startTime;
        /**
         * 流程结束时间
         */
        private String endTime;

        private Boolean isExistProcess;

        public StartResultDTO(String processDefinitionId, String processInstanceId, String taskId, LocalDateTime processStartTime, String businessId, String businessName) {
            this.processDefinitionId = processDefinitionId;
            this.processInstanceId = processInstanceId;
            this.taskId = taskId;
            this.startTime = processStartTime.toString();
            this.businessId = businessId;
            this.businessName = businessName;
            this.isExistProcess = true;
        }

        public StartResultDTO(StartDTO dto) {
            this.businessId = dto.getBusinessId();
            this.businessName = dto.getBusinessName();
            this.isExistProcess = false;
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
                return CharSequenceUtil.isNotBlank(comment) ? comment : approveType.getName();
            }

    }

    @Data
    @NoArgsConstructor
    public static class ApproveResultDTO{
        /**
         * 流程定义id
         */
        private String processDefinitionId;

        /**
         * 流程实例id
         */
        private String processInstanceId;
        /**
         * 业务表id
         */
        private String businessId;

        /**
         * 业务名称 用于流程图显示   任务审核: 产品名称+任务名称  其他申请单: 单据编号
         */
        private String businessName;

        /**
         * 当前任务id
         */
        private String taskId;
        /**
         * 当前任务节点名称
         */
        private String activityName;

        /**
         * 当前任务节点id
         */
        private String activityId;

        /**
         * 流程是否存在
         */
        private Boolean isExistProcess;

        public ApproveResultDTO(String processDefinitionId, String processInstanceId, String businessId,
                                String businessName, String taskId, String activityName, String activityId) {
            this.processDefinitionId = processDefinitionId;
            this.processInstanceId = processInstanceId;
            this.businessId = businessId;
            this.businessName = businessName;
            this.taskId = taskId;
            this.activityName = activityName;
            this.activityId = activityId;
            this.isExistProcess = true;
        }

        public ApproveResultDTO(ApproveDTO dto) {
            this.businessId = dto.getBusinessId();
            this.isExistProcess = false;
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
    public static class BackResultDTO {

        /**
         * 流程定义id
         */
        private String processDefinitionId;

        /**
         * 流程实例id
         */
        private String processInstanceId;

        /**
         * 当前业务id
         */
        private String businessId;

        /**
         * 当前业务名称
         */
        private String businessName;
        /**
         * 当前业务key
         */
        private String businessKey;

        /**
         * 当前节点id
         */
        private String activityId;

        /**
         * 当前节点名称
         */
        private String activityName;
        /**
         * 流程是否存在
         */
        private Boolean isExistProcess;

        public BackResultDTO( String activityId, String activityName,  ManagementTaskDTO managementTask) {
            this.processDefinitionId = managementTask.getProcessDefinitionId();
            this.processInstanceId = managementTask.getProcessInstanceId();
            this.businessId = managementTask.getBusinessId();
            this.businessName = managementTask.getBusinessName();
            this.activityId = activityId;
            this.activityName = activityName;
            this.isExistProcess = true;
            this.businessKey = managementTask.getBusinessKey();
        }

        public BackResultDTO(BackDTO dto) {
            this.businessId = dto.getBusinessId();
            this.businessName = dto.getBusinessKey();
            this.isExistProcess = false;
        }

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
         * 流程创建时间
         */
        private String managementCreateUserId;

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
         * 当前审批人名称
         */
        private String curApproveName;


        /**
         * 任务状态
         */
        private ApproveStatusEnum taskStatus;

        /**
         * 超时预警状态
         */
        private Integer timeoutStatus;

        /**
         * 超时预警时间
         */
        private LocalDateTime timeoutWarnTime;

        /**
         * 超时时间
         */
        private LocalDateTime timeoutHandleTime;

        /**
         * 超时处理方式
         */
        private DictBasicEnum timeoutHandleType;

        /**
         * 任务开始时间
         */
        private LocalDateTime taskStartTime;

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
         * 审批人id
         */
        private String approveId;

        /**
         * 审批人名称
         */
        private String approveName;
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
    @AllArgsConstructor
    public static class TransferBatchDTO {
        @NotNull(message = "id不能为空")
        @Size(min = 1, message = "id不能为空")
        private List<String> ids;
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
    @AllArgsConstructor
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
    public static class ApproveActivityDTO {
        /**
         * 业务类型key
         */
        @NotBlank(message = "业务类型不能为空")
        private String businessKey;

        /**
         * 最新审核人id
         */
        @NotBlank(message = "最新审核人id不能为空")
        private String curApproveId;

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

        /**
         * 流程参数map
         */
        private Map<String,Object> variablesMap;
    }

    @Data
    @NoArgsConstructor
    public static class RevokeResultDTO {

        /**
         * 流程定义id
         */
        private String processDefinitionId;

        /**
         * 流程实例id
         */
        private String processInstanceId;

        /**
         * 当前业务id
         */
        private String businessId;

        /**
         * 当前业务名称
         */
        private String businessName;

        private Boolean isExistProcess;

        public RevokeResultDTO(String processDefinitionId, String processInstanceId, String businessId, String businessName) {
            this.processDefinitionId = processDefinitionId;
            this.processInstanceId = processInstanceId;
            this.businessId = businessId;
            this.businessName = businessName;
            this.isExistProcess = true;
        }

        public RevokeResultDTO(RevokeDTO dto) {
            this.businessId = dto.getBusinessId();
            this.businessName = dto.getBusinessKey();
            this.isExistProcess = false;
        }
    }


    @Data
    @NoArgsConstructor
    public static class SearchDTO extends SortDTO {
        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String, String> sqlMap;

        /**
         * 是否委托
         */
        private Boolean isDelegate;
    }

    @Data
    @NoArgsConstructor
    public static class ExportDTO{

        private List<String> businessNames;

        private List<ProcessStatusEnum> processStatus;

        private Integer processVersion;

        private String processName;

        private List<String> ids;

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String, String> sqlMap;

    }
    @Data
    @NoArgsConstructor
    public static class PagingResultDTO {
        /**
         * 任务id
         */
        private String id;
        /**
         * 流程节点id
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
         * 业务类型KEY
         */
        private String businessKey;
        /**
         * 单据名称
         */
        private String businessKeyName;
        /**
         * 审核来源
         */
        private String sourcePlatform;
        /**
         * 审核来源名称
         */
        private String sourcePlatformName;
        /**
         * 标签标识,isDelegate委托
         */
        private JSONObject labelJson;
        /**
         * 当前节点id
         */
        private String curActivityId;

        /**
         * 当前节点名称
         */
        private String curActivityName;

        /**
         * 审批人id
         */
        private String curApproveId;

        /**
         * 审批人名称
         */
        private String curApproveName;

        /**
         * 流程状态
         */
        private ProcessStatusEnum processStatus;

        /**
         * 任务id
         */
        private String taskId;

        /**
         * 任务状态
         */
        private ApproveStatusEnum taskStatus;

        /**
         * 任务状态名称
         */
        private String taskStatusName;

        /**
         * 流程状态
         */
        private String processStatusName;

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

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 更新时间
         */
        private LocalDateTime updateTime;
        /**
         * 流程节点结束时间
         */
        private LocalDateTime endTime;
        /**
         * 审核完成时间
         */
        private LocalDateTime approveTime;
    }

    @Data
    @NoArgsConstructor
    public static class MainPagingResultDTO {

        /**
         * 流程节点id
         */
        private String id;

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
         * 业务类型KEY
         */
        private String businessKey;
        /**
         * 单据名称
         */
        private String businessKeyName;
        /**
         * 审核来源
         */
        private String sourcePlatform;
        /**
         * 审核来源名称
         */
        private String sourcePlatformName;
        /**
         * 当前节点id
         */
        private String curActivityId;

        /**
         * 当前节点名称
         */
        private String curActivityName;

        /**
         * 流程状态
         */
        private ProcessStatusEnum processStatus;

        /**
         * 流程状态
         */
        private String processStatusName;

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

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 流程节点结束时间
         */
        private LocalDateTime endTime;
    }

    @Data
    @NoArgsConstructor
    public static class DetailPagingResultDTO {
        /**
         * 任务表id
         */
        private String id;

        /**
         * 流程名称
         */
        private String processName;

        /**
         * 当前节点id
         */
        private String curActivityId;

        /**
         * 当前节点名称
         */
        private String curActivityName;

        /**
         * 审批人id
         */
        private String curApproveId;

        /**
         * 审批人名称
         */
        private String curApproveName;

        /**
         * 标签标识,isDelegate委托
         */
        private JSONObject labelJson;

        /**
         * 任务状态
         */
        @Dict
        private ApproveStatusEnum taskStatus;

        /**
         * 审核完成时间
         */
        private LocalDateTime approveTime;
    }

    @Data
    @NoArgsConstructor
    public static class DetailSearchDTO {
        /**
         * 审核来源
         */
        @NotBlank(message = "审核来源不能为空")
        private String sourcePlatform;

        /**
         * 主键id
         */
        private String id;
    }


    @Data
    @NoArgsConstructor
    public static class ExportResultDTO {

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
         * 当前节点名称
         */
        private String curActivityName;
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
        private String processStatusName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

    }

    @Data
    @NoArgsConstructor
    public static class ProgressDTO {

        @NotBlank(message = "流程实例id不能为空")
        private String processInstanceId;
    }

    @Data
    @NoArgsConstructor
    public static class ProcessResultDTO {

        /**
         * 流程实例ID
         */
        private String processInstanceId;
        /**
         * 流程定义ID
         */
        private String processDefinitionId;
        /**
         * 当前任务列表
         */
        private List<TaskResultDTO> curTaskList;

        /**
         * 流程定义xml
         */
        private String bpmnXml;

        public ProcessResultDTO(List<TaskResultDTO> tasks, String bpmnXml, String processInstanceId, String processDefinitionId) {
            this.curTaskList = tasks;
            this.bpmnXml = bpmnXml;
            this.processInstanceId = processInstanceId;
            this.processDefinitionId = processDefinitionId;
        }
    }

    @Data
    @NoArgsConstructor
    public static class TaskResultDTO {

        /**
         * 任务名称
         */
        private String taskName;
        /**
         * 任务定义key
         */
        private String taskDefinitionKey;
        /**
         * 任务ID
         */
        private String taskId;

        public TaskResultDTO(String taskId, String taskName, String taskDefinitionKey) {
            this.taskId = taskId;
            this.taskName = taskName;
            this.taskDefinitionKey = taskDefinitionKey;
        }
    }

    @Data
    @NoArgsConstructor
    public static class CurApproveInfoDTO{

        /**
         * 流程实例ID
         */
        private String processInstanceId;

        /**
         * 流程业务ID
         */
        private String businessId;

        /**
         * 流程业务KEY
         */
        private String businessKey;

        /**
         * 流程定义ID
         */
        private String businessName;

        /**
         * 当前节点id
         */
        private String activityId;

        /**
         * 当前节点名称
         */
        private String activityName;

        /**
         * 当前审批人id
         */
        private String curApproveId;

        /**
         * 当前审批人名称
         */
        private String curApproveName;

        public CurApproveInfoDTO(HistoryActivityDTO historyActivityDTO) {
            this.businessId = historyActivityDTO.getBusinessId();
            this.businessKey = historyActivityDTO.getBusinessKey();
        }
    }

    @Data
    @NoArgsConstructor
    public static class TaskKeyInfoDTO {
        /**
         * 业务类型key
         */
        @NotBlank(message = "业务类型不能为空")
        private String businessKey;

        /**
         * 最新审核人id
         */
        @NotBlank(message = "最新审核人id不能为空")
        private String curApproveId;
        /**
         * 任务状态
         */
        @NotBlank(message = "任务状态不能为空")
        private String taskStatus;

    }

    @Data
    @NoArgsConstructor
    public static class CheckSubmitByBusinessIdDTO {
        /**
         * 业务类型key
         */
        private String businessKey;

        /**
         * 业务id
         */
        private String businessId;


    }
}
