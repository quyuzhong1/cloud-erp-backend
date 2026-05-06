package com.erp.server.dmp.convert;

import com.erp.model.dmp.entity.DmpWdtWarehouseInventoryRecordEntity;
import com.sdk.wangdian.sdk.api.wms.stockin.dto.CreateOtherStockinRequest;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.CreateOtherStockoutRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface DmpWdtConverter {
    DmpWdtConverter INSTANCE = Mappers.getMapper(DmpWdtConverter.class);

    @Mapping(target = "warehouseNo", source = "entity.thirdWarehouseCode")
    @Mapping(target = "targetPlatformName", expression = "java(com.erp.model.dmp.enums.PlatformEnum.WANGDIAN.getName())")
    @Mapping(target = "sysWarehouseId", source = "entity.erpWarehouseId")
    @Mapping(target = "sourcePlatformName", expression = "java(com.erp.model.dmp.enums.PlatformEnum.ERP_DMP.getName())")
    @Mapping(target = "sourceId", source = "entity.id")
    @Mapping(target = "reason", constant = "数大臣库存对比差异执行库存调整")
    @Mapping(target = "outerNo", source = "entity.batchNo")
    @Mapping(target = "operateCode", expression = "java(com.common.business.enums.SyncOperateEnum.OPERATE_APPROVE.getCode())")
    @Mapping(target = "logisticsNo", ignore = true)
    @Mapping(target = "logisticsCode", ignore = true)
    @Mapping(target = "isCheck", constant = "true")
    @Mapping(target = "goodsList", source = "detailList")
    @Mapping(target = "dmpSyncTaskId", ignore = true)
    CreateOtherStockinRequest toCreateOtherStockinRequest(DmpWdtWarehouseInventoryRecordEntity entity, List<DmpWdtWarehouseInventoryRecordEntity> detailList);

    @Mapping(target = "sourceCode", ignore = true)
    @Mapping(target = "postFee", ignore = true)
    @Mapping(target = "warehouseNo", source = "entity.thirdWarehouseCode")
    @Mapping(target = "targetPlatformName", expression = "java(com.erp.model.dmp.enums.PlatformEnum.WANGDIAN.getName())")
    @Mapping(target = "sysWarehouseId", source = "entity.erpWarehouseId")
    @Mapping(target = "sourcePlatformName", expression = "java(com.erp.model.dmp.enums.PlatformEnum.ERP_DMP.getName())")
    @Mapping(target = "sourceId", source = "entity.id")
    @Mapping(target = "reason", constant = "数大臣库存对比差异执行库存调整")
    @Mapping(target = "outerNo", source = "entity.batchNo")
    @Mapping(target = "operateCode", expression = "java(com.common.business.enums.SyncOperateEnum.OPERATE_APPROVE.getCode())")
    @Mapping(target = "logisticsNo", ignore = true)
    @Mapping(target = "logisticsCode", ignore = true)
    @Mapping(target = "isCheck", constant = "true")
    @Mapping(target = "goodsList", source = "detailList")
    @Mapping(target = "dmpSyncTaskId", ignore = true)
    CreateOtherStockoutRequest toCreateOtherStockOutRequest(DmpWdtWarehouseInventoryRecordEntity entity, List<DmpWdtWarehouseInventoryRecordEntity> detailList);

    @Mapping(target = "warehouseId", ignore = true)
    @Mapping(target = "specNo", source = "thirdSkuNo")
    @Mapping(target = "productionDate", ignore = true)
    @Mapping(target = "positionNo", ignore = true)
    @Mapping(target = "num", source = "qty")
    @Mapping(target = "expireDate", ignore = true)
    @Mapping(target = "batchNo", ignore = true)
    CreateOtherStockinRequest.GoodsList toGoodsListIn(DmpWdtWarehouseInventoryRecordEntity entity);
    @Mapping(target = "warehouseId", ignore = true)
    @Mapping(target = "specNo", source = "thirdSkuNo")
    @Mapping(target = "positionNo", ignore = true)
    @Mapping(target = "num", source = "qty")
    CreateOtherStockoutRequest.GoodsList toGoodsListOut(DmpWdtWarehouseInventoryRecordEntity entity);
}
