package com.erp.model.plm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateTaskSearchDTO {
    /**
     * 模板id
     */
    private String templateId;

    /**
     * 任务阶段
     */
    private List<String> phaseNameList;
}
