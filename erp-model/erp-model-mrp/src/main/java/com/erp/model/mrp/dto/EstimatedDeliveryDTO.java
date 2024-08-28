package com.erp.model.mrp.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EstimatedDeliveryDTO {

    /**
     * 业务类型
     */
    private String type;
    /**
     * 来源类型
     */
    private String sourceType;
    /**
     * id
     */
    private String id;
}
