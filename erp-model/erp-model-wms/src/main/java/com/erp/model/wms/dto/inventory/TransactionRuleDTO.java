package com.erp.model.wms.dto.inventory;

import com.erp.model.wms.enums.inventory.InventoryModeEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryWarehouseOptionEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * @Classname: TransactionRuleDTO
 * @Description: 交易规则传输实体
 * @CreateTime: 2023-04-26  14:06
 * @Author: zhangchunlin
 */
@Data
public class TransactionRuleDTO implements Serializable {

    private String id;

    /**
     * 业务类型
     */
    private InventoryBusinessTypeEnum dictBizType;

    /**
     * 仓库选项
     */
    private InventoryWarehouseOptionEnum warehouseOption;

    /**
     * 库存状态
     */
    private InventoryStatusEnum inventoryStatus;

    /**
     * 交易方向；1-增加；-1减少
     */
    private InventoryModeEnum transactionMode;

}