package com.erp.model.wms.dto.excel;

import lombok.Data;

/**
 * 仓位导入
 * @date 2024-05-31
 * @author tanmujin
 */
@Data
public class WarehouseLocationExcelDto{
    /**
     * 所属仓库名称
     */
    private String warehouseName;

    /**
     * 所属库区编码
     */
    private String warehouseAreaCode;

    /**
     * 仓位编码
     */
    private String warehouseLocationCode;

    /**
     * 仓位名称
     */
    private String warehouseLocationName;

    /**
     * 备注
     */
    private String remark;

}
