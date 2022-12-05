package com.erp.model.plm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

/**
 * @Classname UpdateTaskDTO
 * @Description TODO
 * @Date 2022-10-13 17:24
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class UpdateTaskDTO  implements Serializable {


    /**
     * 任务id
     */
    @NotBlank(message = "任务id不能为空")
    private String taskId;

    /**
     * 任务名
     */
    private String name;

    /**
     *负责人id集合
     */
    private List<String> chargeIdList;


    /**
     *计划开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private String planStartTime;


    /**
     *计划结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private String planEndTime;
}
