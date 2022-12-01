package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: 模板阶段删除DTO
 * @date 2022/11/18 9:50
 */
@Data
@NoArgsConstructor
public class TemplatePhaseDeleteDTO implements Serializable {

    /**
     * 主键id
     */
    private String id;

    /**
     * 模板id
     */
    @NotBlank(message = "模板id不能为空")
    private String templateId;
}
