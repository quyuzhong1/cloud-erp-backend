package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: 批量模板阶段DTO
 * @date 2022/11/17 10:01
 */
@Data
@NoArgsConstructor
public class BatchTemplatePhaseDTO implements Serializable {

    @NotBlank(message = "模板id不能为空")
    private String templateId;

    /**
     * 模板阶段集合
     */
    @Valid
    List<TemplatePhaseDTO> templatePhases;
}
