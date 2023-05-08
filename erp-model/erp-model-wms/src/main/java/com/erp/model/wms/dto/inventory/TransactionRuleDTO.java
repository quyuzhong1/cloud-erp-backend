package com.erp.model.wms.dto.inventory;

import com.common.core.anno.StateEnumValue;
import com.erp.model.wms.enums.inventory.InventoryModeEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryWarehouseOptionEnum;
import lombok.Data;

import javax.validation.constraints.NotNull;
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
     * 自定义规则请勿指定
     */
    private transient InventoryBusinessTypeEnum dictBizType;

    /**
     * 仓库选项
     */
    @NotNull(message = "仓库选项不能为空")
    @StateEnumValue(clazz = InventoryWarehouseOptionEnum.class,message = "仓库选项有误")
    private InventoryWarehouseOptionEnum warehouseOption;

    /**
     * 库存状态
     */
    @NotNull(message = "库存状态不能为空")
    @StateEnumValue(clazz = InventoryStatusEnum.class,message = "库存状态有误")
    private InventoryStatusEnum inventoryStatus;

    /**
     * 交易方向；1-增加；-1减少
     */
    @NotNull(message = "库存交易方向不能为空")
    @StateEnumValue(clazz = InventoryModeEnum.class,message = "库存交易方向有误")
    private InventoryModeEnum transactionMode;

}