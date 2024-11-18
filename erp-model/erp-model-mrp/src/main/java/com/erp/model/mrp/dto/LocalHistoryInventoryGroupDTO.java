package com.erp.model.mrp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LocalHistoryInventoryGroupDTO {

    /**
     * 组织
     */
    private String orgId;

    /**
     * 仓库
     */
    private String warehouseId;

    /**
     * sku id
     */
    private String skuId;

    /**
     * sku
     */
    private String skuNo;
}
