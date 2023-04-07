package com.erp.model.plm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateCiteTaskDTO {
    /**
     * 模板id
     */
    private String templateId;

    /**
     * 产品id
     */
    private String productId;

    /**
     * 任务id
     */
    private List<String> taskIdList;
}
