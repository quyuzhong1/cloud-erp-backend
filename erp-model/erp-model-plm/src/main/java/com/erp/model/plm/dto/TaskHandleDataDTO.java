package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Classname TaskHandleDataDTO
 * @Description TODO
 * @Date 2022-10-20 11:43
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class TaskHandleDataDTO implements Serializable {

    /**
     * 任务id
     */
    private String taskId;

    /**
     * 流程id
     */
    private String processId;

    /**
     * 流程任务id
     */
    private String processTaskId;
}
