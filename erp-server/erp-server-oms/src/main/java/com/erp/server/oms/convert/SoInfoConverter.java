package com.erp.server.oms.convert;

import com.erp.model.dmp.entity.BiOrderInfoEntity;
import com.erp.model.dmp.entity.BiOrderItemSplitEntity;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.wms.dto.SoOutstockDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Mapper
@Component
public interface SoInfoConverter {

    SoInfoConverter INSTANCE = Mappers.getMapper(SoInfoConverter.class);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "platformOrderId", source = "code"),
            @Mapping(target = "canSend", constant = "1"),
            @Mapping(target = "isReturned", constant = "2"),
            @Mapping(target = "isRefund", constant = "2"),
            @Mapping(target = "salesRecordNumber", source = "code"),
            @Mapping(target = "platformOrderStatus", source = "approveStatus"),
            @Mapping(target = "orderFee", source = "allAmountLc"),
            @Mapping(target = "sourcePlatform", constant = "soInfo"),
            @Mapping(target = "isUnion", constant = "2"),
            @Mapping(target = "isSplit", constant = "2"),
            @Mapping(target = "isResend", constant = "2"),
            @Mapping(target = "hasGoods", constant = "0"),
            @Mapping(target = "manStreet", source = "receiveAddress"),
            @Mapping(target = "manPhone", source = "telNumber"),
            @Mapping(target = "fbaFlag", constant = "1"),
            @Mapping(target = "sellerMessage", source = "remark"),
            @Mapping(target = "currencyCode", source = "currency"),
            @Mapping(target = "platformFee", constant = "0"),
            @Mapping(target = "subsidyAmount", constant = "0"),
            @Mapping(target = "platformSign", constant = "erp-oms"),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "companyId", source = "salesOrgId"),
            @Mapping(target = "companyName", source = "salesOrgName"),
            @Mapping(target = "platformCreateTime", source = "createTime"),
            @Mapping(target = "chargeName", source = "sellerName"),
            @Mapping(target = "deptId", source = "salesDeptId"),
            @Mapping(target = "chargeId", source = "sellerId")
    })
    BiOrderInfoEntity soInfoToDmpOrder(SoInfoEntity soInfo);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "itemId", source = "skuNo"),
            @Mapping(target = "platformSku", source = "skuNo"),
            @Mapping(target = "platformQuantity", source = "qty"),
            @Mapping(target = "sellPriceOrigin", source = "price"),
            @Mapping(target = "quantity", source = "qty"),
            @Mapping(target = "productUnit", constant = "pcs"),
            @Mapping(target = "isGift", expression = "java(SoInfoConverter.getIsGift(soDetailEntity))"),
            @Mapping(target = "hasGoods", constant = "1"),
            @Mapping(target = "isCombo", constant = "2"),
            @Mapping(target = "itemRemark", source = "remark"),
            @Mapping(target = "status", constant = "2"),
            @Mapping(target = "stockGrid", source = "warehouseLocation"),
            @Mapping(target = "skuNo", source = "skuNo"),
            @Mapping(target = "erpOrderItemId", source = "id"),
            @Mapping(target = "currencyRate", source = "exchangeRate"),
            @Mapping(target = "cnySettleRate", source = "exchangeRate"),
            @Mapping(target = "sourceItemId", source = "id")
    })
    BiOrderItemSplitEntity soDetailToDmpOrderItem(SoDetailEntity soDetailEntity);

    @Mappings({
            @Mapping(target = "id", source = "soInfoEntity.id"),
            @Mapping(target = "code", source = "soInfoEntity.code"),
            @Mapping(target = "detailId", source = "soDetailEntity.id"),
            @Mapping(target = "customerId", source = "soInfoEntity.customerId"),
            @Mapping(target = "customerName", source = "customerInfo.name"),
            @Mapping(target = "skuId", source = "soDetailEntity.skuId"),
            @Mapping(target = "skuNo", source = "soDetailEntity.skuNo"),
            @Mapping(target = "billDate", expression = "java(java.time.LocalDate.now())"),
            @Mapping(target = "salesQty", source = "soDetailEntity.qty"),
            @Mapping(target = "warehouseId", source = "soInfoEntity.warehouseId"),
            @Mapping(target = "warehouseOrgId", source = "soInfoEntity.warehouseOrgId"),
            @Mapping(target = "warehouseOrgName", source = "soInfoEntity.warehouseOrgName"),
            @Mapping(target = "sellerId", source = "soInfoEntity.sellerId"),
            @Mapping(target = "sellerName", source = "soInfoEntity.sellerName"),
            @Mapping(target = "warehouseLocation", source = "soDetailEntity.warehouseLocation"),
            @Mapping(target = "remark", ignore = true),
    })
    SoInfoDTO.GenerateSoOutView soDetailToGenerateSoOutView(SoDetailEntity soDetailEntity, SoInfoEntity soInfoEntity, CustomerInfoEntity customerInfo);

    static int getIsGift(SoDetailEntity soDetailEntity) {
        if (Optional.ofNullable(soDetailEntity.getIsGift()).isPresent()) {
            return soDetailEntity.getIsGift() ? 1 : 2;
        } else {
            return 2;
        }
    }
    @Mappings({
            @Mapping(target = "soId", source = "id"),
            @Mapping(target = "sourceId", source = "id"),
            @Mapping(target = "sourceType", expression = "java(com.common.business.enums.SourceTypeEnum.SO_INFO.getCode())"),
            @Mapping(target = "sourceCode", source = "code"),
            @Mapping(target = "deliveryOrgId", source = "warehouseOrgId"),
            @Mapping(target = "deliveryOrgName", source = "warehouseOrgName"),
            @Mapping(target = "orderType",  expression = "java(com.common.business.enums.OrderTypeEnum.B2B.getCode())"),
            @Mapping(target = "planDeliveryDate", source = "billDate"),
            @Mapping(target = "billDate", source = "billDate"),
            @Mapping(target = "sellerId", source = "sellerId"),
            @Mapping(target = "sellerName", source = "sellerName"),
            @Mapping(target = "warehouseId", source = "warehouseId"),
            @Mapping(target = "soDetailId", source = "detailId"),
            @Mapping(target = "sourceDetailId", source = "detailId"),
            @Mapping(target = "skuId", source = "skuId"),
            @Mapping(target = "skuNo", source = "skuNo"),
            @Mapping(target = "qty", source = "actualDeliveryQty"),
            @Mapping(target = "warehouseLocation", source = "warehouseLocation"),
            @Mapping(target = "remark", source = "remark"),
    })
    SoOutstockDTO.GenerateSoOutstockViewDTO soOutViewToGenerateSoOut(SoInfoDTO.GenerateSoOutView soOutView);
}
