package com.erp.model.wms.dto.inventory;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.validator.ValidGroup;
import com.erp.model.wms.entity.PoReturnDetailEntity;
import com.erp.model.wms.entity.PoReturnEntity;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import lombok.Data;

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
         * 单据类型
         */
        @NotNull(message = "单据类型不能为空", groups = {ValidGroup.Update.class})
        private InventorySourceTypeEnum sourceType;

        /**
         * 单据id
         */
        @NotEmpty(message = "单据id不能为空", groups = {ValidGroup.Update.class})
        private String sourceId;

        /**
         * 单据编号
         */
        @NotEmpty(message = "单据编号不能为空", groups = {ValidGroup.Update.class})
        private String sourceCode;

        /**
         * 单据日期
         */
        @NotNull(message = "单据日期不能为空", groups = {ValidGroup.Update.class})
        private LocalDate billDate;

        /**
         * 原单明细id
         */
        @NotEmpty(message = "原单明细id不能为空", groups = {ValidGroup.Update.class})
        private String sourceDetailId;

        /**
         * 库存变更数量
         * 增加或减少库存都传正数，程序判断正数或负数
         */
        @NotNull(message = "库存变更数量不能为空", groups = {ValidGroup.Update.class})
        // @Min(value = 1,message = "库存变更数量不能小于1")
        private Integer qty;

        /**
         * 虚拟仓库Id
         */
        private String virtualWarehouseId;

        /**
         * 库存锁等待时间  默认： 5s
         */
        private Long lockWaitTime;


        public static InOutStockDTO initByReturnOrder(PoReturnEntity entity, PoReturnDetailEntity detail, InventorySourceTypeEnum sourceType, Integer qty, InventoryStatusEnum inventoryStatus) {
                InOutStockDTO inOutStockDTO = new InOutStockDTO();
                inOutStockDTO.setSourceType(sourceType);
                inOutStockDTO.setSourceId(entity.getId());
                inOutStockDTO.setSourceCode(entity.getCode());
                inOutStockDTO.setSourceDetailId(detail.getId());
                inOutStockDTO.setBillDate(entity.getBillDate());
                inOutStockDTO.setSkuId(detail.getSkuId());
                inOutStockDTO.setSkuNo(detail.getSkuNo());
                inOutStockDTO.setWarehouseId(entity.getReturnWarehouseId());
                inOutStockDTO.setWarehouseLocation(detail.getWarehouseLocation());
                // 根据捕获数量增加在途
                inOutStockDTO.setQty(qty);
                inOutStockDTO.setInventoryStatus(inventoryStatus);
                return inOutStockDTO;
        }

        public static InOutStockDTO getInOutStockDTO(SoOutstockEntity entity, String sourceDetailId, String skuId, String skuNo, String warehouseLocation, Integer qty){
                InOutStockDTO stockDTO = new InOutStockDTO();
                stockDTO.setWarehouseId(entity.getWarehouseId());
                stockDTO.setSourceId(entity.getId());
                stockDTO.setSourceCode(entity.getCode());
                stockDTO.setSourceDetailId(sourceDetailId);
                stockDTO.setSkuId(skuId);
                stockDTO.setSkuNo(skuNo);
                stockDTO.setWarehouseLocation(CharSequenceUtil.isNotBlank(entity.getBatchNo()) ? "" : warehouseLocation);
                stockDTO.setQty(qty);
                stockDTO.setBillDate(entity.getBillDate());
                return stockDTO;
        }
}