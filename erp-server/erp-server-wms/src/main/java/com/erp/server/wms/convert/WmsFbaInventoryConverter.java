package com.erp.server.wms.convert;

import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.wms.entity.FbaInventoryEntity;
import com.erp.sdk.oms.amz.spapi.dto.ReportFbaMyiAllInventoryMongoDTO;
import com.erp.server.wms.convert.tool.TypeConversionWorker;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

/**
 * FBA库存实体映射工具
 *
 * @Author Jim
 * @Date 2023/11/22
 **/
@Mapper(uses = TypeConversionWorker.class)
@Component
public interface WmsFbaInventoryConverter {

    WmsFbaInventoryConverter INSTANCE = Mappers.getMapper(WmsFbaInventoryConverter.class);

    @Mappings({
            @Mapping(target = "id", source = "oldEntity.id"),
            @Mapping(target = "createTime", source = "oldEntity.createTime"),
            @Mapping(target = "createUserId", source = "oldEntity.createUserId"),
            @Mapping(target = "createUserName", source = "oldEntity.createUserName"),
            @Mapping(target = "updateTime", source = "oldEntity.updateTime"),
            @Mapping(target = "updateUserId", source = "oldEntity.updateUserId"),
            @Mapping(target = "updateUserName", source = "oldEntity.updateUserName"),
            @Mapping(target = "isDeleted", source = "oldEntity.isDeleted"),
            @Mapping(target = "version", source = "oldEntity.version"),
            // 其他记录信息
            @Mapping(target = "skuNo", expression = "java(cn.hutool.core.text.CharSequenceUtil.isBlank(newEntity.getSkuNo())? oldEntity.getSkuNo() : newEntity.getSkuNo())"),
            @Mapping(target = "productName", expression = "java(cn.hutool.core.text.CharSequenceUtil.isBlank(newEntity.getProductName())? oldEntity.getProductName() : newEntity.getProductName())"),
            @Mapping(target = "dataStartTime", expression = "java(null == newEntity.getDataStartTime() ? oldEntity.getDataStartTime() : newEntity.getDataStartTime())"),
            @Mapping(target = "dataEndTime", expression = "java(null == newEntity.getDataEndTime() ? oldEntity.getDataStartTime() : newEntity.getDataEndTime())"),
            @Mapping(target = "warehouseName", expression = "java(cn.hutool.core.text.CharSequenceUtil.isBlank(newEntity.getWarehouseName())? oldEntity.getWarehouseName() : newEntity.getWarehouseName())"),
            @Mapping(target = "warehouseId", expression = "java(cn.hutool.core.text.CharSequenceUtil.isBlank(newEntity.getWarehouseId())? oldEntity.getWarehouseId() : newEntity.getWarehouseId())"),
            // 库存报告管理信息
            @Mapping(target = "asin", source = "oldEntity.asin"),
            @Mapping(target = "msku", source = "oldEntity.msku"),
            @Mapping(target = "fnSku", source = "oldEntity.fnSku"),
            @Mapping(target = "deliveryChannels", expression = "java(cn.hutool.core.text.CharSequenceUtil.isBlank(newEntity.getDeliveryChannels())? oldEntity.getDeliveryChannels() : newEntity.getDeliveryChannels())"),
            @Mapping(target = "fbmFulfillableQty", expression = "java(null == newEntity.getFulfillableQty() ? oldEntity.getFulfillableQty() : newEntity.getFulfillableQty())"),
            @Mapping(target = "inboundWorkingQty", expression = "java(null == newEntity.getInboundWorkingQty() ? oldEntity.getInboundWorkingQty() : newEntity.getInboundWorkingQty())"),
            @Mapping(target = "inboundShippedQty", expression = "java(null == newEntity.getInboundShippedQty() ? oldEntity.getInboundShippedQty() : newEntity.getInboundShippedQty())"),
            @Mapping(target = "inboundReceivingQty", expression = "java(null == newEntity.getInboundReceivingQty() ? oldEntity.getInboundReceivingQty() : newEntity.getInboundReceivingQty())"),
            @Mapping(target = "fulfillableQty", expression = "java(null == newEntity.getFulfillableQty() ? oldEntity.getFulfillableQty() : newEntity.getFulfillableQty())"),
            @Mapping(target = "reservedQty", expression = "java(null == newEntity.getReservedQty() ? oldEntity.getReservedQty() : newEntity.getReservedQty())"),
            @Mapping(target = "researchingQty", expression = "java(null == newEntity.getResearchingQty() ? oldEntity.getResearchingQty() : newEntity.getResearchingQty())"),
            @Mapping(target = "unsellableQty", expression = "java(null == newEntity.getUnsellableQty() ? oldEntity.getUnsellableQty() : newEntity.getUnsellableQty())"),
            // 预留报告信息
            @Mapping(target = "reservedTransfersQty", expression = "java(null == newEntity.getReservedTransfersQty() ? oldEntity.getReservedTransfersQty() : newEntity.getReservedTransfersQty())"),
            @Mapping(target = "reservedProcessingQty", expression = "java(null == newEntity.getReservedProcessingQty() ? oldEntity.getReservedProcessingQty() : newEntity.getReservedProcessingQty())"),
            @Mapping(target = "reservedOrderQty", expression = "java(null == newEntity.getReservedOrderQty() ? oldEntity.getReservedOrderQty() : newEntity.getReservedOrderQty())"),
            // 库龄报告信息
            @Mapping(target = "inventoryAge0To30Days", expression = "java(null == newEntity.getInventoryAge0To30Days() ? oldEntity.getInventoryAge0To30Days() : newEntity.getInventoryAge0To30Days())"),
            @Mapping(target = "inventoryAge31To60Days", expression = "java(null == newEntity.getInventoryAge31To60Days() ? oldEntity.getInventoryAge31To60Days() : newEntity.getInventoryAge31To60Days())"),
            @Mapping(target = "inventoryAge61To90Days", expression = "java(null == newEntity.getInventoryAge61To90Days() ? oldEntity.getInventoryAge61To90Days() : newEntity.getInventoryAge61To90Days())"),
            @Mapping(target = "inventoryAge91To180Days", expression = "java(null == newEntity.getInventoryAge91To180Days() ? oldEntity.getInventoryAge91To180Days() : newEntity.getInventoryAge91To180Days())"),
            @Mapping(target = "inventoryAge181To270Days", expression = "java(null == newEntity.getInventoryAge181To270Days() ? oldEntity.getInventoryAge181To270Days() : newEntity.getInventoryAge181To270Days())"),
            @Mapping(target = "inventoryAge271To365Days", expression = "java(null == newEntity.getInventoryAge271To365Days() ? oldEntity.getInventoryAge271To365Days() : newEntity.getInventoryAge271To365Days())"),
            @Mapping(target = "inventoryAge365PlusDays", expression = "java(null == newEntity.getInventoryAge365PlusDays() ? oldEntity.getInventoryAge365PlusDays() : newEntity.getInventoryAge365PlusDays())"),
    })
    FbaInventoryEntity newCombineOld(FbaInventoryEntity oldEntity, FbaInventoryEntity newEntity);
}
