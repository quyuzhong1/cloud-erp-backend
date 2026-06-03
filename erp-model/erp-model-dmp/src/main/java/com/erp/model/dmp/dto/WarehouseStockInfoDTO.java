package com.erp.model.dmp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * <p>
 * 委外订单子件明细对应的金蝶仓库信息载体。
 * 仓库类型为【供应商仓库】时同步仓位，其它类型仅同步仓库。
 * </p>
 *
 * @author wtr
 * @since 2026-01-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseStockInfoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 金蝶仓库编码
     */
    private String kingdeeWarehouseCode;

    /**
     * 仓位编码
     */
    private String warehouseLocation;

    /**
     * 是否供应商仓库
     */
    private boolean supplierWarehouse;

}
