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
 * 拉取任务
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_input_task")
public class DmpInputTaskEntity extends BaseEntity<DmpInputTaskEntity> {

    /**
    * 拉取数据配置id
    */
    @TableField("cfg_input_id")
    private String cfgInputId;
    /**
     * 下一层级id
     */
     @TableField("next_level_id")
     private String nextLevelId;
    /**
    * 拉取接口条件的开始时间
    */
    @TableField("start_time")
    private LocalDateTime startTime;
    /**
    * 拉取接口条件的结束时间
    */
    @TableField("end_time")
    private LocalDateTime endTime;
    /**
    * 状态：init=待拉取，fds=上传fds，mongo=保存mongo，dmp=保存dmp，finish=完成，error=异常  枚举：DmpInputTaskStatusEnum
    */
    @TableField("status")
    private String status;
    /**
    * 异常原因
    */
    @TableField("error_message")
    private String errorMessage;
    /**
    * 任务类型：normal=正常任务，history=历史任务，hotfix=及时任务  枚举：DmpInputTaskTaskTypeEnum
    */
    @TableField("task_type")
    private String taskType;
    
    /**
     * 错误次数
     */
     @TableField("error_count")
    private Integer errorCount;
    
     /**
      * 父类任务id
      */
    @TableField("parent_task_id")
    private String parentTaskId;
    
    /**
     * 扩展json
     */
     @TableField("extend_json")
     private String extendJson;
    
    /**
     * 执行超时时间，单位秒
     */
     @TableField("exec_timeout")
     private Integer execTimeout;
     
     /**
      * 下次执行任务时间
      */
     @TableField("next_exec_time")
     private LocalDateTime nextExecTime;

    public static final String INPUT_DETAIL_ID = "input_detail_id";

    public static final String START_TIME = "start_time";

    public static final String END_TIME = "end_time";

    public static final String STATUS = "status";

    public static final String ERROR_MESSAGE = "error_message";

    public static final String TASK_TYPE = "task_type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
