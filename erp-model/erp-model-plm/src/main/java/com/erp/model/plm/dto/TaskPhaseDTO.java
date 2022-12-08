package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @Classname 任务阶段
 * @Description TODO
 * @Date 2022-09-13 16:14
 * @Created by yl
 */
@Data
@NoArgsConstructor
@Validated
public class TaskPhaseDTO {


    /**
     * 阶段id
     */
    private String id;

    /**
     * 阶段名
     */
    @NotBlank(message = "阶段名不能为空")
    @Size(min = 0, max = 50, message = "阶段名长度不能超过50个字符")
    private String name;

    /**
     * 阶段名
     * 是否是立项阶段
     */
    private Integer isProjectApproval=0;

    private Boolean ifQuote=false;






}
