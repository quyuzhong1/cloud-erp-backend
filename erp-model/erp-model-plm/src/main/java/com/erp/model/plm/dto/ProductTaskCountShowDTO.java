package com.erp.model.plm.dto;

import com.common.business.dto.base.PermissionsDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductTaskCountShowDTO extends PermissionsDTO {
    /**
     * 产品id
     */
    private String  productId;
}
