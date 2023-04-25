package com.erp.model.wms.dto;

import com.erp.model.wms.enums.InventoryDirectEnum;
import com.erp.model.wms.enums.InventoryStatusEnum;
import com.erp.model.wms.enums.InventoryTransTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * @Classname: InventoryDTO
 * @Description: 实时库存操作入参
 * @CreateTime: 2023-04-25  10:29
 * @Author: zhangchunlin
 */
@Data
public class InventoryDTO implements Serializable {


    /**
     * 调用方传入List<AddDTO>，因为实时库存表是根据状态来区分字段的；一个操作会导致多个库存状态的变更
     */

    @Data
    public static class AddDTO implements Serializable {

        /**
         * 交易单据类型
         */
        @NotNull(message = "交易单据类型不能为空")
        private InventoryTransTypeEnum transTypeEnum;

        @NotEmpty(message = "库存组织不能为空")
        private String orgId;

        @NotEmpty(message = "id不能为空")
        private String warehouseId;

        /**
         * 具体操作的sku数据
         */
        @NotNull(message = "库存sku数据不能为空")
        @Size(min = 1, message = "至少需要1条库存sku数据")
        private List<CommonDTO> members;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        @NotEmpty(message = "sku不能为空")
        private String skuId;

        @NotEmpty(message = "sku编码不能为空")
        private String skuNo;

        /**
         * 库位id
         */
        private String warehouseLocation;

        /**
         * 需要修改的库存状态
         */
        @NotNull(message = "库存状态不能为空")
        private InventoryStatusEnum inventoryStatusEnum;

        /**
         * 库存增加或减少
         */
        @NotNull(message = "库存增加或减少不能为空")
        private InventoryDirectEnum inventoryDirectEnum;

        /**
         * 增加或减少库存都传正数，由枚举类InventoryDirectEnum决定
         */
        @NotNull(message = "库存变更数量不能为空")
        private Integer qty;
    }




}