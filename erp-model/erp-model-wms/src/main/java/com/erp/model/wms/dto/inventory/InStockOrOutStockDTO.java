package com.erp.model.wms.dto.inventory;

import com.erp.model.wms.enums.inventory.InventoryModeEnum;
import com.erp.model.wms.enums.inventory.InventoryOperationModeEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
/**
 * @Classname: InstockOrOutStockDTO
 * @Description: 出入库请求实体；出入库只针对一个仓库的操作
 * @CreateTime: 2023-04-26  14:13
 * @Author: zhangchunlin
 */
@Data
public class InStockOrOutStockDTO extends InventoryStockBaseDTO implements Serializable {

        @NotEmpty(message = "仓库组织不能为空")
        private String orgId;

        @NotEmpty(message = "仓库不能为空")
        private String warehouseId;

        @NotNull(message = "单据类型不能为空")
        private InventorySourceTypeEnum sourceType;

        @NotEmpty(message = "单据id不能为空")
        private String sourceId;

        @NotEmpty(message = "单据编号不能为空")
        private String sourceCode;

        @NotNull(message = "单据日期不能为空")
        private LocalDate billDate;

        @NotEmpty(message = "原单明细id不能为空")
        private String sourceDetailId;

        @NotEmpty(message = "sku不能为空")
        private String skuId;

        @NotEmpty(message = "sku编码不能为空")
        private String skuNo;

        /**
         * 库位id（没有不用传输，某些单据不需要选择库位信息）
         */
        private String warehouseLocation;

        /**
         * 调拨用，出入库业务一般不用；特殊场景可以考虑使用（无法固化状态的）
         * 需要修改的库存状态（可以不传，默认会从配置中读取；如果指定了则取指定的状态）
         */
        private InventoryStatusEnum inventoryStatus;

        /**
         * 从规则中配置的交易不用配置，特殊情况需要人工指定库存状态的则需要传输，库存增加或减少（如果指定了库存状态，此字段必填）
         */
        private InventoryModeEnum inventoryMode;

        /**
         * 增加或减少库存都传正数，程序判断正数或负数
         */
        @NotNull(message = "库存变更数量不能为空")
        @Min(value = 1,message = "库存变更数量不能小于1")
        private Integer qty;

        /**
         * 操作类型；默认为审核，后补单时需赋值，反审核有专门的方法入口
         */
        private InventoryOperationModeEnum operationMode =  InventoryOperationModeEnum.APPROVE;

}