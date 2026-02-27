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
    * 单据名称
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
    * 任务超时时间 单位：秒
    */
    @TableField("retry_times")
    private Integer retryTimes;
    /**
    * 状态：pending=待执行,ing=进行中,finish=已完成,  failed=失败  枚举：AsyncTaskRecordStatusEnum
    */
    @TableField("status")
    private String status;
    /**
    * json
    */
    @TableField("data_json")
    private String dataJson;
    /**
     *错误信息
     */
    @TableField("error_data")
    private String errorData;
    /**
     *明细任务数量
     */
    @TableField("detail_count")
    private Integer detailCount;
    /**
     *错误数量
     */
    @TableField("error_count")
    private Integer errorCount;
    /**
     *执行类型：auto=自动, manual=手动
     */
    @TableField("exec_type")
    private String execType;
    /**
     * 是否已重试
     */
    @TableField("is_retry")
    private Boolean isRetry;

    public static final String BUSINESS_TYPE = "business_type";

    public static final String START_TIME = "start_time";

    public static final String END_TIME = "end_time";

    public static final String STATUS = "status";

    public static final String DATA_JSON = "data_json";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
