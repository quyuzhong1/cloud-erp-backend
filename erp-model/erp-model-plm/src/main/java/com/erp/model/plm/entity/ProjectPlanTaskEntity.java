package com.erp.model.plm.entity;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.annotation.*;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.plm.dto.PlanTaskNameDTO;
import com.erp.model.plm.dto.ProjectPlanTaskDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;

import static com.common.core.utils.date.LocalDateUtil.countDaysForLocalDate;

/**
 * 项目计划任务表(ProjectPlanTask)实体类
 *
 * @author yl
 * @since 2023-02-03 14:45:44
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("project_plan_task")
@NoArgsConstructor
public class ProjectPlanTaskEntity implements Serializable {
    private static final long serialVersionUID = 184565397899617521L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;
    /**
     * 创建人id
     */
    @TableField(value = "create_user_id", fill = FieldFill.INSERT)
    private String createUserId;
    /**
     * 创建名
     */
    @TableField(value = "create_user_name", fill = FieldFill.INSERT)
    private String createUserName;
    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;
    /**
     * 更改人
     */
    @TableField(value = "update_user_id", fill = FieldFill.INSERT_UPDATE)
    private String updateUserId;
    /**
     * 更改人名
     */
    @TableField(value = "update_user_name", fill = FieldFill.INSERT_UPDATE)
    private String updateUserName;
    /**
     * 更改时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
    /**
     * 产品id
     */
    private String productId;
    /**
     * 任务id
     */
    private String taskId;
    /**
     * 源计划开始时间
     */
    private Date originStartTime;
    /**
     * 源计划结束时间
     */
    private Date originEndTime;
    /**
     * 变更后计划开始时间
     */
    private Date changeStartTime;
    /**
     * 变更后计划结束时间
     */
    private Date changeEndTime;
    /**
     * 项目变更表id
     */
    private String projectPlanId;

    /**
     * 源负责人id
     */
    private String originChargeId;

    /**
     * 变更负责人
     */
    private String changeChargeId;

    @TableLogic
    private Boolean isDeleted;


    /**
     * 是否重启
     */
    private Boolean isRestart;
    /**
     * 工期
     */
    @TableField("work_period")
    private Integer workPeriod;

    public ProjectPlanTaskEntity(String taskId, LocalDate startDate, Integer workPeriod, List<LocalDate> dateList, Integer type) {
        this.id = taskId;
        LocalDate endDate = startDate;
        while (workPeriod > 0){
            if(!dateList.contains(startDate.plusDays(1))){
                endDate = startDate.plusDays(1);
                workPeriod --;
            }
        }
        if(1 == type){
            this.originStartTime = LocalDateUtil.localDate2Date(startDate);
            this.originStartTime = LocalDateUtil.localDate2Date(endDate);
        }else {
            this.changeStartTime = LocalDateUtil.localDate2Date(startDate);
            this.changeEndTime = LocalDateUtil.localDate2Date(endDate);
        }
        this.workPeriod = workPeriod;
    }
}

