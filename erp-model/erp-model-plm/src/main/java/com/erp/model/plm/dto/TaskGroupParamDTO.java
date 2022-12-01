package com.erp.model.plm.dto;

import com.erp.common.annotation.StateEnumValue;
import com.erp.common.dto.base.PermissionsDTO;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @Classname TaskGroupParamDTO
 * @Description TODO
 * @Date 2022-11-08 11:14
 * @Created by yl
 */
@Data
public class TaskGroupParamDTO extends PermissionsDTO {




    /**
     * 任务条件
     * 1.待完成，待审核
     * 2 全部
     */
    @NotNull(message = "任务条件不能为空")
    @StateEnumValue(intValues = {1,2}, message = "任务条件有误")
    private Integer taskCondition;


    /**
     * 分组名 no 不分组
     * product 产品分组
     * planEndTime  计划结束时间
     */
    @StateEnumValue(strValues = {"no","product", "planEndTime"}, message = "分组属性有误")
    private String groupName;

}
