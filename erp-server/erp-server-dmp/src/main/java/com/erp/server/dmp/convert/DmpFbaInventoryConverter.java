package com.erp.server.dmp.convert;

import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.wms.entity.FbaInventoryEntity;
import com.erp.sdk.oms.amz.spapi.csv.ReportFbaInventoryPlanningCsvEntity;
import com.erp.sdk.oms.amz.spapi.csv.ReportFbaMyiAllInventoryCsvEntity;
import com.erp.sdk.oms.amz.spapi.csv.ReportReservedCsvEntity;
import com.erp.sdk.oms.amz.spapi.dto.ReportFbaInventoryPlanningMongoDTO;
import com.erp.sdk.oms.amz.spapi.dto.ReportFbaMyiAllInventoryMongoDTO;
import com.erp.sdk.oms.amz.spapi.dto.ReportFbaMyiUnsuppressedInventoryMongoDTO;
import com.erp.sdk.oms.amz.spapi.dto.ReportReservedMongoDTO;
import com.erp.server.dmp.convert.tool.TypeConversionWorker;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.Optional;


/**
 * <p>
 * 亚马逊库存报告映射工具类
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
            @Mapping(target = "skuNo", expression = "java(null == listingInfoWithSkuMappingDTO ? \"\":listingInfoWithSkuMappingDTO.checkAndGetProductSkuNo())"),
            @Mapping(target = "productName", expression = "java(null == listingInfoWithSkuMappingDTO ? \"\":listingInfoWithSkuMappingDTO.checkAndGetProductName())"),
            @Mapping(target = "dataStartTime", expression = "java(java.time.OffsetDateTime.parse(dataStartTime))"),
            @Mapping(target = "dataEndTime", expression = "java(java.time.OffsetDateTime.parse(dataEndTime))"),
            @Mapping(target = "warehouseName", expression = "java(org.apache.commons.lang3.StringUtils.isBlank(shopInfoEntity.getWarehouseName())? \"\":shopInfoEntity.getWarehouseName())"),
            @Mapping(target = "warehouseId", expression = "java(org.apache.commons.lang3.StringUtils.isBlank(shopInfoEntity.getWarehouseId())? \"\":shopInfoEntity.getWarehouseId())"),
            // 库存报告管理信息
            @Mapping(target = "asin", source = "esvEntity.asin"),
            @Mapping(target = "msku", source = "esvEntity.sku"),
            @Mapping(target = "fnSku", source = "esvEntity.fnsku"),
            @Mapping(target = "deliveryChannels", expression = "java(esvEntity.switchDeliveryChannels())"),
            @Mapping(target = "fbmFulfillableQty", expression = "java(esvEntity.mfnFulfillableQuantityCheckToInt())"),
            @Mapping(target = "inboundWorkingQty", expression = "java(esvEntity.afnInboundWorkingQuantityCheckToInt())"),
            @Mapping(target = "inboundShippedQty", expression = "java(esvEntity.afnInboundShippedQuantityCheckToInt())"),
            @Mapping(target = "inboundReceivingQty", expression = "java(esvEntity.afnInboundReceivingQuantityCheckToInt())"),
            @Mapping(target = "fulfillableQty", expression = "java(esvEntity.afnFulfillableQuantityCheckToInt())"),
            @Mapping(target = "reservedQty", expression = "java(esvEntity.afnReservedQuantityCheckToInt())"),
            @Mapping(target = "researchingQty", expression = "java(esvEntity.afnResearchingQuantityCheckToInt())"),
            @Mapping(target = "unsellableQty", expression = "java(esvEntity.afnUnsellableQuantityCheckToInt())"),
    })
    FbaInventoryEntity reportFbaMyiAllInventoryToEntity(
            ReportFbaMyiAllInventoryCsvEntity esvEntity,
            ShopInfoEntity shopInfoEntity,
            ListingInfoWithSkuMappingDTO listingInfoWithSkuMappingDTO,
            String dataStartTime,
            String dataEndTime
    );


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
            @Mapping(target = "skuNo", expression = "java(null == listingInfoWithSkuMappingDTO ? \"\":listingInfoWithSkuMappingDTO.checkAndGetProductSkuNo())"),
            @Mapping(target = "productName", expression = "java(null == listingInfoWithSkuMappingDTO ? \"\":listingInfoWithSkuMappingDTO.checkAndGetProductName())"),
            @Mapping(target = "dataStartTime", expression = "java(java.time.OffsetDateTime.parse(dataStartTime))"),
            @Mapping(target = "dataEndTime", expression = "java(java.time.OffsetDateTime.parse(dataEndTime))"),
            @Mapping(target = "warehouseName", expression = "java(org.apache.commons.lang3.StringUtils.isBlank(shopInfoEntity.getWarehouseName())? \"\":shopInfoEntity.getWarehouseName())"),
            @Mapping(target = "warehouseId", expression = "java(org.apache.commons.lang3.StringUtils.isBlank(shopInfoEntity.getWarehouseId())? \"\":shopInfoEntity.getWarehouseId())"),
            // 库存报告管理信息
            @Mapping(target = "asin", source = "csvEntity.asin"),
            @Mapping(target = "msku", source = "csvEntity.sku"),
            @Mapping(target = "fnSku", source = "csvEntity.fnsku"),
            // 预留报告信息
            @Mapping(target = "reservedTransfersQty", expression = "java(null == csvEntity? 0 :csvEntity.reservedFCTransfersCheckToInt())"),
            @Mapping(target = "reservedProcessingQty", expression = "java(null == csvEntity? 0 :csvEntity.reservedFCProcessingCheckToInt())"),
            @Mapping(target = "reservedOrderQty", expression = "java(null == csvEntity? 0 :csvEntity.reservedCustomerOrdersCheckToInt())"),

    })
    FbaInventoryEntity reportReservedToEntity(
            ReportReservedCsvEntity csvEntity,
            ShopInfoEntity shopInfoEntity,
            ListingInfoWithSkuMappingDTO listingInfoWithSkuMappingDTO,
            String dataStartTime,
            String dataEndTime
    );

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
            @Mapping(target = "skuNo", expression = "java(null == listingInfoWithSkuMappingDTO ? \"\":listingInfoWithSkuMappingDTO.checkAndGetProductSkuNo())"),
            @Mapping(target = "productName", expression = "java(null == listingInfoWithSkuMappingDTO ? \"\":listingInfoWithSkuMappingDTO.checkAndGetProductName())"),
            @Mapping(target = "dataStartTime", expression = "java(java.time.OffsetDateTime.parse(dataStartTime))"),
            @Mapping(target = "dataEndTime", expression = "java(java.time.OffsetDateTime.parse(dataEndTime))"),
            @Mapping(target = "warehouseName", expression = "java(org.apache.commons.lang3.StringUtils.isBlank(shopInfoEntity.getWarehouseName())? \"\":shopInfoEntity.getWarehouseName())"),
            @Mapping(target = "warehouseId", expression = "java(org.apache.commons.lang3.StringUtils.isBlank(shopInfoEntity.getWarehouseId())? \"\":shopInfoEntity.getWarehouseId())"),

            @Mapping(target = "asin", source = "csvEntity.asin"),
            @Mapping(target = "msku", source = "csvEntity.sku"),
            @Mapping(target = "fnSku", source = "csvEntity.fnsku"),
            // 库龄报告信息
            @Mapping(target = "inventoryAge0To30Days", expression = "java(null== csvEntity ? 0: DmpFbaInventoryConverter.checkAndGetInt(csvEntity.getInvAge0To30Days()))"),
            @Mapping(target = "inventoryAge31To60Days", expression = "java(null== csvEntity ? 0: DmpFbaInventoryConverter.checkAndGetInt(csvEntity.getInvAge31To60Days()))"),
            @Mapping(target = "inventoryAge61To90Days", expression = "java(null== csvEntity ? 0: DmpFbaInventoryConverter.checkAndGetInt(csvEntity.getInvAge61To90Days()))"),
            @Mapping(target = "inventoryAge91To180Days", expression = "java(null== csvEntity ? 0: DmpFbaInventoryConverter.checkAndGetInt(csvEntity.getInvAge91To180Days()))"),
            @Mapping(target = "inventoryAge181To270Days", expression = "java(null== csvEntity ? 0: DmpFbaInventoryConverter.checkAndGetInt(csvEntity.getInvAge181To270Days()))"),
            @Mapping(target = "inventoryAge271To365Days", expression = "java(null== csvEntity ? 0: DmpFbaInventoryConverter.checkAndGetInt(csvEntity.getInvAge271To365Days()))"),
            @Mapping(target = "inventoryAge365PlusDays", expression = "java(null== csvEntity ? 0: DmpFbaInventoryConverter.checkAndGetInt(csvEntity.getInvAge365PlusDays()))"),

    })
    FbaInventoryEntity reportFbaInventoryPlanningToEntity(
            ReportFbaInventoryPlanningCsvEntity csvEntity,
            ShopInfoEntity shopInfoEntity,
            ListingInfoWithSkuMappingDTO listingInfoWithSkuMappingDTO,
            String dataStartTime,
            String dataEndTime
    );

    static int checkAndGetInt(String numStr) {
        if (StringUtils.isBlank(numStr)){
            return 0;
        } else {
            return Integer.parseInt(numStr);
        }
    }


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
            @Mapping(target = "skuNo", expression = "java(null == listingInfoWithSkuMappingDTO ? \"\":listingInfoWithSkuMappingDTO.checkAndGetProductSkuNo())"),
            @Mapping(target = "productName", expression = "java(null == listingInfoWithSkuMappingDTO ? \"\":listingInfoWithSkuMappingDTO.checkAndGetProductName())"),
            @Mapping(target = "dataStartTime", expression = "java(java.time.OffsetDateTime.parse(dataStartTime))"),
            @Mapping(target = "dataEndTime", expression = "java(java.time.OffsetDateTime.parse(dataEndTime))"),
            @Mapping(target = "warehouseName", expression = "java(org.apache.commons.lang3.StringUtils.isBlank(shopInfoEntity.getWarehouseName())? \"\":shopInfoEntity.getWarehouseName())"),
            @Mapping(target = "warehouseId", expression = "java(org.apache.commons.lang3.StringUtils.isBlank(shopInfoEntity.getWarehouseId())? \"\":shopInfoEntity.getWarehouseId())"),
            // 库存报告管理信息
            @Mapping(target = "asin", source = "esvEntity.asin"),
            @Mapping(target = "msku", source = "esvEntity.sku"),
            @Mapping(target = "fnSku", source = "esvEntity.fnsku"),
            @Mapping(target = "deliveryChannels", expression = "java(esvEntity.switchDeliveryChannels())"),
            @Mapping(target = "fbmFulfillableQty", expression = "java(esvEntity.mfnFulfillableQuantityCheckToInt())"),
            @Mapping(target = "inboundWorkingQty", expression = "java(esvEntity.afnInboundWorkingQuantityCheckToInt())"),
            @Mapping(target = "inboundShippedQty", expression = "java(esvEntity.afnInboundShippedQuantityCheckToInt())"),
            @Mapping(target = "inboundReceivingQty", expression = "java(esvEntity.afnInboundReceivingQuantityCheckToInt())"),
            @Mapping(target = "fulfillableQty", expression = "java(esvEntity.afnFulfillableQuantityCheckToInt())"),
            @Mapping(target = "reservedQty", expression = "java(esvEntity.afnReservedQuantityCheckToInt())"),
            @Mapping(target = "researchingQty", expression = "java(esvEntity.afnResearchingQuantityCheckToInt())"),
            @Mapping(target = "unsellableQty", expression = "java(esvEntity.afnUnsellableQuantityCheckToInt())"),
    })
    FbaInventoryEntity reportFbaMyiUnSuppressedInventoryToEntity(
            ReportFbaMyiUnsuppressedInventoryMongoDTO esvEntity,
            ShopInfoEntity shopInfoEntity,
            ListingInfoWithSkuMappingDTO listingInfoWithSkuMappingDTO,
            String dataStartTime,
            String dataEndTime
    );

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
            @Mapping(target = "skuNo", expression = "java(null == listingInfoWithSkuMappingDTO ? \"\":listingInfoWithSkuMappingDTO.checkAndGetProductSkuNo())"),
            @Mapping(target = "productName", expression = "java(null == listingInfoWithSkuMappingDTO ? \"\":listingInfoWithSkuMappingDTO.checkAndGetProductName())"),
            @Mapping(target = "dataStartTime", expression = "java(java.time.OffsetDateTime.parse(dataStartTime))"),
            @Mapping(target = "dataEndTime", expression = "java(java.time.OffsetDateTime.parse(dataEndTime))"),
            @Mapping(target = "warehouseName", expression = "java(org.apache.commons.lang3.StringUtils.isBlank(shopInfoEntity.getWarehouseName())? \"\":shopInfoEntity.getWarehouseName())"),
            @Mapping(target = "warehouseId", expression = "java(org.apache.commons.lang3.StringUtils.isBlank(shopInfoEntity.getWarehouseId())? \"\":shopInfoEntity.getWarehouseId())"),
            // 库存报告管理信息
            @Mapping(target = "asin", source = "esvEntity.asin"),
            @Mapping(target = "msku", source = "esvEntity.sku"),
            @Mapping(target = "fnSku", source = "esvEntity.fnsku"),
            @Mapping(target = "deliveryChannels", expression = "java(esvEntity.switchDeliveryChannels())"),
            @Mapping(target = "fbmFulfillableQty", expression = "java(esvEntity.mfnFulfillableQuantityCheckToInt())"),
            @Mapping(target = "inboundWorkingQty", expression = "java(esvEntity.afnInboundWorkingQuantityCheckToInt())"),
            @Mapping(target = "inboundShippedQty", expression = "java(esvEntity.afnInboundShippedQuantityCheckToInt())"),
            @Mapping(target = "inboundReceivingQty", expression = "java(esvEntity.afnInboundReceivingQuantityCheckToInt())"),
            @Mapping(target = "fulfillableQty", expression = "java(esvEntity.afnFulfillableQuantityCheckToInt())"),
            @Mapping(target = "reservedQty", expression = "java(esvEntity.afnReservedQuantityCheckToInt())"),
            @Mapping(target = "researchingQty", expression = "java(esvEntity.afnResearchingQuantityCheckToInt())"),
            @Mapping(target = "unsellableQty", expression = "java(esvEntity.afnUnsellableQuantityCheckToInt())"),
    })
    FbaInventoryEntity reportFbaMyiAllInventoryToEntity(
            ReportFbaMyiAllInventoryMongoDTO esvEntity,
            ShopInfoEntity shopInfoEntity,
            ListingInfoWithSkuMappingDTO listingInfoWithSkuMappingDTO,
            String dataStartTime,
            String dataEndTime
    );
}
