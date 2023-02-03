package com.erp.model.plm.dto;

import com.erp.common.annotation.StateEnumValue;
import com.erp.common.dto.base.PermissionsDTO;
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
public class TaskSearchParamDTO  extends PermissionsDTO {

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
     * 排序类型
     * priority 任务优秀级
     * plan_end_time
     * create_time 创建时间
     * reality_end_time 实际接受时间
     */
    @StateEnumValue(strValues = {"priority", "plan_end_time", "create_time", "reality_end_time" }, message = "搜索类型有误")
    private String searchType="plan_end_time";

    /**
     * 排序
     * desc  降序
     * asc 升序
     */
    @StateEnumValue(strValues = {"desc","asc"}, message = "排序有误")
    private String orderBy="desc";


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
     * 查询类别(TaskSearchCategoryEnum枚举，仅作用于后端判断)
     */
    private Integer searchCategory;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     * @return
     */
    private Date endTime;

    /**
     * 流程ids
     */
    List<String> processInstanceIds;


}
