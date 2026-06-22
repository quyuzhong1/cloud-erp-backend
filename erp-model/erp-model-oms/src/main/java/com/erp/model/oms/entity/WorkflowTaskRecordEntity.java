package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 任务节点记录表
 * </p>
 *
 * @author jack
 * @since 2025-09-16
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("workflow_task_record")
public class WorkflowTaskRecordEntity extends BaseEntity<WorkflowTaskRecordEntity> {

    /**
    * 来源类型 WorkflowTaskRecordTypeEnum
    */
    @TableField("source_type")
    private String sourceType;
    /**
    * 来源id
    */
    @TableField("source_id")
    private String sourceId;
    /**
    * 来源单号
    */
    @TableField("source_code")
    private String sourceCode;
    /**
    * 下一个处理节点id
    */
    @TableField("index")
    private Integer index;
    /**
    * 处理类全路径
    */
    @TableField("class_path")
    private String classPath;
    /**
    * 状态：pending =待执行 , 执行中 =processing , 成功=success ,失败=failed  枚举：WorkflowTaskRecordStatusEnum
    */
    @TableField("status")
    private String status;
    /**
    * 当前重试次数
    */
    @TableField("retry_count")
    private Integer retryCount;
    /**
    * 错误信息
    */
    @TableField("last_error")
    private String lastError;
    /**
     * 输入数据
     */
    @TableField("input_data")
    private String inputData;
    /**
    * 输出数据
    */
    @TableField("output_data")
    private String outputData;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 节点id
    */
    @TableField("dict_basic_id")
    private String dictBasicId;
    /**
     * trace_id
     */
    @TableField("trace_id")
    private String traceId;

    /**
     * 编排实例 id
     */
    @TableField("instance_id")
    private String instanceId;

    /**
     * 目标服务编码 oms/wms/tms
     */
    @TableField("target_service")
    private String targetService;

    /**
     * 目标接口标识
     */
    @TableField("target_endpoint")
    private String targetEndpoint;

    /**
     * 跨服务调用耗时毫秒
     */
    @TableField("feign_duration_ms")
    private Long feignDurationMs;

    /**
     * 错误来源 orchestrator / remote
     */
    @TableField("error_source")
    private String errorSource;

    /**
     * 节点开始执行时间
     */
    @TableField("started_at")
    private java.time.LocalDateTime startedAt;

    /**
     * 节点结束执行时间
     */
    @TableField("finished_at")
    private java.time.LocalDateTime finishedAt;


    public static final String SOURCE_ID = "source_id";

    public static final String NEXT_ID = "next_id";

    public static final String CLASS_PATH = "class_path";

    public static final String STATUS = "status";

    public static final String RETRY_COUNT = "retry_count";

    public static final String LAST_ERROR = "last_error";

    public static final String OUTPUT_DATA = "output_data";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
