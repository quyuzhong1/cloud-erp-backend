package com.erp.model.oms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 任务编排实例 DTO
 */
@Data
@NoArgsConstructor
public class WorkflowTaskInstanceDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {
        /** 页面高级查询 */
        private List<AdvanceQueryDTO> advanceQueryDTOList;
        /** sqlMap 默认 key default */
        private Map<String, String> sqlMap;

        private List<String> sourceTypeList;
        private String sourceCode;
        private String sourceId;
        private List<String> statusList;
        private Boolean hasError;
        private String currentNodeName;
        private String targetService;
    }

    @Data
    @NoArgsConstructor
    public static class ListDTO implements Serializable {
        private String instanceId;
        private String sourceType;
        private String sourceTypeName;
        private String sourceId;
        private String sourceCode;
        private String status;
        private String statusName;
        private Integer currentIndex;
        private Integer totalSteps;
        private Integer progressPercent;
        private String currentNodeName;
        private String lastError;
        private Integer retryCount;
        private String traceId;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
        private String createUserName;
        /** 是否已达自动重试上限（retryCount > 3 且失败） */
        private Boolean autoRetryExceeded;
    }

    @Data
    @NoArgsConstructor
    public static class ViewDTO implements Serializable {
        private String instanceId;
        private String sourceType;
        private String sourceTypeName;
        private String sourceId;
        private String sourceCode;
        private String status;
        private String statusName;
        private Integer currentIndex;
        private Integer totalSteps;
        private Integer progressPercent;
        private String lastError;
        private String traceId;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
        private String createUserName;
        private LocalDateTime startTime;
        private LocalDateTime finishTime;
        private Boolean autoRetryExceeded;
        private List<StepDTO> steps;
    }

    @Data
    @NoArgsConstructor
    public static class StepDTO implements Serializable {
        /** 批量查询时用于分组，非 API 必返字段 */
        private String instanceId;
        private String stepId;
        private Integer index;
        private String nodeName;
        private String classPath;
        private String handlerCode;
        private String targetService;
        private String targetServiceName;
        private String targetEndpoint;
        private Long feignDurationMs;
        private String errorSource;
        private String status;
        private String statusName;
        private Integer retryCount;
        private String lastError;
        private String inputData;
        private String outputData;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Long durationMs;
        private String remark;
    }

    /** 实例级人工重试入参：指定 stepId 或 instanceId 二选一。 */
    @Data
    @NoArgsConstructor
    public static class RetryDTO implements Serializable {
        private String instanceId;
        private String stepId;
        private Integer retryCount;
        private String remark;
    }

    /** 从指定 index 起重跑后续节点的入参。 */
    @Data
    @NoArgsConstructor
    public static class RetryFromStepDTO implements Serializable {
        @NotBlank
        private String instanceId;
        @Min(0)
        private Integer fromIndex;
        private Integer retryCount;
        private String remark;
    }

    /** 取消编排实例入参。 */
    @Data
    @NoArgsConstructor
    public static class CancelDTO implements Serializable {
        @NotBlank
        private String instanceId;
        private String remark;
    }

    /** 按业务单查询编排实例历史入参。 */
    @Data
    @NoArgsConstructor
    public static class ListBySourceParamDTO implements Serializable {
        @NotBlank
        private String sourceType;
        @NotBlank
        private String sourceId;
        /** 数据权限 SQL，由框架注入 */
        private String permissionSql;
    }

    /** 异常统计查询条件。 */
    @Data
    @NoArgsConstructor
    public static class ErrorReportParamDTO implements Serializable {
        private List<String> sourceTypeList;
        private String targetService;
        /** 数据权限 SQL，由框架注入 */
        private String permissionSql;
    }

    /** 异常统计结果：按业务类型 × 节点 × 目标服务聚合。 */
    @Data
    @NoArgsConstructor
    public static class ErrorReportDTO implements Serializable {
        private String sourceType;
        private String sourceTypeName;
        private String dictBasicId;
        private String dictBasicName;
        private String targetService;
        private String targetServiceName;
        private Integer failedCount;
        private Integer waitingCount;
        private Integer avgRetryCount;
        private LocalDateTime lastOccurTime;
    }

    /** 实例重试 API 出参，封装底层 forceRetry 结果。 */
    @Data
    @NoArgsConstructor
    public static class RetryResultDTO implements Serializable {
        private String instanceId;
        private WorkflowTaskRecordDTO.ForceRetryResultDTO forceRetryResult;
    }
}
