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
     *  产品经理id
     */
    private String productChargeId;

    /**
     *  产品经理
     */
    private String productChargeName;


    /**
     *  任务负责人id
     */
    private String taskChargeId;

    /**
     *  任务负责人
     */
    private String taskChargeName;


    /**
     *  任务名
     */
    private String taskName;


    /**
     * 任务类型 0 一般任务 1：审核任务
     *
     */
    private String type;

    /**
     * 任务优先级 1 低级 2 中级 3 高级
     *
     */
    private String priority;

    /**
     * 计划开始时间
     *
     */
    private Date planStartTime;

    /**
     * 计划结束时间
     *
     */
    private Date planEndTime;

    /**
     * 产品名称
     *
     */
    private String productName;


    /**
     * 创建时间
     *
     */
    private Date createTime;

    /**
     * 任务阶段
     *
     */
    private String phaseName;

    /**
     * 更新时间
     *
     */
    private Date updateTime;

    /**
     * 创建人
     *
     */
    private String createUserName;


    /**
     * 前置任务
     */
    private RefTaskInfoDTO preTask;

    /**
     * 子 任务
     */
    private RefTaskInfoDTO childTask;

    /**
     * 输出文档 列表
     *
     */
    private List<DeliveryDocsDTO> outputDocsList ;



}
