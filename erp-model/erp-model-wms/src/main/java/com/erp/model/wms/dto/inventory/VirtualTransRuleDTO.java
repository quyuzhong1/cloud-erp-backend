package com.erp.model.wms.dto.inventory;

import com.common.core.anno.StateEnumValue;
import com.erp.model.wms.enums.inventory.InventoryModeEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.model.wms.enums.inventory.InventoryWarehouseOptionEnum;
import com.erp.model.wms.enums.inventory.VirtualInventoryBusinessTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 虚拟库存规则
 * @author will
 * @date 2024/6/4 11:09
 */
@Data
@NoArgsConstructor
public class VirtualTransRuleDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class StockParamDTO {
        /**
         * 主键id
         */
        private String id;

        /**
         * 业务类型
         * 自定义规则请勿指定
         */
        private transient VirtualInventoryBusinessTypeEnum dictBizType;

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


        public StockParamDTO(InventoryWarehouseOptionEnum inventoryWarehouseOptionEnum, InventoryStatusEnum inventoryStatusEnum, InventoryModeEnum inventoryModeEnum) {
            this.warehouseOption = inventoryWarehouseOptionEnum;
            this.inventoryStatus = inventoryStatusEnum;
            this.transactionMode = inventoryModeEnum;
        }
    }


}