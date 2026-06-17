package com.erp.model.tms.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.util.Map;

import com.common.business.dto.base.SuperDTO;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 异步任务记录请求响应实体
 * </p>
 *
 * @author jack
 * @since 2026-01-28
*/
@Data
@NoArgsConstructor
public class TmsAsyncTaskRecordDTO implements Serializable {

    public static final String RETRY_MODE_FAILED_ONLY = "FAILED_ONLY";

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
        private String tabFlagName;

        /**
         * 数量
         */
        private Integer count;

    }

    /**
     * 分页列表查询参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;

        /**
         * 勾选的id集合
         */
        private List<String> ids;

    }

    /**
     * 分页列表查询参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingDetailParamDTO extends SortDTO {

        /**
         * 勾选的id
         */
        @NotBlank(message = "id不能为空")
        private String id;

    }


    /**
     * 分页列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
         * 主键id
         */
        private String  id;
        /**
         * 创建人id
         */
        private String createUserId;

        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 修改人id
         */
        private String updateUserId;

        /**
         * 修改人名称
         */
        private String updateUserName;

        /**
         * 更新时间
         */
        private LocalDateTime updateTime;

        /**
         * 任务id
         */
        private String code;

        /**
         * 执行系统
         */
        private String sysModule;
        private String sysModuleName;

        /**
         * 单据名称
         */
        private String businessType;
        private String businessTypeName;

        /**
         * 开始时间
         */
        private LocalDateTime startTime;

        /**
         * 结束时间
         */
        private LocalDateTime endTime;

        /**
         * 任务超时时间 单位：秒
         */
        private Integer execTimeout;

        /**
         * 重试次数
         */
        private Integer retryTimes;

        /**
         * 状态：pending=待执行,ing=进行中, finish=已完成, failed=失败 AsyncTaskRecordStatusEnum
         */
        private String status;
        private String statusName;

        /**
         * 错误信息
         */
        private String errorData;

        /**
         * 执行数量
         */
        private Integer detailCount;

        /**
         * 错误数量
         */
        private Integer errorCount;

        /**
         * 执行类型：auto=自动, manual=手动 AsyncTaskRecordExecTypeEnum
         */
        private String execType;
        private String execTypeName;

        /**
         * 方法类型：同一 business_type 下区分不同方法  枚举：TmsAsyncTaskMethodTypeEnum
         */
        private String methodType;
        private String methodTypeName;

    }

