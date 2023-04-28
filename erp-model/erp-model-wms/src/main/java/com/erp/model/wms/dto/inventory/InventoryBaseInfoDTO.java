package com.erp.model.wms.dto.inventory;

import com.erp.model.wms.enums.SourceTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryModeEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.model.wms.enums.inventory.InventoryWarehouseOptionEnum;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * @Classname: InventoryBaseInfoDTO
 * @Description: TODO
 * @CreateTime: 2023-04-27  19:51
 * @Author: zhangchunlin
 */
@Data
public class InventoryBaseInfoDTO implements Serializable {

    private String orgId;

    private String warehouseId;

    private String skuId;

    private String skuNo;

    private SourceTypeEnum sourceType;

    private String warehouseLocation;

    private String sourceId;

    private LocalDate billDate;

    private InventoryStatusEnum inventoryStatus;

    private InventoryModeEnum inventoryMode;

    private Integer qty;

    /**
     * 仓库选项，调拨业务用
     */
    private InventoryWarehouseOptionEnum warehouseOption;

}