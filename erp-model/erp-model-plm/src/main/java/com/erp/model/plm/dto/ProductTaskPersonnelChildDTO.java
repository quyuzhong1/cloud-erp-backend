package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

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
     * id（用于前端展示）
     */
    private Integer id;

    /**
     * 父级id（用于前端展示）
     */
    private Integer parentId;

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
    private String planStartTime;

    /**
     * 计划结束日期
     */
    private String planEndTime;

    /**
     * 实际开始日期
     */
    private String realityStartTime;

    /**
     * 实际结束日期
     */
    private String realityEndTime;

    /**
     * 任务状态
     */
    private Integer status;

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
