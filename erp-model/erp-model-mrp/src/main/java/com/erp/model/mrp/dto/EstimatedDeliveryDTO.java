package com.erp.model.mrp.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class EstimatedDeliveryDTO {

    /**
     * 业务类型
     */
    @NotBlank(message = "业务类型不能为空")
    private String type;
    /**
     * 来源类型
     */
    @NotBlank(message = "单据来源类型不能为空")
    private String sourceType;
    /**
     * id
     */
    @NotBlank(message = "建议明细id")
    private String id;
}
