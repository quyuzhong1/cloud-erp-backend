package com.erp.model.plm.dto;

import com.erp.common.annotation.StateEnumValue;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 以人为 维度 搜素条件
 *
 * @Classname TaskSearchParamDTO
 * @Description TODO
 * @Date 2022-11-07 17:57
 * @Created by yl
 */
@Data
public class TaskSearchParamDTO {

    /**
     * 搜索关键字
     */
    private String searchKeyword;


    /**
     * 任务属性
     */
    @NotBlank(message = "任务查找属性不能为空")
    @StateEnumValue(strValues = {"assignToMe", "myCreate", "all"}, message = "任务属性有误")
    private String taskProperty;

    /**
     * 任务条件
     * 1.待完成，待审核
     * 2 全部
     */
    private Integer taskCondition;


    /**
     * 搜索类型
     * priority 任务优秀级
     * plan_end_time
     * create_time 创建时间
     * reality_end_time 实际接受时间
     */
    @StateEnumValue(strValues = {"priority", "plan_end_time", "create_time", "reality_end_time" }, message = "搜索类型有误")
    private String searchType;

    /**
     * 排序
     */
    @StateEnumValue(strValues = {"desc","asc"}, message = "排序有误")
    private String orderBy;


    /**
     * 分组名 no 不分组
     * product 产品分组
     * planEndTime  计划结束时间
     */
    private String groupName;

    /**
     * 分组的标示 可能是时间 也可能是产品id

     */
    private String groupFlag;
}
