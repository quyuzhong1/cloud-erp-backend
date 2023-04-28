package com.erp.model.wms.dto.inventory;

import com.erp.model.wms.enums.inventory.InventoryWarehouseOptionEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * @Classname: InStockOrOutStockTransformDTO
 * @Description: TODO
 * @CreateTime: 2023-04-28  15:09
 * @Author: zhangchunlin
 */
@Data
public class InStockOrOutStockTransformDTO extends InStockOrOutStockDTO implements Serializable {

    /**
     * 仓库选项
     */
    private InventoryWarehouseOptionEnum warehouseOptionEnum;

}