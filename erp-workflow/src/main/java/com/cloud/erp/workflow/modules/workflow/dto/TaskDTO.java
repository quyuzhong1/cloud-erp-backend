package com.cloud.erp.workflow.modules.workflow.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Classname TaskDTO
 * @Description TODO
 * @Date 2022-08-17 16:07
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class TaskDTO  implements Serializable {


    private String taskId;

    private String processInstanceId;
}
