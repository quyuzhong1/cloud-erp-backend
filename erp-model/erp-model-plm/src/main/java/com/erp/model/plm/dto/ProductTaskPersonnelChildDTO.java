package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Will
 * @version 1.0
 * @description: 产品任务视图
 * @date 2022/11/22 18:09
 */
@Data
@NoArgsConstructor
public class ProductTaskPersonnelChildDTO implements Serializable {

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
     * 人员名称（分组条件）
     */
    private String chargeId;

    /**
     * 人员名称
     */
    private String chargeName;

    /**
     * 是否延期(0否，1是)
     */
    private Integer isDelay;
}
