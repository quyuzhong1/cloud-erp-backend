package com.erp.model.plm.dto;

import com.common.core.anno.StateEnumValue;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Classname SysTaskDTO
 * @Description TODO
 * @Date 2022-09-15 16:55
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SysTaskDTO implements Serializable {


    private String id;

    /**
     * 任务名
     */
    @NotBlank(message = "任务名不能为空")
    @Size(max = 50, message = "任务名最大50字符")
    private String name;

    /**
     * 任务类型 0 一般任务 1：审核任务
     */
    @StateEnumValue(intValues = {0, 1}, message = "任务类型只能是0或者1")
    private Integer type;


    /**
     * 分配类型（0角色，1人员）
     */
    @NotNull(message = "分配类型不能为空")
    private Integer distributionType;

    /**
     * 负责人id
     */
    private List<String> chargeIds;

    /**
     * 角色id
     */
    private List<String> roleIds;

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
    private Integer priority;

    /**
     * 任务阶段id
     */
    private String phaseId;

    /**
     * 任务阶段名
     */
    private String phaseName;

    /**
     * 是否是固定任务 1 是  0 不是
     */
    private Integer isFixed;

    /**
     * 任务描述
     */
    private String description;

    /**
     * 交付文档审核分配人集合
     */
    private List<TaskChargeDistributionDTO> approvalList;

    /**
     * 业务流程表id
     */
    private String businessProcessId;

    /**
     * 关联sku类型,RelatedSkuTypeEnum枚举(1，自动关联，2选择关联，3不关联)
     */
    private String relatedSkuType;

    /**
     * 业务流程名
     */
    private String businessName = "";

    private List<DocsDTO> deliveryDocsList;

    /**
     * 字段配置类型 createSku 创造sku，fillProductInfo 填写信息
     */
    //   @NotBlank(message = "字段配置类型 不能为空")
    //  @StateEnumValue(strValues = {"createSku","fillProductInfo"}, message = "字段配置类型有误")
    private String fieldConfigType;


    /**
     * 勾选字段后的json 字段
     */
    private String fieldJson;


    /**
     * 模板id
     */
    @NotBlank(message = "模板id不能为空")
    private String templateId;


}
