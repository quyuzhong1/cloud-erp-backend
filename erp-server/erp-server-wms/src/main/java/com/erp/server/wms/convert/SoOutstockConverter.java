package com.erp.server.wms.convert;

import com.erp.model.dmp.entity.DmpDeliveryDetailInfoEntity;
import com.erp.model.dmp.entity.DmpDeliveryDetailItemEntity;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import com.erp.model.dmp.entity.DmpOrderItemSplitEntity;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.dto.SoB2cDetailDTO;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.server.wms.convert.tool.TypeConversionWorker;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper(uses = TypeConversionWorker.class)
@Component
public interface SoOutstockConverter {
    SoOutstockConverter INSTANCE = Mappers.getMapper(SoOutstockConverter.class);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "platformOrderId", source = "code"),
            @Mapping(target = "buyerUserId", source = "viewDTO.receiverDTO.loginId"),
            @Mapping(target = "buyerName", source = "viewDTO.receiverDTO.name"),
            @Mapping(target = "shopNo", source = "shopId"),
            @Mapping(target = "shopName", source = "shopName"),
            @Mapping(target = "canSend", constant = "1"),
            @Mapping(target = "isReturned", constant = "2"),
            @Mapping(target = "isRefund", constant = "2"),
            @Mapping(target = "paidTime", source = "payTime"),
            @Mapping(target = "salesRecordNumber", source = "code"),
            @Mapping(target = "platformOrderStatus", source = "approveStatus"),
            @Mapping(target = "orderFee", source = "amount"),
            @Mapping(target = "sourcePlatform", constant = "soB2c"),
            @Mapping(target = "isUnion", constant = "2"),
            @Mapping(target = "isSplit", constant = "2"),
            @Mapping(target = "isResend", constant = "2"),
            @Mapping(target = "hasGoods", constant = "0"),
            @Mapping(target = "district", source = "viewDTO.receiverDTO.districtName"),
            @Mapping(target = "city", source = "viewDTO.receiverDTO.cityName"),
            @Mapping(target = "province", source = "viewDTO.receiverDTO.provinceName"),
            @Mapping(target = "manStreet", source = "viewDTO.receiverDTO.firstAddress"),
            @Mapping(target = "secondStreet", source = "viewDTO.receiverDTO.secondAddress"),
            @Mapping(target = "manPhone", source = "viewDTO.receiverDTO.telNumber"),
            @Mapping(target = "secondPhone", source = "viewDTO.receiverDTO.receiverTelNumber"),
            @Mapping(target = "fbaFlag", constant = "1"),
            @Mapping(target = "sellerMessage", source = "remark"),
            @Mapping(target = "currencyCode", source = "currency"),
            @Mapping(target = "currencyRate", source = "exchangeRate"),
            @Mapping(target = "itemTotal", source = "amount"),
            @Mapping(target = "platformFee", source = "viewDTO.financialInfoDTO.platformCost"),
            @Mapping(target = "subsidyAmount", constant = "0"),
            @Mapping(target = "countryNameEn", source = "viewDTO.receiverDTO.country"),
            @Mapping(target = "countryNameCn", source = "viewDTO.receiverDTO.countryName"),
            @Mapping(target = "platformSign", constant = "erp-oms"),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "companyId", source = "orgId"),
            @Mapping(target = "companyName", source = "orgName"),
            @Mapping(target = "platformCreateTime", source = "platformOrderCreateTime"),
            @Mapping(target = "shippingFee", source = "viewDTO.shippingFee"),
    })
    DmpOrderInfoEntity soB2cToDmpOrder(SoB2cDTO.ViewDTO viewDTO);


    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "orderId", source = "detailViewDTO.mainId"),
            @Mapping(target = "itemId", source = "detailViewDTO.skuId"),
            @Mapping(target = "platformSku", source = "detailViewDTO.platformSkuNo"),
            @Mapping(target = "platformQuantity", source = "detailViewDTO.qty"),
            @Mapping(target = "itemName", source = "detailViewDTO.productName"),
            @Mapping(target = "pictureUrl", source = "detailViewDTO.imageUrl"),
            @Mapping(target = "costPrice", source = "detailViewDTO.taxCost"),
            @Mapping(target = "sellPriceOrigin", source = "detailViewDTO.advicePrice"),
            @Mapping(target = "sellPrice", source = "detailViewDTO.amount"),
            @Mapping(target = "quantity", source = "detailViewDTO.qty"),
            @Mapping(target = "hasGoods", constant = "1"),
            @Mapping(target = "isCombo", constant = "2"),
            @Mapping(target = "status", constant = "2"),
            @Mapping(target = "specifics", constant = ""),
            @Mapping(target = "stockGrid", source = "detailViewDTO.warehouseLocation"),
            @Mapping(target = "skuNo", source = "detailViewDTO.skuNo"),
            @Mapping(target = "stockStatus", constant = "3"),
            @Mapping(target = "stockWarehouseId", source = "detailViewDTO.warehouseId"),
            @Mapping(target = "erpOrderItemId", source = "detailViewDTO.id"),
            @Mapping(target = "currencyRate", source = "detailViewDTO.exchangeRate"),
            @Mapping(target = "cnySettleRate", source = "detailViewDTO.exchangeRate"),
            @Mapping(target = "sourceItemId", source = "id")
    })
    DmpOrderItemSplitEntity soB2cToDmpOrderItem(SoB2cDetailDTO.ViewDTO detailViewDTO);
    List<DmpOrderItemSplitEntity> soB2cToDmpOrderItem(List<SoB2cDetailDTO.ViewDTO> viewDTO);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "billNo", source = "code"),
            @Mapping(target = "logisticsNo", source = "trackNo"),
            @Mapping(target = "platformName", source = "orderType"),
            @Mapping(target = "subsidyAmount", constant = "0"),
            @Mapping(target = "salesManId", source = "sellerId"),
            @Mapping(target = "salesManName", source = "sellerName"),
            @Mapping(target = "status", constant = "1"),
            @Mapping(target = "platformApproveTime", source = "approveTime"),
            @Mapping(target = "platformCreateTime", source = "createTime"),
            @Mapping(target = "platformUpdateTime", source = "updateTime"),
            @Mapping(target = "companyId", source = "warehouseOrgId"),
            @Mapping(target = "companyName", source = "warehouseOrgName"),
            @Mapping(target = "platformSign", constant = "erp-wms"),
//            @Mapping(target = "deliveryDate", source = "actualDeliveryDate"),
            @Mapping(target = "platformOrderId", source = "code")
    })
    DmpDeliveryDetailInfoEntity soOutstockToDmpDelivery(SoOutstockEntity soOutstockEntity);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "itemId", source = "skuId"),
            @Mapping(target = "platformSku", source = "skuNo"),
            @Mapping(target = "skuNo", source = "skuNo"),
            @Mapping(target = "quantity", source = "actualQty"),
            @Mapping(target = "stockName", source = "warehouseName"),
            @Mapping(target = "warehouseLocation", source = "warehouseLocation")
    })
    DmpDeliveryDetailItemEntity soOutstockToDmpDeliveryItem(SoOutstockDetailEntity detail);
}