//     /**
//     * 状态统计
//     */
//     @Data
//     @NoArgsConstructor
//     @AllArgsConstructor
//     public static class TaskDTO {
//
//         /**
//         *
//         */
//         private List<String> ids;
//
//         /**
//          *核算日期
//          */
//         private String reportDate;
//         /**
//          * 主任务id
//          */
//         private String taskId;
//         /**
//          * 单据类型
//          */
//         private String businessType;
//         /**
//          *物流标签类型
//          */
//         private String type;
//     }


    /**
     * 明细分页列表
     */
    @Data
    @NoArgsConstructor
    public static class DetailListDTO {
        /**
         * 主键id
         */
        private String id;
        /**
         * 明细id
         */
        private String detailId;
        /**
         * 单据id
         */
        private String businessId;
        /**
         * 业务单号
         */
        private String businessCode;
        /**
         * 错误原因
         */
        private String errorData;
    }


    /**
     *
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO {
        /**
         * 主键id
         */
        @NotBlank(message = "id不能为空")
        private String id;

        /**
         * 开始时间
         */
        @NotNull(message = "开始时间不能为空")
        private Long startTime;

    }

    /**
     * 手动创建异步任务入参
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ManualCreateDTO implements Serializable {

        /**
         * 单据类型
         */
        private String businessType;

        /**
         * 方法类型
         */
        private String methodType;

        /**
         * 预期明细数量（创建时直接写入）
         */
        private Integer detailCount;

        /**
         * 任务参数 JSON（PushParamsDTO 序列化）
         */
        private String dataJson;
    }

    /**
     * 自动创建异步任务入参
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AutoCreateDTO implements Serializable {

        /**
         * 单据类型
         */
        private String businessType;

        /**
         * 方法类型
         */
        private String methodType;

        /**
         * 任务参数 JSON（PushParamsDTO 序列化）
         */
        private String dataJson;

        /**
         * 任务开始时间 yyyy-MM-dd
         */
        private String startTimeStr;

        /**
         * 主任务执行超时时间（秒）。
         * 自动周期任务创建时应传入对账周期配置中的单据超时；为空时由 {@code addAutoTask} 回退批次配置。
         */
        private Integer execTimeout;
    }

    /**
     * TMS 异步任务信封。
     * 调度、重试和载荷路由字段放在信封层，业务参数放入 payloadJson。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskEnvelopeDTO implements Serializable {

        /**
         * 单据类型
         */
        private String businessType;

        /**
         * 方法类型：同一 business_type 下区分不同方法
         */
        private String methodType;

        /**
         * 重试模式：FAILED_ONLY 表示按来源任务失败明细分页执行
         */
        private String retryMode;

        /**
         * 错误重试来源任务
         */
        private String retrySourceTaskId;

        /**
         * 业务载荷类型，默认使用 businessType:methodType 约定。
         */
        private String payloadType;

        /**
         * 业务载荷版本，用于后续载荷结构演进。
         */
        private Integer payloadVersion;

        /**
         * 任务提交人 ID
         */
        private String operatorUserId;

        /**
         * 任务提交人名称
         */
        private String operatorUserName;

        /**
         * 业务载荷 JSON
         */
        private String payloadJson;
    }

    /**
     * 小包费用分摊下推载荷。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SmallBagPushAllocationPayloadDTO implements Serializable {

        /**
         * 核算日期
         */
        private String reportDate;

        /**
         * 费用类型：自发货/尾程
         */
        private String type;
    }

    /**
     * 头程费用分摊下推载荷。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FirstMilePushAllocationPayloadDTO implements Serializable {

        /**
         * 核算日期 yyyy-MM。
         */
        private String reportDate;
    }

    /**
     * 中转费用分摊下推载荷。
     * <p>
     * 仅持久化 B2C 报关对账审核日期查询范围；游标、批次大小和重试元数据由框架运行时注入，不写入 payload。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransferDeclarePushAllocationPayloadDTO implements Serializable {

        /**
         * B2C 报关对账审核日期范围起始（含）。
         */
        private LocalDate startDate;

        /**
         * B2C 报关对账审核日期范围结束（不含）。
         */
        private LocalDate endDate;
    }

    /**
     * 头程对账单下推载荷。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FirstMileReconciliationPushPayloadDTO implements Serializable {

        /**
         * 对账周期起始（含）。
         */
        private LocalDate startDate;

        /**
         * 对账周期结束（含）。
         */
        private LocalDate endDate;
    }

    /**
     * B2C 报关对账下推载荷。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class B2cDeclareReconciliationPushPayloadDTO implements Serializable {

        /**
         * 对账周期起始（含）。
         */
        private LocalDate startDate;

        /**
         * 对账周期结束（含）。
         */
        private LocalDate endDate;
    }

    /**
     * 头程下推分摊候选发货单游标查询参数。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FirstMilePushAllocationQueryDTO implements Serializable {

        /**
         * 游标 ID。
         */
        private String lastId;

        /**
         * 批次大小。
         */
        private Integer batchSize;
    }

    /**
     * 手动创建任务
     */
    @Data
    @NoArgsConstructor
    public static class PushParamsDTO {

        /**
         * ids
         */
        private List<String> ids;
        /**
         *核算日期
         */
        private String reportDate;
        /**
         * 主任务id
         */
        private String taskId;
        /**
         * 单据类型
         */
        private String businessType;
        /**
         *物流标签类型
         */
        private String type;

        /**
         * 方法类型：同一 business_type 下区分不同方法  枚举：TmsAsyncTaskMethodTypeEnum
         */
        private String methodType;

        /**
         * 错误重试来源任务：避免把大批量失败明细ID写入 dataJson
         */
        private String retrySourceTaskId;

        /**
         * 重试模式：FAILED_ONLY 表示按来源任务失败明细分页执行
         */
        private String retryMode;

        /**
         * 核算期间 yyyy-MM（按月处理时的过滤条件）
         */
        private String reportPeriodStr;

        /**
         * 目标核算状态（批量更新核算状态时使用）
         */
        private String reportStatus;

        /**
         * 目标对账状态（批量更新对账状态时使用）
         */
        private String reconciliationStatus;

        /**
         * 对账确认时间
         */
        private LocalDateTime confirmTime;

        /**
         * 页面高级查询生成的 SQL 条件
         */
        private Map<String, String> sqlMap;

        /**
         * 数据权限 SQL
         */
        private String permissionSql;

        /**
         * 任务提交人ID
         */
        private String operatorUserId;

        /**
         * 任务提交人名称
         */
        private String operatorUserName;

        /**
         * 对账状态：暂估确认
         */
        private String estimateConfirmStatus;

        /**
         * 对账状态：账单确认
         */
        private String confirmedStatus;

        /**
         * 对账状态：已作废
         */
        private String invalidStatus;

        /**
         * 对账状态：待确认
         */
        private String toBeConfirmStatus;

        /**
         * 核算状态：已生成
         */
        private String checkedCheckStatus;

        /**
         * 核算状态：待生成
         */
        private String checkingCheckStatus;

        /**
         * 支付类型：退款
         */
        private String refundPayType;

        /**
         * 支付状态：待付款/待退款
         */
        private String paymentPayStatus;

        /**
         *开始日期
         */
        private LocalDate startDate;
        /**
         *结束日期
         */
        private LocalDate endDate;

        /**
         *开始日期
         */
        private LocalDateTime startTime;
        /**
         *结束日期
         */
        private LocalDateTime endTime;

        /**
         * 游标分页：上一批最后一条记录的 id（首次传空字符串）
         * 仅在服务内部循环中使用，不随 MQ 消息体传递
         */
        private String lastId;

        /**
         * 游标分页：每批查询条数
         */
        private Integer batchSize;

    }

    @Data
    @NoArgsConstructor
    public static class CursorPageDTO {

        /**
         * 业务筛选 ID 集合，如物流商 ID 或待处理业务 ID
         */
        private List<String> ids;

        private LocalDate startDate;

        private LocalDate endDate;

        private LocalDateTime startTime;

        private LocalDateTime endTime;

        /**
         * 游标分页：上一批最后一条业务 ID
         */
        private String lastId;

        /**
         * 每批查询条数
         */
        private Integer batchSize;

        private String orderType;

        private String reconciliationStatus;

        private String trackStatus;
    }

    /**
     * 内部类：批次处理结果
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BatchProcessResult {

        private int successCount;

        private int failedCount;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO extends SuperDTO {

        /**
         * 任务id
         */
        @NotBlank(message = "任务id不能为空")
        @Size(max = 32, message = "任务id最大长度不能超过32位")
        private String code;

        /**
         * 执行系统
         */
        @NotBlank(message = "执行系统不能为空")
        @Size(max = 32, message = "执行系统最大长度不能超过32位")
        private String sysModule;

        /**
         * 单据名称
         */
        @NotBlank(message = "单据名称不能为空")
        @Size(max = 50, message = "单据名称最大长度不能超过50位")
        private String businessType;

        /**
         * 开始时间
         */
        @NotNull(message = "开始时间不能为空")
        private LocalDateTime startTime;

        /**
         * 结束时间
         */
        @NotNull(message = "结束时间不能为空")
        private LocalDateTime endTime;

        /**
         * 任务超时时间 单位：秒
         */
        private Integer execTimeout;

        /**
         * 重试次数
         */
        private Integer retryTimes;

        /**
         * 状态：pending=待执行,ing=进行中, finish=已完成, failed=失败
         */
        @NotBlank(message = "状态不能为空")
        private String status;

        /**
         * json
         */
        private String dataJson;

        /**
         * 错误信息
         */
        @Size(max = 500, message = "错误信息最大长度不能超过500位")
        private String errorData;

        /**
         * 执行数量
         */
        private Integer detailCount;

        /**
         * 错误数量
         */
        private Integer errorCount;

        /**
         * 执行类型：auto=自动, manual=手动
         */
        private String execType;
    }


    /**
     * 自动周期任务生成单项结果。
     * <p>
     * {@link #status} 取值见本类 {@code STATUS_*} 常量。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenAutoTaskItemResult implements Serializable {

        /** 任务已创建 */
        public static final String STATUS_CREATED = "CREATED";
        /** 同调度日任务已存在，跳过创建 */
        public static final String STATUS_SKIPPED_EXISTS = "SKIPPED_EXISTS";
        /** 配置的生成类型当前不支持 */
        public static final String STATUS_SKIPPED_UNSUPPORTED = "SKIPPED_UNSUPPORTED";
        /** 生成或保存失败 */
        public static final String STATUS_FAILED = "FAILED";

        /** 展示名称，如「头程对账单」「小包费用分摊-自配送」 */
        private String taskName;
        /** 业务类型 */
        private String businessType;
        /** 方法类型 */
        private String methodType;
        /** 调度日，格式 yyyy-MM-dd，对应当月配置的生成日期 */
        private String startTimeStr;
        /** 创建成功时的任务主键；跳过时为空 */
        private String taskId;
        /** 单项执行状态，见 {@code STATUS_*} */
        private String status;
        /** 失败或不支持时的说明 */
        private String message;
    }

    /**
     * 自动周期任务生成汇总。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenAutoTaskResultDTO implements Serializable {

        /** 本次 genAutoTask 总耗时（毫秒） */
        private long durationMs;
        /** 各业务生成器的逐项结果 */
        private List<GenAutoTaskItemResult> items;
        /** {@link GenAutoTaskItemResult#STATUS_CREATED} 数量 */
        private int createdCount;
        /** 跳过数量（已存在 + 不支持） */
        private int skippedCount;
        /** {@link GenAutoTaskItemResult#STATUS_FAILED} 数量 */
        private int failedCount;
    }

    /**
     * 任务执行结果
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskResult {
        private String name;
        private boolean success;
        private String errorMsg;
        private long duration;

        public boolean isSuccess() {
            return success;
        }
    }


}