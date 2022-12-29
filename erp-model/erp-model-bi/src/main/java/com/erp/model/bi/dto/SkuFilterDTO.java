package com.erp.model.bi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class SkuFilterDTO extends BiFilterDTO {
    /**
     * sku
     */
    @NotNull(message = "sku不能为空")
    private String skuNo;

}
