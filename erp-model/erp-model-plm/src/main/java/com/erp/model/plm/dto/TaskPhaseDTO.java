package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @Classname 任务阶段
 * @Description TODO
 * @Date 2022-09-13 16:14
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class TaskPhaseDTO {


    private String id;

    @NotBlank(message = "阶段名不能为空")
    private String name;






}
