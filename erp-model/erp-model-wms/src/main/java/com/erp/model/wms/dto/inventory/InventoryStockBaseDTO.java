package com.erp.model.wms.dto.inventory;

import com.common.business.validator.AddGroup;
import com.common.business.validator.ValidGroup;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * @Classname: InventoryStockBaseDTO

 * @CreateTime: 2023-05-04  15:41
 * @Author: zhangchunlin
 */

@Data
public class InventoryStockBaseDTO implements Serializable {

    /**
     * sku id
     */
    @NotEmpty(message = "sku id不能为空")
    private String skuId;

    /**
     * sku编码
     */
    @NotEmpty(message = "sku编码不能为空")
    private String skuNo;

    /**
     * 仓库id
     */
    @NotEmpty(message = "仓库id 不能为空", groups = {ValidGroup.Update.class})
    private String warehouseId;
    /**
     * 库位id（没有不用传输，某些单据不需要选择库位信息）
     */
    private String warehouseLocation;

    /**
     * 仓库存状态
     */
    private InventoryStatusEnum inventoryStatus;

}