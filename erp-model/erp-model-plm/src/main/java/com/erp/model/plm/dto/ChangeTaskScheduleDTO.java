package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Classname ChangeTaskScheduleDTO
 * @Description TODO
 * @Date 2023-02-09 15:33
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ChangeTaskScheduleDTO implements Serializable {

    /**
     * 产品id
     */
    @NotBlank(message = "产品id不能为空")
    private String productId;


    /**
     * 任务id
     */
    @NotBlank(message = "任务id 不能为空")
    private String taskId;


    /**
     * 计划开始时间
     */
    @NotNull(message = "计划开始时间不能为空")
    private Date planStartTime;


    /**
     * 计划结束时间
     */
    @NotNull(message = "计划结束时间不能为空")
    private Date planEndTime;


    private List<String> chargeIdList;
}

