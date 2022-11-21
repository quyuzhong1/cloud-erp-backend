package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Will
 * @version 1.0
 * @description: 模板任务列表展示DTO
 * @date 2022/11/16 9:55
 */
@Data
@NoArgsConstructor
public class TemplateTaskShowDTO implements Serializable {


    /**
     * 模板表id
     */
    private String templateId;

    /**
     * 任务id
     */
    private String id;

    /**
     * 任务名
     */
    private String name;

    /**
     * 任务类型 0 一般任务 1：审核任务
     */
    private Integer type;

    /**
     * 负责人id
     */
    private String chargeId;

    /**
     * 负责人名
     */
    private String chargeName;

    /**
     * 任务优先级 1 低级 2 中级 3 高级
     */
    private Integer priority;

    /**
     * 前置任务id
     */
    private String preTaskId;

    /**
     * 前置任务名称
     */
    private String preTaskName;

    /**
     * 目标交付文档
     */
    private String docsNames;

    /**
     * 是否是固定任务 1 是  0  不是
     */
    private Integer isFixed;

    /**
     * 任务描述
     */
    private String description;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 创建人
     */
    private String createUserName;

}
