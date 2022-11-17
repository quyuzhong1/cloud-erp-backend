package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: 模板idDTO
 * @date 2022/11/17 10:45
 */
@Data
@NoArgsConstructor
public class BasicTemplateIdDTO  implements Serializable {

    /**
     * 模板id
     */
    @NotBlank(message = "模板id不能为空")
    private String templateId;
}
