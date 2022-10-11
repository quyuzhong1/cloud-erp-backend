package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** 关联任务
 * @Classname RefTaskInfoDto
 * @Description TODO
 * @Date 2022-10-11 9:34
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class RefTaskInfoDTO implements Serializable {

    /**
     * 任务名称
     */
    private String taskName;


    /**
     * 任务负责人
     */
    private String taskChargeName;

    /**
     * 任务类型 任务类型 0 一般任务 1：审核任务
     */
    private Integer taskType;


    /**
     * 任务状态  0:待发布 1:未开始 2:进行中 3 已完成, 4.完成待确认 5.审核中  6 审核通过 7 审核不通过
     */
    private Integer taskStatus;
}
