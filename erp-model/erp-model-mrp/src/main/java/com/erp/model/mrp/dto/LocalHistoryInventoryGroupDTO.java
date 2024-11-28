package com.erp.model.mrp.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
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
