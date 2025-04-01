package com.erp.model.wms.dto.inventory;

import com.common.core.anno.StateEnumValue;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.model.wms.enums.inventory.VirtualInventoryBusinessTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
/**
 * 流水重构DTO
 * @author will
 * @date 2025/4/1 14:39
 */
@Data
@NoArgsConstructor
public class VirtualFlowRefactorDTO {



    /**
     * 出入库数据
     */
    @Data
    @NoArgsConstructor
    public static class OutInStockDTO {
        /**
         * sku id
         */
        @NotBlank(message = "sku id不能为空")
        private String skuId;

        /**
         * sku编码
         */
        @NotBlank(message = "sku编码不能为空")
        private String skuNo;

        /**
         * 虚拟仓库id
         */
        @NotBlank(message = "虚拟仓库id不能为空")
        private String virtualWarehouseId;

        /**
         * 实物仓库id
         */
        @NotBlank(message = "实物仓库id不能为空")
        private String warehouseId;

        /**
         * 单据类型
         */
        @NotNull(message = "单据类型不能为空")
        private InventorySourceTypeEnum sourceType;

        /**
         * 单据id
         */
        @NotBlank(message = "单据id不能为空")
        private String sourceId;

        /**
         * 单据编号
         */
        @NotBlank(message = "单据编号不能为空")
        private String sourceCode;

        /**
         * 单据日期
         */
        @NotNull(message = "单据日期不能为空")
        private LocalDate billDate;

        /**
         * 原单明细id
         */
        @NotBlank(message = "原单明细id不能为空")
        private String sourceDetailId;

        /**
         * 库存变更数量
         * 增加或减少库存都传正数，程序判断正数或负数
         */
        @NotNull(message = "库存变更数量不能为空")
        private Integer qty;

        /**
         * 业务类型
         */
        @NotBlank(message = "业务类型不能为空")
        @StateEnumValue(clazz = VirtualInventoryBusinessTypeEnum.class,message = "业务类型有误")
        private String businessType;

        /**
         * 是否拆分BOM
         */
        private Boolean isSplitBom = Boolean.TRUE;
    }

}
