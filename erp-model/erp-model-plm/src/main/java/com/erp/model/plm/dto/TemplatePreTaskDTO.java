package com.erp.model.plm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @author Cloud
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplatePreTaskDTO {
        @NotBlank(message = "任务id不能为空")
        private String taskId;

        @NotBlank(message = "模板id不能为空")
        private String templateId;
}