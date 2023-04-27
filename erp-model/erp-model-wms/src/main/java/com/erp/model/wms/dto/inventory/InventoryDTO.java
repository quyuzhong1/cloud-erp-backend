package com.erp.model.wms.dto.inventory;

import com.erp.model.wms.enums.inventory.InventoryModeEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.model.wms.enums.SourceTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddDTO extends CommonDTO implements Serializable {

        /**
         * 具体操作的sku数据
         */
        @NotNull(message = "库存sku数据不能为空")
        private List<MemberDTO> members;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommonDTO {

        /**
         * 交易业务类型
         */
        @NotNull(message = "交易业务类型不能为空")
        private InventoryBusinessTypeEnum transTypeEnum;

        @NotEmpty(message = "库存组织不能为空")
        private String orgId;

        @NotNull(message = "单据类型不能为空")
        private SourceTypeEnum sourceTypeEnum;

        @NotEmpty(message = "当前仓不能为空")
        private String warehouseId;

        /**
         * 目的仓（某些交易需要修改目的仓的数据，根据交易规则判断是否传输）
         */
        private String destWarehouseId;
    }

    @Data
    @NoArgsConstructor
    public static class MemberDTO {

        @NotEmpty(message = "sku不能为空")
        private String skuId;

        @NotEmpty(message = "sku编码不能为空")
        private String skuNo;

        @NotEmpty(message = "原单明细id不能为空")
        private String sourceDetailId;

        /**
         * 库位id（可以传输，某些单据没有库位信息）
         */
        private String warehouseLocation;

        /**
         * 需要修改的库存状态（可以不传，默认会从配置中读取；如果指定了则更改指定的状态）
         */
        private InventoryStatusEnum inventoryStatusEnum;

        /**
         * 库存增加或减少（如果指定了库存状态，此字段必填）
         */
        private InventoryModeEnum inventoryModeEnum;

        /**
         * 增加或减少库存都传正数，程序判断正数或负数
         */
        @NotNull(message = "库存变更数量不能为空")
        private Integer qty;
    }




}