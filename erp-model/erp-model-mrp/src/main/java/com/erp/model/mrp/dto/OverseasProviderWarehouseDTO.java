package com.erp.model.mrp.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OverseasProviderWarehouseDTO {

    /**
     * 仓库编码
     */
    private String platformWarehouseCode;
    /**
     * 仓库名称
     */
    private String platformWarehouseName;
    /**
     * 系统仓库id
     */
    private String warehouseId;
    /**
     * 系统仓库名称
     */
    private String warehouseName;
    /**
     * 系统仓库编码
     */
    private String warehouseCode;
    /**
     * 是否禁用 true 禁用 false 启用
     */
    private Boolean disabled;
}
