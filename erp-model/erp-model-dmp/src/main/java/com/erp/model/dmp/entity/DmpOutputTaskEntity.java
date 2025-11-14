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
 * 推送任务
 * </p>
 *
 * @author shukai
 * @since 2024-07-01
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_output_task")
public class DmpOutputTaskEntity extends BaseEntity<DmpOutputTaskEntity> {

    /**
    * 推送数据配置id
    */
    @TableField("cfg_output_id")
    private String cfgOutputId;
    /**
    * 推送下一层级id
    */
    @TableField("next_level_id")
    private String nextLevelId;
    /**
    * 推送接口条件的开始时间
    */
    @TableField("start_time")
    private LocalDateTime startTime;
    /**
    * 推送接口条件的结束时间
    */
    @TableField("end_time")
    private LocalDateTime endTime;
    /**
    * 推送状态：init=待推送,finish=已推送,error=推送失败  枚举：DmpOutputTaskStatusEnum
    */
    @TableField("status")
    private String status;

    /**
     * 推送类型：  枚举：DmpOutputTaskTypeEnum
     */
     @TableField("task_type")
     private String taskType;
     
     /**
      * 错误次数
      */
     @TableField("error_count")
     private Integer errorCount;
     
      /**
       * 输入任务id
       */
     @TableField("input_task_id")
     private String inputTaskId;
     
     /**
      * 执行超时时间，单位秒
      */
     @TableField("exec_timeout")
     private Integer execTimeout;
      
      /**
       * 异常原因
       */
      @TableField("error_message")
      private String errorMessage;
      
      /**
       * 执行系统：DmpCfgInputExecSystemEnum 枚举
       */
      @TableField("exec_system")
      private String execSystem;

    public static final String CFG_OUTPUT_ID = "cfg_output_id";

    public static final String NEXT_LEVEL_ID = "next_level_id";

    public static final String START_TIME = "start_time";

    public static final String END_TIME = "end_time";

    public static final String STATUS = "status";

    public static final String REQUEST_DATA = "request_data";

    public static final String RESPONSE_DATA = "response_data";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
