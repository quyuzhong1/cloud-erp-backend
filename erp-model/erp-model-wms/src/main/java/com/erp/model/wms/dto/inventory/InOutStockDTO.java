package com.erp.model.wms.dto.inventory;

import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
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
public class InOutStockDTO extends InventoryStockBaseDTO implements Serializable {

        /**
         * 仓库id
         */
        @NotEmpty(message = "仓库不能为空")
        private String warehouseId;

        /**
         * 单据类型
         */
        @NotNull(message = "单据类型不能为空")
        private InventorySourceTypeEnum sourceType;

        /**
         * 单据id
         */
        @NotEmpty(message = "单据id不能为空")
        private String sourceId;

        /**
         * 单据编号
         */
        @NotEmpty(message = "单据编号不能为空")
        private String sourceCode;

        /**
         * 单据日期
         */
        @NotNull(message = "单据日期不能为空")
        private LocalDate billDate;

        /**
         * 原单明细id
         */
        @NotEmpty(message = "原单明细id不能为空")
        private String sourceDetailId;

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
         * 库位id（没有不用传输，某些单据不需要选择库位信息）
         */
        private String warehouseLocation;

        /**
         * 库存变更数量
         * 增加或减少库存都传正数，程序判断正数或负数
         */
        @NotNull(message = "库存变更数量不能为空")
        @Min(value = 1,message = "库存变更数量不能小于1")
        private Integer qty;


}