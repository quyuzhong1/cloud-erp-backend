package com.erp.model.plm.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 产品项目计划
 *
 * @Classname
 * @Description TODO
 * @Date 2023-02-03 15:48
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ProductItemScheduleVO implements Serializable {

    /**
     * 总任务数
     */
    private Integer totalTaskCount;

    /**
     * 已排期的任务数
     */
    private Integer scheduleTaskCount;


    /**
     * 未排期任务数
     */
    private Integer unscheduledTaskCount;


    /**
     * 审核的任务数
     */
    private Integer scheduleAuditTaskCount;


    private List<ProductTaskVO> taskList;
}
