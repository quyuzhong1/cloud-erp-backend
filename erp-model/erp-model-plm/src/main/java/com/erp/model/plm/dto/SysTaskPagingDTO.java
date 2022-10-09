package com.erp.model.plm.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.erp.common.annotation.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * @Classname SysTaskPagingDTO
 * @Description TODO
 * @Date 2022-09-15 18:54
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SysTaskPagingDTO implements Serializable {

    /**
     * 任务id
     */
    private String id;

    /**
     * 任务名
     */
    @NotBlank(message = "任务名不能为空")
    private String name;

    /**
     * 任务类型 0 一般任务 1：审核任务
     */
    @StateEnumValue(intValues = {0, 1}, message = "任务类型只能是0或者1")
    private Integer type;

    /**
     * 负责人id
     */
    @NotBlank(message = "负责人id 不能为空")
    private String chargeId;

    /**
     * 负责人名
     */
    @NotBlank(message = "负责人不能为空")
    private String chargeName;

    /**
     * 前置任务id
     */
    private String preTaskId;

    /**
     * 前置任务id
     */
    private String preTaskName;

    /**
     * 计划开始时间
     */
    @NotNull(message = "计划开始时间不能为空")
    private Date planStartTime;

    /**
     * j计划结束时间
     */
    @NotNull(message = "计划结束时间不能为空")
    private Date planEndTime;

    /**
     * 任务优先级 1 低级 2 中级 3 高级
     */
    @StateEnumValue(intValues = {1, 2, 3}, message = "任务优先级有误")
    private Integer priority;

    /**
     * 任务阶段id
     */
    private String phaseId;

    /**
     * 是否是固定任务 1 是  0 不是
     */
    private Integer isFixed;

    /**
     * 任务描述
     */
    private String description;



    /**
     * 文档名称
     */
    private String docsNames;



    /**
     * 创建人id
     */
    private String createUserId;


    /**
     * 创建人名
     */
    private String createUserName;

    /**
     * 创建时间
     */
    private Date createTime;


    /**
     * 流程id
     */
    private String processId;

    /**
     * 阶段名
     */
    private String phaseName;

}
