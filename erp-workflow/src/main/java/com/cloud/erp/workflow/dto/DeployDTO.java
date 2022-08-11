package com.cloud.erp.workflow.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * @Classname 部署流程入参
 *
 * @Description TODO
 * @Date 2022-08-11 11:16
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class DeployDTO implements Serializable {

    //流程名字
    @NotEmpty(message = "流程名不能为空")
    private String name;

    @NotEmpty(message = "目录下的流程图不能为空")
    private String resource;


}
