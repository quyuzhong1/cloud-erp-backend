package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: 模板任务删除DTO
 * @date 2022/11/16 9:51
 */
@Data
@NoArgsConstructor
public class TemplateTaskDeleteDTO implements Serializable {
    /**
     * 任务id
     */
    @NotBlank(message = "模板任务id不能为空")
    private String id;

    /**
     * 模板id
     */
    @NotBlank(message = "模板id不能为空")
    private String templateId;
}
