package com.erp.model.plm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Classname TaskPagingShowDTO
 * @Date 2022-09-21 15:37
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class TaskPagingShowDTO  implements Serializable {


    /**
     * 任务id
     */
    private String id;

    /**
     * 产品id
     */
    private String productId;



    /**
     * 负责人id
     */
    private String chargeId;


    /**
     * 阶段id
     */
    private String phaseId;


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

    private Date planStartTime;

    /**
     * 计划结束时间
     */

    private Date planEndTime;



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
     * 任务状态 任务状态 0:待发布 1:未开始 2:进行中 3 已完成, 4.完成待确认 5.审核中  6 审核通过 7 审核不通过
     */
    private Integer status;


    /**
     * 负责人
     */
    private String chargeName;

    /**
     * 是否是系统任务
     * true 是 false 不是
     */
    private Boolean isSysTask=false;

    /**
     * 引用的系统任务id
     *
     */
    private String quoteSysTaskId;

    /**
     * 父级id
     *
     */
    private String pid;


    /**
     * 子 任务
     */
    @JsonInclude(value= JsonInclude.Include.NON_EMPTY)
    private List<TaskPagingShowDTO>  childList;


}
