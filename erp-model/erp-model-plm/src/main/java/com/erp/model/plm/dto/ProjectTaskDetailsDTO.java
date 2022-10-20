package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Classname ProjectTaskDetailsDTO
 * @Description TODO
 * @Date 2022-10-11 9:16
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ProjectTaskDetailsDTO implements Serializable {


    /**
     * 任务id
     */
    private String taskId;

    /**
     * 产品id
     */
    private String productId;

    /**
     * 产品经理id
     */
    private String productChargeId;

    /**
     * 产品经理
     */
    private String productChargeName;

    /**
     * 项目负责人id
     */
    private String projectChargeId;

    /**
     * 项目负人
     */
    private String projectChargeName;


    /**
     * 任务负责人id
     */
    private String taskChargeId;

    /**
     * 任务负责人
     */
    private String taskChargeName;


    /**
     * 任务名
     */
    private String taskName;


    /**
     * 任务状态
     * 任务状态 0:待发布 1:待开始
     * 2:待审核  3:进行中
     * 4 已完成, 5 已关闭
     * 6.完成待审核 7.审核中
     * 8 审核通过  9 审核不通过
     */
    private Integer taskState;

    /**
     * 任务名
     */
    private String taskDescription;


    /**
     * 任务类型 0 一般任务 1：审核任务
     */
    private String type;

    /**
     * 任务优先级 1 低级 2 中级 3 高级
     */
    private String priority;

    /**
     * 计划开始时间
     */
    private Date planStartTime;

    /**
     * 计划结束时间
     */
    private Date planEndTime;

    /**
     * 计划时间
     */
    private String planTime;


    /**
     * 实际开始时间
     */
    private Date realityStartTime;

    /**
     * 实际结束时间
     */
    private Date realityEndTime;

    /**
     * 实际时间
     */
    private String realityTime;

    /**
     * 产品名称
     */
    private String productName;


    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 任务阶段
     */
    private String phaseName;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建人
     */
    private String createUserName;


    /**
     * 前置任务
     */
    private List<RefTaskInfoDTO> preTasks;

    /**
     * 子 任务
     */
    private List<RefTaskInfoDTO> childTasks;

    /**
     * 输出文档 列表
     */
    private List<DeliveryDocsDTO> outputDocsList;


    /**
     * 变更文档流程状态
     * 0 没有
     * 2:待审核
     * 7.审核中
     * 8 审核通过
     */
    private Integer changeDocsProcessState=0;

    private String businessProcessId;


}
