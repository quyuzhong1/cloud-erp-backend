package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class TemplateTaskParamsDTO implements Serializable {

    /**
     * 任务id
     */
    @NotBlank(message = "模板任务id不能为空")
    private List<String> ids;

    /**
     * 模板id
     */
    @NotBlank(message = "模板id不能为空")
    private String templateId;
}