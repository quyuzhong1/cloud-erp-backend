package com.erp.server.dmp.convert;

import com.baomidou.mybatisplus.annotation.TableField;
import com.erp.model.dmp.entity.*;
import com.erp.model.oms.entity.*;
import com.erp.model.wms.entity.FbaInventoryEntity;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.sdk.oms.amz.spapi.dto.ReportFbaInventoryPlanningMongoDTO;
import com.erp.sdk.oms.amz.spapi.dto.ReportFbaMyiAllInventoryMongoDTO;
import com.erp.sdk.oms.amz.spapi.dto.ReportInventoryCombineMongoDTO;
import com.erp.sdk.oms.amz.spapi.dto.ReportReservedMongoDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.Optional;


/**
 * <p>
 * 亚马逊报告映射工具类
 * </p>
 *
 * @author Jim
 * @since 2023-11-08
 */
@Mapper
@Component
public interface DmpFbaInventoryConverter {
    DmpFbaInventoryConverter INSTANCE = Mappers.getMapper(DmpFbaInventoryConverter.class);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "createUserId", ignore = true),
            @Mapping(target = "createUserName", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "updateUserId", ignore = true),
            @Mapping(target = "updateUserName", ignore = true),
            @Mapping(target = "isDeleted", ignore = true),
            @Mapping(target = "version", ignore = true),
            // 其他记录信息
            @Mapping(target = "name", source = "shopInfoEntity.warehouseName"),
            @Mapping(target = "skuNo", source = "listingInfoEntity.skuNo"),
            @Mapping(target = "productName", source = "listingInfoEntity.productName"),
            @Mapping(target = "dataStartTime", source = "combineInventoryDTO.dataStartTime"),
            @Mapping(target = "dataEndTime", source = "combineInventoryDTO.dataEndTime"),
            // 库存报告管理信息
            @Mapping(target = "asin", source = "inventoryMongoDTO.asin"),
            @Mapping(target = "msku", source = "inventoryMongoDTO.sku"),
            @Mapping(target = "fnSku", source = "inventoryMongoDTO.fnsku"),
            @Mapping(target = "deliveryChannels", expression = "java(inventoryMongoDTO.switchDeliveryChannels())"),
            @Mapping(target = "fbmFulfillableQty", source = "inventoryMongoDTO.mfnFulfillableQuantity"),
            @Mapping(target = "inboundWorkingQty", source = "inventoryMongoDTO.afnInboundWorkingQuantity"),
            @Mapping(target = "inboundShippedQty", source = "inventoryMongoDTO.afnInboundShippedQuantity"),
            @Mapping(target = "inboundReceivingQty", source = "inventoryMongoDTO.afnInboundReceivingQuantity"),
            @Mapping(target = "fulfillableQty", source = "inventoryMongoDTO.afnFulfillableQuantity"),
            @Mapping(target = "reservedQty", source = "inventoryMongoDTO.afnReservedQuantity"),
            @Mapping(target = "researchingQty", source = "inventoryMongoDTO.afnResearchingQuantity"),
            @Mapping(target = "unsellableQty", source = "inventoryMongoDTO.afnUnsellableQuantity"),
            // 预留报告信息
            @Mapping(target = "reservedTransfersQty", source = "reservedMongoDTO.reservedFCTransfers"),
            @Mapping(target = "reservedProcessingQty", source = "reservedMongoDTO.reservedFCProcessing"),
            @Mapping(target = "reservedOrderQty", source = "reservedMongoDTO.reservedCustomerOrders"),
            // 库龄报告信息
            @Mapping(target = "inventoryAge0To30Days", source = "planningMongoDTO.invAge0To30Days"),
            @Mapping(target = "inventoryAge31To60Days", source = "planningMongoDTO.invAge31To60Days"),
            @Mapping(target = "inventoryAge61To90Days", source = "planningMongoDTO.invAge61To90Days"),
            @Mapping(target = "inventoryAge91To180Days", source = "planningMongoDTO.invAge91To180Days"),
            @Mapping(target = "inventoryAge181To270Days", source = "planningMongoDTO.invAge181To270Days"),
            @Mapping(target = "inventoryAge271To365Days", source = "planningMongoDTO.invAge271To365Days"),
            @Mapping(target = "inventoryAge365PlusDays", source = "planningMongoDTO.invAge365PlusDays"),

    })
    FbaInventoryEntity mergeToFbaInventoryEntity(
            ReportInventoryCombineMongoDTO combineInventoryDTO,
            ReportFbaMyiAllInventoryMongoDTO inventoryMongoDTO,
            ReportReservedMongoDTO reservedMongoDTO,
            ReportFbaInventoryPlanningMongoDTO planningMongoDTO,
            ShopInfoEntity shopInfoEntity,
            ListingInfoEntity listingInfoEntity
    );







}
