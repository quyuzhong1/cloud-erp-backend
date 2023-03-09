package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: 输出物修改状态DTO
 * @date 2022/11/17 9:48
 */
@Data
@NoArgsConstructor
public class TemplateDeliveryDocsUpdateStatusDTO implements Serializable {

    /**
     * 主键id
     */
    private String id;

    /**
     * 状态
     */
    @NotNull(message = "状态不能为空")
    private Boolean status;

    /**
     * 模板id
     */
    @NotBlank(message = "模板id不能为空")
    private String templateId;
}
