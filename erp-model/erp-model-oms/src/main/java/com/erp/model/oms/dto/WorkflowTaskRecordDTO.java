package com.erp.model.oms.dto;

import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.oms.enums.WorkflowTaskRecordTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 任务节点记录表请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-09-16
*/
@Data
@NoArgsConstructor
public class WorkflowTaskRecordDTO implements Serializable {




    /**
    * 新增任务
    */
    @Data
    @NoArgsConstructor
    public static class AddTaskDTO {

        private WorkflowTaskRecordTypeEnum sourceTypeEnum;

        private DictBasicTypeEnum dictBasicTypeEnum;

        private String sourceId;

        private String sourceCode;

        private String traceId;

        //第一个节点的入参
        private Map<String, Object> firstNodeInputData = new HashMap<>();

        /**
         * 编排实例 id（单步调度）
         */
        private String instanceId;

        /**
         * 指定执行的节点序号；为空时自动选择第一个未完成节点
         */
        private Integer targetIndex;

        /**
         * 是否允许重试 failed 节点（Job/人工重试时为 true）
         */
        private Boolean retryFailedStep;

        /**
         * 人工 forceRetry 标记，跳开自动重试上限校验
         */
        private Boolean forceRetry;
    }


    /**
    * mq请求体（跨服务节点统一入参）
    */
    @Data
    @NoArgsConstructor
    public static class MqRequestDTO {

        /** 节点业务入参，通常为上一节点 output 或首节点 firstNodeInputData */
        private Map<String,Object> data;

        /** 当前任务节点 ID */
        private String taskId;

        /** 业务类型编码 */
        private String sourceType;

        /** 节点序号 */
        private Integer index;

        /** 编排实例 ID */
        private String instanceId;

        /** 链路 traceId */
        private String traceId;

    }




    /**
     * mq响应体
     */
    @Data
    @NoArgsConstructor
    public static class MqResponseDTO {

        private Map<String,Object> data;

        private String errorMsg;

        /**
         * 节点执行状态，与 WorkflowTaskRecordStatusEnum 一致（如 SUCCESS / FAILED / WAITING）
         */
        private String status;
    }

    /**
     * 异常任务报告
     */
    @Data
    @NoArgsConstructor
    public static class TaskErrorReportDTO {

        private String sourceType;

        private String dictBasicId;

        private String dictBasicName;

        private Integer errorCount;
    }

    /**
     * 人工强制重试入参（运维/API 使用）。
     */
    @Data
    @NoArgsConstructor
    public static class ForceRetryDTO {

        /**
         * 指定节点 id；与 sourceType+sourceId 二选一，id 优先级更高
         */
        private String id;

        /** 业务类型编码 */
        private String sourceType;

        /** 业务主键 */
        private String sourceId;

        /** 编排实例 ID；指定时仅重试该实例下的节点 */
        private String instanceId;

        /**
         * 重置后的 retry_count，默认 0
         */
        private Integer retryCount;

        /** 操作备注，写入节点 remark */
        private String remark;
    }

    /**
     * 人工强制重试结果。
     */
    @Data
    @NoArgsConstructor
    public static class ForceRetryResultDTO {

        private String sourceType;

        private String sourceId;

        /** 被重置为 PENDING 的节点数量 */
        private int resetCount;

        /** 实际发送的调度 MQ 数量（按 source 分组） */
        private int scheduledMqCount;

        /** 同 scheduledMqCount，兼容旧字段 */
        private int mqCount;
    }

}
