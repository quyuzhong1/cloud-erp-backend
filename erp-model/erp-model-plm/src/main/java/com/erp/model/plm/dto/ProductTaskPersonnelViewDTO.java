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
public class ProductTaskPersonnelViewDTO implements Serializable {

    /**
     * 人员名称（分组条件）
     */
    private String name;

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
     * 任务状态
     */
    private String statusName;

    /**
     * 是否存在下级（0否，1是）
     */
    private Integer isSubordinate;

}
