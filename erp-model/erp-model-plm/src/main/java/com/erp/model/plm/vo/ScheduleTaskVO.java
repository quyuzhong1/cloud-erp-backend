package com.erp.model.plm.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @Classname ScheduleTaskVO
 * @Description TODO
 * @Date 2023-02-08 17:48
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ScheduleTaskVO implements Serializable {

    /**
     * 任务id
     */
    private String taskId;


    /**
     * 任务负责人id
     */
    private String chargeId;

    /**
     * 计划开始时间
     */
    private Date planStartTime;

    /**
     * 计划结束时间
     */
    private Date planEndTime;


    /**
     * 产品id
     */
    private String productId;


}
