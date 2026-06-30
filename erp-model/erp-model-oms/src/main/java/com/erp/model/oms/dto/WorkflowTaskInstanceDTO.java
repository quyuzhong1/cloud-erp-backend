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

    /**
     * 实例分页查询入参
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {
        /** 页面高级查询 */
        private List<AdvanceQueryDTO> advanceQueryDTOList;
        /** sqlMap 默认 key default */
        private Map<String, String> sqlMap;

        /** 业务类型多选，对应 WorkflowTaskRecordTypeEnum.code */
        private List<String> sourceTypeList;
        /** 来源单号，模糊匹配 */
        private String sourceCode;
        /** 来源业务主键，精确匹配 */
        private String sourceId;
        /** 实例状态多选：running/waiting/success/failed/cancelled */
        private List<String> statusList;
        /** 是否仅查询含失败节点的实例 */
        private Boolean hasError;
        /** 当前节点名称，对应 dict_basic.name */
        private String currentNodeName;
        /** 目标微服务编码，如 oms/wms/tms */
        private String targetService;
    }

    /**
     * 实例分页列表出参
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO implements Serializable {
        /** 编排实例 ID */
        private String instanceId;
        /** 业务类型编码 [可排序] */
        private String sourceType;
        /** 业务类型名称 */
        private String sourceTypeName;
        /** 来源业务主键 */
        private String sourceId;
        /** 来源单号 [可排序] */
        private String sourceCode;
        /** 实例状态编码 [可排序] */
        private String status;
        /** 实例状态名称 */
        private String statusName;
        /** 当前执行节点序号（从 0 开始） */
        private Integer currentIndex;
        /** 编排总节点数 */
        private Integer totalSteps;
        /** 进度百分比：成功节点数 / 总节点数 × 100 */
        private Integer progressPercent;
        /** 当前执行节点名称 */
        private String currentNodeName;
        /** 最近错误摘要 */
        private String lastError;
        /** 当前失败节点的重试次数 */
        private Integer retryCount;
        /** 链路追踪 ID */
        private String traceId;
        /** 创建时间 [可排序] */
        private LocalDateTime createTime;
        /** 更新时间 [可排序] */
        private LocalDateTime updateTime;
        /** 触发人姓名 */
        private String createUserName;
        /** 是否已达自动重试上限（retryCount > 3 且失败） */
        private Boolean autoRetryExceeded;
    }

    /**
     * 实例详情出参
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO implements Serializable {
        /** 编排实例 ID */
        private String instanceId;
        /** 业务类型编码 */
        private String sourceType;
        /** 业务类型名称 */
        private String sourceTypeName;
        /** 来源业务主键 */
        private String sourceId;
        /** 来源单号 */
        private String sourceCode;
        /** 实例状态编码 */
        private String status;
        /** 实例状态名称 */
        private String statusName;
        /** 当前执行节点序号（从 0 开始） */
        private Integer currentIndex;
        /** 编排总节点数 */
        private Integer totalSteps;
        /** 进度百分比：成功节点数 / 总节点数 × 100 */
        private Integer progressPercent;
        /** 最近错误摘要 */
        private String lastError;
        /** 链路追踪 ID */
        private String traceId;
        /** 创建时间 */
        private LocalDateTime createTime;
        /** 更新时间 */
        private LocalDateTime updateTime;
        /** 触发人姓名 */
        private String createUserName;
        /** 编排开始时间 */
        private LocalDateTime startTime;
        /** 编排结束时间 */
        private LocalDateTime finishTime;
        /** 是否已达自动重试上限 */
        private Boolean autoRetryExceeded;
        /** 节点列表，按 index 升序 */
        private List<StepDTO> steps;
    }

    /**
     * 编排节点详情
     */
    @Data
    @NoArgsConstructor
    public static class StepDTO implements Serializable {
        /** 批量查询时用于分组，非 API 必返字段 */
        private String instanceId;
        /** 节点记录 ID（workflow_task_record.id） */
        private String stepId;
        /** 节点序号（从 0 开始） */
        private Integer index;
        /** 节点名称，对应 dict_basic.name */
        private String nodeName;
        /** 节点执行类全路径或配置原始值 */
        private String classPath;
        /** 节点处理器编码（运维展示用） */
        private String handlerCode;
        /** 目标微服务编码 */
        private String targetService;
        /** 目标微服务名称 */
        private String targetServiceName;
        /** 目标接口标识，如 OtherInstockController#generateOtherApprove */
        private String targetEndpoint;
        /** 跨服务 Feign 调用耗时（毫秒） */
        private Long feignDurationMs;
        /** 错误来源：orchestrator / remote */
        private String errorSource;
        /** 节点状态编码：pending/processing/waiting/success/failed */
        private String status;
        /** 节点状态名称 */
        private String statusName;
        /** 节点重试次数 */
        private Integer retryCount;
        /** 最近错误信息 */
        private String lastError;
        /** 节点输入数据（JSON 字符串） */
        private String inputData;
        /** 节点输出数据（JSON 字符串） */
        private String outputData;
        /** 节点开始执行时间 */
        private LocalDateTime startTime;
        /** 节点结束执行时间 */
        private LocalDateTime endTime;
        /** 节点执行耗时（毫秒） */
        private Long durationMs;
        /** 备注，含人工重试说明 */
        private String remark;
    }

    /** 实例级人工重试入参。 */
    @Data
    @NoArgsConstructor
    public static class RetryDTO implements Serializable {
        /** 编排实例 ID*/
        @NotBlank(message = "实例ID不能为空")
        private String instanceId;
        /** 重置后的 retry_count；不传时默认当前值 +1 */
        private Integer retryCount;
        /** 操作备注，写入节点 remark */
        private String remark;
    }

    /** 从指定 index 起重跑后续节点的入参。 */
    @Data
    @NoArgsConstructor
    public static class RetryFromStepDTO implements Serializable {
        /** 编排实例 ID */
        @NotBlank
        private String instanceId;
        /** 起始节点序号，fromIndex 及之后节点重置为 pending 后重跑 */
        @Min(0)
        private Integer fromIndex;
        /** 重置后的 retry_count；不传时默认当前值 +1 */
        private Integer retryCount;
        /** 操作备注 */
        private String remark;
    }

    /** 取消编排实例入参。 */
    @Data
    @NoArgsConstructor
    public static class CancelDTO implements Serializable {
        /** 编排实例 ID */
        @NotBlank
        private String instanceId;
        /** 取消原因备注 */
        private String remark;
    }

    /** 按业务单查询编排实例历史入参。 */
    @Data
    @NoArgsConstructor
    public static class ListBySourceParamDTO implements Serializable {
        /** 业务类型编码 */
        @NotBlank
        private String sourceType;
        /** 来源业务主键 */
        @NotBlank
        private String sourceId;
        /** 数据权限 SQL，由框架注入 */
        private String permissionSql;
    }

    /** 异常统计查询条件。 */
    @Data
    @NoArgsConstructor
    public static class ErrorReportParamDTO implements Serializable {
        /** 业务类型多选 */
        private List<String> sourceTypeList;
        /** 目标微服务编码 */
        private String targetService;
        /** 数据权限 SQL，由框架注入 */
        private String permissionSql;
    }

    /** 异常统计结果：按业务类型 × 节点 × 目标服务聚合。 */
    @Data
    @NoArgsConstructor
    public static class ErrorReportDTO implements Serializable {
        /** 业务类型编码 */
        private String sourceType;
        /** 业务类型名称 */
        private String sourceTypeName;
        /** 节点字典 ID（dict_basic.id） */
        private String dictBasicId;
        /** 节点名称（dict_basic.name） */
        private String dictBasicName;
        /** 目标微服务编码 */
        private String targetService;
        /** 目标微服务名称 */
        private String targetServiceName;
        /** 失败次数 */
        private Integer failedCount;
        /** 等待中次数 */
        private Integer waitingCount;
        /** 平均重试次数 */
        private Integer avgRetryCount;
        /** 最近发生时间 */
        private LocalDateTime lastOccurTime;
    }

    /** 实例重试 API 出参，封装底层 forceRetry 结果。 */
    @Data
    @NoArgsConstructor
    public static class RetryResultDTO implements Serializable {
        /** 编排实例 ID */
        private String instanceId;
        /** 底层 forceRetry 执行结果 */
        private WorkflowTaskRecordDTO.ForceRetryResultDTO forceRetryResult;
    }
    @Data
    @NoArgsConstructor
    public static class TabListDTO {
        /**
         * 类型
         */
        private String tabFlag;

        /**
         * 类型名称
         */
        private String tabFlagName;

        /**
         * 数量
         */
        private Integer count;
    }
}
