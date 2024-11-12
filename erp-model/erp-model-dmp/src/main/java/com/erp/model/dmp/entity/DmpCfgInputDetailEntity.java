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
 * 外部系统接口明细
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_cfg_input_detail")
public class DmpCfgInputDetailEntity extends BaseEntity<DmpCfgInputDetailEntity> {

    /**
    * 输入信息id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 下一层级id
    */
    @TableField("next_level_id")
    private String nextLevelId;
    /**
    * 最后成功时间
    */
    @TableField("last_time")
    private LocalDateTime lastTime;
    /**
    * 下次执行结束时间
    */
    @TableField("next_time")
    private LocalDateTime nextTime;
    /**
    * 间隔时间长度单位秒
    */
    @TableField("interval_time")
    private Integer intervalTime;
    /**
    * 覆盖时间单位秒
    */
    @TableField("override_time")
    private Integer overrideTime;
    /**
    * 最大重试次数
    */
    @TableField("max_retry_count")
    private Integer maxRetryCount;
    /**
    * 执行超时时间，单位秒
    */
    @TableField("exec_timeout")
    private Integer execTimeout;
    /**
    * 是否禁用
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
    * 扩展json
    */
    @TableField("extend_json")
    private String extendJson;
    
    /**
     * 延迟时间，单位秒
     */
     @TableField("dealy_time")
     private Integer dealyTime;
     
     /**
      * 任务类型：normal=正常任务，history=历史任务，hotfix=及时任务  枚举：DmpInputTaskTaskTypeEnum
      */
     @TableField("task_type")
     private String taskType;

    /**
     * 最大间隔时间长度单位:秒, 0=按interval_time，-1=按当前时间-延迟时间
     */
    @TableField("max_interval_time")
    private Integer maxIntervalTime;


    public static final String MAIN_ID = "main_id";

    public static final String NEXT_LEVEL_ID = "next_level_id";

    public static final String LAST_TIME = "last_time";

    public static final String NEXT_TIME = "next_time";

    public static final String INTERVAL_TIME = "interval_time";

    public static final String OVERRIDE_TIME = "override_time";

    public static final String MAX_RETRY_COUNT = "max_retry_count";

    public static final String EXEC_TIMEOUT = "exec_timeout";

    public static final String DISABLED = "disabled";

    public static final String EXTEND_JSON = "extend_json";

    @Override
    public Serializable pkVal() {
        return null;
    }

}