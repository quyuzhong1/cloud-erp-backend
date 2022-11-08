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
     * name 任务名
     * priority 任务优先级
     * chargeId 负责人id
     * createTime 创建时间
     * status 状态
     */
    @StateEnumValue(strValues = {"name", "priority", "chargeId", "status",  "createTime"}, message = "搜索类型有误")
    private String searchType;
}
