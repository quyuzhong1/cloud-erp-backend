package com.erp.common.dto.base;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @Classname WorkflowProcessIdDTO
 * @Description TODO
 * @Date 2022-09-20 11:18
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class WorkflowProcessIdDTO {

    @NotBlank(message = "流程id不能为空")
    private String processId;

}
