package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * @Author Cloud
 * @Date 2023/2/27 21:03
 **/

@Data
@NoArgsConstructor
public class PlanTaskNameDTO {

    /**
     * 计划id
     */
    private String id;

    /**
     * 任务id
     */
    private String taskId;

    /**
     * 源计划开始时间
     */
    private LocalDate originStartTime;
    /**
     * 源计划结束时间
     */
    private LocalDate originEndTime;
    /**
     * 变更后计划开始时间
     */
    private LocalDate changeStartTime;
    /**
     * 变更后计划结束时间
     */
    private LocalDate changeEndTime;

    /**
     * 计划任务
     */
    private String taskName;
    /**
     * 任务属性 1： 立项任务  2：项目任务
     */
    private Integer property;
    /**
     * 任务状态 0:待发布 1:待开始
     * 2:待审核  3:进行中 4 已完成, 5 已关闭   6.完成待审核 7.审核中  8 审核通过  9 审核不通过 ,10 部分完成
     */

    private Integer status;
    /**
     * 状态排期状态  waitSubmit 待提交  waitAudit 待审核   auditIng 审核中 auditNoPass 审核不通过 auditPass审核通过
     */

    private String scheduleStatus;
    /**
     * 排期类型 change 变更 initial 初始化
     */
    private String scheduleType;
    /**
     * 工期
     */
    private Integer workPeriod;



}
