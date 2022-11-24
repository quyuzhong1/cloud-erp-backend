package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author Will
 * @version 1.0
 * @description: 任务视图查询返回DTO
 * @date 2022/11/23 15:34
 */
@Data
@NoArgsConstructor
public class ProductTaskViewDTO {

    /**
     * 产品id
     */
    private String productId;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 计划开始日期
     */
    private Date planStartTime;

    /**
     * 计划结束日期
     */
    private Date planEndTime;

    /**
     * 实际开始日期
     */
    private Date realityStartTime;

    /**
     * 实际结束日期
     */
    private Date realityEndTime;

    /**
     * 任务状态名称
     */
    private String statusName;

    /**
     * 任务状态
     */
    private Integer status;

    /**
     * 人员名称
     */
    private String chargeId;

    /**
     * 人员名称
     */
    private String chargeName;

    /**
     * 阶段id
     */
    private String phaseId;

    /**
     * 阶段名称
     */
    private String phaseName;

    /**
     * 是否延期(0否，1是)
     */
    private Integer isDelay;
}
