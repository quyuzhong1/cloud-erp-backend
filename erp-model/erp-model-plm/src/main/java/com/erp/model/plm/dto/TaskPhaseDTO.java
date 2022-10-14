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


    /**
     * 阶段id
     */
    private String id;

    /**
     * 阶段名
     */
    @NotBlank(message = "阶段名不能为空")
    private String name;

    /**
     * 阶段名
     * 是否是立项阶段
     */
    private Integer isProjectApproval=0;






}
