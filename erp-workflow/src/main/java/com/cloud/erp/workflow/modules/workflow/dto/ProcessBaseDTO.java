package com.cloud.erp.workflow.modules.workflow.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Classname ProcessBaseDTO
 * @Description TODO
 * @Date 2022-08-11 14:27
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ProcessBaseDTO implements Serializable {


    //流程id
    @NotBlank(message = "用户id不能为空")
    private String userId;

    //流程id
    @NotBlank(message = "流程id不能为空")
    private String processInstanceId;




    //评论 意见
    private String comment;
}
