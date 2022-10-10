package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Classname 关联前置任务
 * @Description TODO
 * @Date 2022-09-22 18:34
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SetPreTaskDTO implements Serializable {


    @NotBlank(message = "任务id 不能为空")
    private String taskId;

    @NotBlank(message = "前置任务id 不能为空")
    private String preTaskId;
}
