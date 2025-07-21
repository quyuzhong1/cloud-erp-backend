package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * etl任务
 * </p>
 *
 * @author shukai
 * @since 2025-07-21
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_etl_task")
public class DmpEtlTaskEntity extends BaseEntity<DmpEtlTaskEntity> {

    /**
    * 拉取数据配置id
    */
    @TableField("cfg_etl_id")
    private String cfgEtlId;
    /**
    * 执行条件的开始时间
    */
    @TableField("start_time")
    private LocalDateTime startTime;
    /**
    * 执行条件的结束时间
    */
    @TableField("end_time")
    private LocalDateTime endTime;
    /**
    * 状态：init=待执行，finish=完成，error=异常  枚举：DmpEtlTaskStatusEnum
    */
    @TableField("status")
    private String status;
    /**
    * 异常原因
    */
    @TableField("error_message")
    private String errorMessage;
    /**
    * 错误次数
    */
    @TableField("error_count")
    private Integer errorCount;
    /**
    * 执行超时时间，单位秒
    */
    @TableField("exec_timeout")
    private Integer execTimeout;
    /**
    * 扩展字段
    */
    @TableField("extend_json")
    private String extendJson;
    /**
    * 下次执行任务时间
    */
    @TableField("next_exec_time")
    private LocalDateTime nextExecTime;
    /**
    * 实例id
    */
    @TableField("etl_instance_id")
    private String etlInstanceId;


    public static final String CFG_ETL_ID = "cfg_etl_id";

    public static final String START_TIME = "start_time";

    public static final String END_TIME = "end_time";

    public static final String STATUS = "status";

    public static final String ERROR_MESSAGE = "error_message";

    public static final String ERROR_COUNT = "error_count";

    public static final String EXEC_TIMEOUT = "exec_timeout";

    public static final String EXTEND_JSON = "extend_json";

    public static final String NEXT_EXEC_TIME = "next_exec_time";

    public static final String ETL_INSTANCE_ID = "etl_instance_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
