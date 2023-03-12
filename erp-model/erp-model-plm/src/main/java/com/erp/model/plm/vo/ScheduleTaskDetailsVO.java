package com.erp.model.plm.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @Classname ScheduleTaskDetailsVO
 * @Description TODO
 * @Date 2023-02-09 16:58
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ScheduleTaskDetailsVO implements Serializable {

    /**
     * 任务id
     */
    private String taskId;

    /**
     * 任务名
     */
    private String taskName;


    /**
     * 任务负责人id
     */
    private String chargeId;

    /**
     * 任务负责人id
     */
    private String chargeName;

    /**
     * 计划开始时间
     */
    private LocalDateTime planStartTime;

    /**
     * 计划结束时间
     */
    private LocalDateTime planEndTime;


    private List<ScheduleTaskDetailsVO> historyList;


    /**
     * 交付文档名称
     */
    private String deliveryDocsNames;

    /**
     * 前端要求id
     */
    private String id;

    /**
     * 工期
     */
    private Integer workPeriod;
    /**
     * 前置任务关系列表
     */
    private List<String> preTaskIdList;

}
