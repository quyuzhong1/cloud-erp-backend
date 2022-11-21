package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: 模板输出物删除DTO
 * @date 2022/11/16 10:55
 */
@Data
@NoArgsConstructor
public class TemplateDeliveryDocsDeleteDTO implements Serializable {

    /**
     * 输出物id
     */
    @NotBlank(message = "输出物id不能为空")
    private String id;

    /**
     * 模板任务id
     */
    private String taskId;

    /**
     * 模板id
     */
    @NotBlank(message = "模板id不能为空")
    private String templateId;
}
