package com.erp.model.plm.dto;

import com.common.core.anno.StateEnumValue;
import com.erp.common.business.dto.base.SortDTO;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.Date;
import java.util.List;

/**
 * 以人为 维度 搜素条件
 *
 * @Classname TaskSearchParamDTO
 * @Description TODO
 * @Date 2022-11-07 17:57
 * @Created by yl
 */
@Data
public class TaskSearchParamDTO  extends SortDTO {

    /**
     * 搜索关键字
     */
    private String searchKeyword;


    /**
     * 任务属性
     * assignToMe 分配给我
     * myCreate 我创造
     * all 全部
     */
    @NotBlank(message = "任务查找属性不能为空")
    @StateEnumValue(strValues = {"assignToMe", "myCreate", "all"}, message = "任务属性有误")
    private String taskProperty="assignToMe";

    /**
     * 任务条件
     * 1.待完成
     * 2 全部
     * 3.待审核
     */
    private Integer taskCondition=1;


    /**
     * 分组名 no 不分组
     * product 产品分组
     * planEndTime  计划结束时间
     */
    @StateEnumValue(strValues = {"no","product","planEndTime"}, message = "分组名标示")
    private String groupNameFlag;

    /**
     * 分组的标示 可能是时间 也可能是产品id
     */
    private String groupFlag;

    /**
     * 高级搜索筛选条件
     */
    private TaskSearchDTO  taskSearchDTO;

    /**
     * 是否可执行（0否，1是）
     */
    private Integer isExecutable;




    /**
     * 查询类别(TaskSearchCategoryEnum枚举，仅作用于后端判断)
     */
    private Integer searchCategory;

    /**
     * 开始时间 (无需传值)
     */
    private Date startTime;

    /**
     * 结束时间(无需传值)
     * @return
     */
    private Date endTime;

    /**
     * 流程ids(无需传值)
     */
    private List<String> processInstanceIds;


}
