package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;


/**
 * <p>
 * 异步任务记录
 * </p>
 *
 * @author jack
 * @since 2026-01-28
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("tms_async_task_record")
public class TmsAsyncTaskRecordEntity extends BaseEntity<TmsAsyncTaskRecordEntity> {

    /**
    * 任务id
    */
    @TableField("code")
    private String code;
    /**
    * 执行系统
    */
    @TableField("sys_module")
    private String sysModule;
    /**
    * 单据名称  TmsAsyncTaskRecordBusinessTypeEnum
    */
    @TableField("business_type")
    private String businessType;
    /**
    * 开始时间
    */
    @TableField("start_time")
    private LocalDateTime startTime;
    /**
    * 结束时间
    */
    @TableField("end_time")
    private LocalDateTime endTime;
    /**
    * 任务超时时间 单位：秒
    */
    @TableField("exec_timeout")
    private Integer execTimeout;
    /**
    * 剩余可重试次数
    */
    @TableField("retry_times")
    private Integer retryTimes;
    /**
    * 状态：pending=待执行,ing=进行中,finish=已完成,failed=失败  枚举：TmsAsyncTaskRecordStatusEnum
    */
    @TableField("status")
    private String status;
    /**
    * 主任务参数快照 JSON
    */
    @TableField("data_json")
    private String dataJson;
    /**
     * 错误信息
     */
    @TableField("error_data")
    private String errorData;
    /**
     * 预计明细任务数量
     */
    @TableField("detail_count")
    private Integer detailCount;
    /**
     * 失败明细数量
     */
    @TableField("error_count")
    private Integer errorCount;
    /**
     * 执行类型：auto=自动, manual=手动
     */
    @TableField("exec_type")
    private String execType;
    /**
     * 是否已重试（已废弃，新引擎任务不再读写此字段，保留列用于存量兼容）
     */
    @TableField("is_retry")
    private Boolean isRetry;
    /**
     * 方法类型：同一 business_type 下区分不同方法  枚举：TmsAsyncTaskMethodTypeEnum
     */
    @TableField("method_type")
    private String methodType;

    // -------------------------------------------------------
    // 新增字段（refactor-tms-async-task-engine）
    // -------------------------------------------------------

    /**
     * 任务防重键（MD5 hash of unique_key_text）；存量任务值为 'legacy' 不参与新引擎防重
     */
    @TableField("unique_key")
    private String uniqueKey;

    /**
     * 任务防重原文；存量任务值为 'legacy'
     */
    @TableField("unique_key_text")
    private String uniqueKeyText;

    /**
     * 计划执行时间；未设置时为 null，读取前必须判断非 null
     */
    @TableField("execute_time")
    private LocalDateTime executeTime;

    /**
     * MQ 派发成功时间；未派发时为 null，读取前必须判断非 null
     */
    @TableField("dispatch_time")
    private LocalDateTime dispatchTime;

    /**
     * 执行结果状态  TmsAsyncTaskResultStatusEnum：none/success/partial_failed/failed/timeout/canceled
     */
    @TableField("result_status")
    private String resultStatus;

    /**
     * 成功明细数量，随批处理增量累加
     */
    @TableField("success_count")
    private Integer successCount;

    /**
     * 最近心跳时间；任务未运行时为 null；补偿扫描需 IS NOT NULL AND < NOW() - threshold
     */
    @TableField("last_heartbeat_time")
    private LocalDateTime lastHeartbeatTime;

    /**
     * 重试根任务 ID；非重试任务值为自身 ID；用于全量重试链路追踪
     */
    @TableField("root_task_id")
    private String rootTaskId;

    /**
     * 直接来源任务 ID；非重试任务为空字符串；全量重试时填原任务 ID
     */
    @TableField("retry_source_task_id")
    private String retrySourceTaskId;

    /**
     * 重试模式：空字符串=非重试, full=全量重试, failed_only=错误明细重试
     */
    @TableField("retry_mode")
    private String retryMode;

    // -------------------------------------------------------
    // 列名常量
    // -------------------------------------------------------

    public static final String BUSINESS_TYPE = "business_type";
    public static final String METHOD_TYPE = "method_type";
    public static final String START_TIME = "start_time";
    public static final String END_TIME = "end_time";
    public static final String STATUS = "status";
    public static final String DATA_JSON = "data_json";
    public static final String RESULT_STATUS = "result_status";
    public static final String UNIQUE_KEY = "unique_key";
    public static final String ROOT_TASK_ID = "root_task_id";
    public static final String RETRY_SOURCE_TASK_ID = "retry_source_task_id";
    public static final String RETRY_MODE = "retry_mode";
    public static final String LAST_HEARTBEAT_TIME = "last_heartbeat_time";
    public static final String DISPATCH_TIME = "dispatch_time";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
