package com.erp.model.plm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
     * 任务id
     */
    @NotBlank(message = "任务id 不能为空")
    private String taskId;


    /**
     * 计划开始时间
     */
    @NotNull(message = "计划开始时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date planStartTime;


    /**
     * 计划结束时间
     */
    @NotNull(message = "计划结束时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date planEndTime;


    /**
     * 任务负责人id
     */
    private List<String> chargeIdList;


    /**
     *是否重启 true 是
     */
    private Boolean isRestart;
}

