package com.cloud.erp.workflow.modules.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author yl
 * @since 2022-08-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("act_history_activity")
public class ActHistoryActivityEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 流程id
     */
    @TableField("process_instance_id")
    private String processInstanceId;

    /**
     * 活动id
     */
    @TableField("activity_id")
    private String activityId;

    /**
     * 上一个活动id
     */
    @TableField("pre_activity_id")
    private String preActivityId;

    /**
     * 下一个活动id
     */
    @TableField("next_activity_id")
    private String nextActivityId;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;


}
