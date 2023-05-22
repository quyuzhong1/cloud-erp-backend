package com.erp.model.plm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @Classname TaskPagingShowDTO
 * @Date 2022-09-21 15:37
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class TaskPagingShowDTO implements Serializable {


    /**
     * 任务id
     */
    private String id;

    /**
     * spu编号
     */
    private String spuNo;

    /**
     * 产品id
     */
    private String productId;

    /**
     * 产品名
     */
    private String productName="";


    /**
     * 负责人id
     */
    private String chargeId;


    /**
     * 负责人id集合
     */
    private List<String> chargeIdList;





    /**
     * 阶段id
     */
    private String phaseId;


    /**
     * 阶段名称
     */
    private String phaseName;


    /**
     * 任务名
     */
    private String name;


    /**
     * 任务类型 0 一般任务 1：审核任务
     */
    private Integer type;

    /**
     * 计划开始时间
     */

    private LocalDateTime planStartTime;

    /**
     * 计划结束时间
     */

    private LocalDateTime planEndTime;


    /**
     * 预警
     */
    private String warning;


    /**
     * 总的文档数
     */
    private Integer totalDocsCount;


    /**
     * 已完成文档数
     */
    private Integer finishDocsCount;

    /**
     * 任务优先级 1 低级 2 中级 3 高级
     */
    private Integer priority;

    /**
     * 任务状态 任务状态 0:待发布 1:未开始 2:待审核 3 进行中, 4.已完成 5.已关闭
     * 6 完成待审核 7 审核中   8 审核通过  9 审核不通过
     */
    private Integer status;


    /**
     * 排期状态
     * 状态排期状态
     * waitSubmit 待提交
     * waitAudit 待审核
     * auditIng 审核中
     * auditNoPass 审核不通过
     * auditPass审核通过
     *
     */
    private String scheduleStatus;

    /**
     * 状态名
     */
    private String statusName;



    /**
     * 负责人
     */
    private String chargeName;

    /**
     * 是否是系统任务
     * true 是 false 不是
     */
    private Boolean isSysTask = false;

    /**
     * 引用的系统任务id
     */
    private String quoteSysTaskId;

    /**
     * 父级id
     */
    private String pid;


    /**
     * 流程id
     */
    private String processId;


    /**
     * 流程任务id
     */
    private String processTaskId = "";


    /**
     * 总的前置任务数
     */
    private Integer totalPreTaskCount=0;

    /**
     * 完成的前置任务数
     */
    private Integer  finishPreTaskCount=0;

    /**
     * 字段配置类型 createSku 创造sku，fillProductInfo 填写信息
     */
    private String taskFieldConfigType="";
    

    private List<Map<String,Object>> operateList;


    /**
     * 子 任务
     */
    @JsonInclude(value = JsonInclude.Include.NON_EMPTY)
    private List<TaskPagingShowDTO> childList;

    /**
     * 前置任务名
     */
    private List<String> preTaskNameList;


    /**
     * 能否编辑任务
     */
    private  Boolean ifEditTask;

    /**
     * 角色名称，由模板生成时带过来
     */
    private String roleName;

    /**
     * 分配类型，由模板生成时带过来（0角色，1人员）
     */
    private Integer distributionType;

    /**
     * 是否是固定任务 1是  0  不是
     */
    private Integer isFixed;

    /**
     * 关联类型
     */
    private String relatedSkuType;

    /**
     * 工期
     */
    private Integer workPeriod;
}
