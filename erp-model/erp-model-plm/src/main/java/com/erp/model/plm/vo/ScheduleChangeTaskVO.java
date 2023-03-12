package com.erp.model.plm.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @Classname ChangeTask
 * @Description TODO
 * @Date 2023-02-13 18:14
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ScheduleChangeTaskVO implements Serializable {


    private String taskId;


    private String taskName;

    /**
     * 任务负责人id
     */
    private String chargeId;

    /**
     * 任务负责人名
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

    /**
     * 产品id
     */
    private String productId;



    /*
     * 状态
     */
    private Integer status;

    /*
     * 任务状态名
     */
    private String statusName;
}
