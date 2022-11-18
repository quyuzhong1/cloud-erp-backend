package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: 模板阶段DTO
 * @date 2022/11/17 10:05
 */
@Data
@NoArgsConstructor
public class TemplatePhaseDTO implements Serializable {

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

    private Boolean ifQuote=false;

}
