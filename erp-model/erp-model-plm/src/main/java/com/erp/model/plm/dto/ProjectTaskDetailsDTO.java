package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
    private LocalDateTime planStartTime;

    /**
     * 计划结束时间
     */
    private LocalDateTime planEndTime;

    /**
     * 计划时间
     */
    private String planTime;


    /**
     * 实际开始时间
     */
    private LocalDateTime realityStartTime;

    /**
     * 实际结束时间
     */
    private LocalDateTime realityEndTime;

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
    private LocalDateTime createTime;

    /**
     * 任务阶段
     */
    private String phaseName;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    private String createUserName;

    /**
     * 创建人
     */
    private String createUserId;

    /**
     * 关联sku类型,RelatedSkuTypeEnum枚举(1，自动关联，2选择关联，3不关联)
     */
    private String relatedSkuType;


    /**
     * 前置任务
     */
    private List<RefTaskInfoDTO> preTasks;

    /**
     * 子 任务
     */
    private List<RefTaskInfoDTO> childTasks;

    /**
     * 交付文档 列表
     */
    private List<DeliveryDocsDTO> outputDocsList;


    /**
     * 项目文档 列表
     */
    private List<DeliveryDocsDTO> productDocsList;

    /**
     * 变更文档流程状态
     */
    private String changeDocsProcessState="";

    private String businessProcessId;

    /**
     * 勾选字段后的json 字段
     */
    private String fieldJson;

    /**
     * 字段配置类型 createSku 创造sku，fillProductInfo 填写信息
     */
    private String fieldConfigType;



    /**
     * 关联sku 表id集合
     */
    private List<String> refSkuIdList;


    /**
     * 关联sku 表sku 名字集合
     */
    private List<String> refSkuNoList;


    /**
     * sku 完成信息
     */
    private List<Map<String,Object>> refSkuFinishList;


    private Integer planWorkTime;

    private Integer realWorkTime;

}
