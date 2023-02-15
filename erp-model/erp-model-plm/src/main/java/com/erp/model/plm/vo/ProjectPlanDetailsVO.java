package com.erp.model.plm.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Classname ProjectPlanDE
 * @Description TODO
 * @Date 2023-02-09 16:42
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ProjectPlanDetailsVO implements Serializable {

    /**
     * 产品id
     */
    private String productId;


    /**
     * 产品名称
     */
    private String productName;

    /**
     * 总的任务数
     */
    private Integer totalTaskCount;


    /**
     * 待审核任务数
     */
    private Integer waitAuditTaskCount;

    /**
     * 排期开始时间
     */
    private Date scheduleStartTine;

    /**
     * 排期结束时间
     */
    private Date scheduleEndTine;

    /**
     * 总时长
     */
    private Integer durationDay;


    /**
     * 任务
     */
    private List<ScheduleTaskDetailsVO> taskList;

}
