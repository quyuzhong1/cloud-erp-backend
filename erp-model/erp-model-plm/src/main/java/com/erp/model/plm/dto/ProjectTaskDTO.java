package com.erp.model.plm.dto;

import com.erp.common.annotation.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Classname ProjectTaskDTO
 * @Description TODO
 * @Date 2022-09-22 15:15
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ProjectTaskDTO implements Serializable {



    /**
     * 项目id
     */
    @NotBlank(message = "项目id不能为空")
    private String projectId;


    /**
     * 父级id  保存子任务需要传
     */
    private String pid;

    /**
     * 项目id
     */
    @NotBlank(message = "产品id不能为空")
    private String productId;


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
    @NotNull(message = "负责人不能为空")
    private List<ProjectMemberDTO> chargeList;

    /**
     * 前置任务id
     */
    private String preTaskId;

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
    @NotBlank(message = "阶段id 不能为空")
    private String phaseId;

    /**
     * 任务阶段名
     */
    @NotBlank(message = "阶段名 不能为空")
    private String phaseName;


    /**
     * 任务描述
     */
    private String description;

    /**
     *流程id 审核任务用到
     */
    private String processId;


    /**
     * 交付文档
     *
     */
    @Valid
    private List<DocsDTO>  deliveryDocsList;



}
