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
import com.erp.server.dmp.convert.tool.TypeConversionWorker;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Optional;


/**
 * <p>
 * 亚马逊报告映射工具类
 * </p>
 *
 * @author Jim
 * @since 2023-11-08
 */
@Mapper(uses = TypeConversionWorker.class)
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
//            @Mapping(target = "name", source = "shopInfoEntity.warehouseName"),
            @Mapping(target = "skuNo", expression = "java(null == listingInfoEntity ? \"\":listingInfoEntity.getSkuNo())"),
            @Mapping(target = "productName", expression = "java(null == listingInfoEntity ? \"\":listingInfoEntity.getProductName())"),
            @Mapping(target = "dataStartTime", expression = "java(java.time.OffsetDateTime.parse(combineInventoryDTO.getDataStartTime()))"),
            @Mapping(target = "dataEndTime", expression = "java(java.time.OffsetDateTime.parse(combineInventoryDTO.getDataEndTime()))"),
            // 库存报告管理信息
            @Mapping(target = "asin", source = "inventoryMongoDTO.asin"),
            @Mapping(target = "msku", source = "inventoryMongoDTO.sku"),
            @Mapping(target = "fnSku", source = "inventoryMongoDTO.fnsku"),
            @Mapping(target = "deliveryChannels", expression = "java(inventoryMongoDTO.switchDeliveryChannels())"),
            @Mapping(target = "fbmFulfillableQty", expression = "java(inventoryMongoDTO.mfnFulfillableQuantityCheckToInt())"),
            @Mapping(target = "inboundWorkingQty", expression = "java(inventoryMongoDTO.afnInboundWorkingQuantityCheckToInt())"),
            @Mapping(target = "inboundShippedQty", expression = "java(inventoryMongoDTO.afnInboundShippedQuantityCheckToInt())"),
            @Mapping(target = "inboundReceivingQty", expression = "java(inventoryMongoDTO.afnInboundReceivingQuantityCheckToInt())"),
            @Mapping(target = "fulfillableQty", expression = "java(inventoryMongoDTO.afnFulfillableQuantityCheckToInt())"),
            @Mapping(target = "reservedQty", expression = "java(inventoryMongoDTO.afnReservedQuantityCheckToInt())"),
            @Mapping(target = "researchingQty", expression = "java(inventoryMongoDTO.afnResearchingQuantityCheckToInt())"),
            @Mapping(target = "unsellableQty", expression = "java(inventoryMongoDTO.afnUnsellableQuantityCheckToInt())"),
            // 预留报告信息
            @Mapping(target = "reservedTransfersQty", expression = "java(null == reservedMongoDTO? 0 :reservedMongoDTO.reservedFCTransfersCheckToInt())"),
            @Mapping(target = "reservedProcessingQty", expression = "java(null == reservedMongoDTO? 0 :reservedMongoDTO.reservedFCProcessingCheckToInt())"),
            @Mapping(target = "reservedOrderQty", expression = "java(null == reservedMongoDTO? 0 :reservedMongoDTO.reservedCustomerOrdersCheckToInt())"),
            // 库龄报告信息
            @Mapping(target = "inventoryAge0To30Days", expression = "java(null== planningMongoDTO? 0: java.lang.Integer.parseInt(planningMongoDTO.getInvAge0To30Days()))"),
            @Mapping(target = "inventoryAge31To60Days", expression = "java(null== planningMongoDTO? 0: java.lang.Integer.parseInt(planningMongoDTO.getInvAge31To60Days()))"),
            @Mapping(target = "inventoryAge61To90Days", expression = "java(null== planningMongoDTO? 0: java.lang.Integer.parseInt(planningMongoDTO.getInvAge61To90Days()))"),
            @Mapping(target = "inventoryAge91To180Days", expression = "java(null== planningMongoDTO? 0: java.lang.Integer.parseInt(planningMongoDTO.getInvAge91To180Days()))"),
            @Mapping(target = "inventoryAge181To270Days", expression = "java(null== planningMongoDTO? 0: java.lang.Integer.parseInt(planningMongoDTO.getInvAge181To270Days()))"),
            @Mapping(target = "inventoryAge271To365Days", expression = "java(null== planningMongoDTO? 0: java.lang.Integer.parseInt(planningMongoDTO.getInvAge271To365Days()))"),
            @Mapping(target = "inventoryAge365PlusDays", expression = "java(null== planningMongoDTO? 0: java.lang.Integer.parseInt(planningMongoDTO.getInvAge365PlusDays()))"),

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
