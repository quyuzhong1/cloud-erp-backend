package com.erp.model.plm.dto;

import com.erp.common.annotation.StateEnumValue;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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
     * 任务id
     */
    private String id;

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
     * 产品idid
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
    @NotNull(message = "任务负责人集合不能为空")
    @Size(min = 1,message = "负责人至少有一个")
    private List<String> chargeIds;

    /**
     * 前置任务id
     */
    private List<String> preTaskIdList;

    /**
     * 计划开始时间
     */

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date planStartTime;

    /**
     * 计划结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date planEndTime;

    /**
     * 任务优先级 1 低级 2 中级 3 高级
     */
  //  @StateEnumValue(intValues = {1, 2, 3}, message = "任务优先级有误")
    private Integer priority;

    /**
     * 任务阶段id
     */
  //  @NotBlank(message = "阶段id 不能为空")
    private String phaseId;

    /**
     * 任务阶段名
     */
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
     * 业务流程表id
     */
    private String businessProcessId;

    /**
     * 业务流程名
     */
    private String businessName="";
    
    /**
     * 状态
     */
    private Integer status;



    /**
     * 交付文档
     *
     */
    @Valid
    private List<DocsDTO>  deliveryDocsList;



}
