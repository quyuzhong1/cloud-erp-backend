package com.erp.server.dmp.convert;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.erp.model.dmp.entity.*;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.dto.SoB2cDetailDTO;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.oms.entity.SoReturnDetailEntity;
import com.erp.model.oms.entity.SoReturnEntity;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.entity.SoOutstockEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * @author zdy
 * @ClassName DmpOrderConverter
 * @description: 映射工具类
 * @date 2023年10月12日
 * @version: 1.0
 */
@Mapper
@Component
public interface DmpOrderConverter {
    DmpOrderConverter INSTANCE = Mappers.getMapper(DmpOrderConverter.class);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "platformOrderId", source = "code"),
            @Mapping(target = "canSend", constant = "1"),
            @Mapping(target = "isReturned", constant = "2"),
            @Mapping(target = "isRefund", constant = "2"),
            @Mapping(target = "salesRecordNumber", source = "code"),
            @Mapping(target = "platformOrderStatus", source = "approveStatus"),
            @Mapping(target = "orderFee", source = "allAmountLc"),
            @Mapping(target = "sourcePlatform", constant = "B2B"),
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
    DmpOrderInfoEntity soInfoToDmpOrder(SoInfoEntity soInfo);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "itemId", source = "skuNo"),
            @Mapping(target = "platformSku", source = "skuNo"),
            @Mapping(target = "platformQuantity", source = "qty"),
            @Mapping(target = "sellPriceOrigin", source = "price"),
            @Mapping(target = "quantity", source = "qty"),
            @Mapping(target = "productUnit", constant = "pcs"),
            @Mapping(target = "isGift", expression = "java(DmpOrderConverter.getIsGift(soDetailEntity))"),
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
    DmpOrderItemEntity soDetailToDmpOrderItem(SoDetailEntity soDetailEntity);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "billNo", source = "code"),
            @Mapping(target = "logisticsNo", source = "trackNo"),
            @Mapping(target = "platformName", constant = "B2B"),
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

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "platformOrderId", source = "code"),
            @Mapping(target = "shopName", source = "customerName"),
            @Mapping(target = "status", constant = "4"),
            @Mapping(target = "salesRecordNumber", source = "code"),
            @Mapping(target = "platformName", source = "type"),
            @Mapping(target = "buyerName", source = "customerName"),
            @Mapping(target = "employeeId", source = "sellerId"),
            @Mapping(target = "employeeName", source = "sellerName"),
            @Mapping(target = "returnCreateTime", source = "createTime"),
//            @Mapping(target = "refundTime", source = "billDate"),
            @Mapping(target = "currencyCode", source = "currency"),
            @Mapping(target = "companyId", source = "salesOrgId"),
            @Mapping(target = "companyName", source = "salesOrgName"),
            @Mapping(target = "returnCode", source = "code"),
            @Mapping(target = "chargeId", source = "sellerId"),
            @Mapping(target = "chargeName", source = "sellerName"),
            @Mapping(target = "platformReturnCode", source = "code")
    })
    DmpReturnOrderInfoEntity soReturnOrderToDmpReturn(SoReturnEntity soReturnEntity);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "skuNo", source = "skuNo"),
            @Mapping(target = "quantity", source = "returnQty"),
            @Mapping(target = "status", constant = "2"),
            @Mapping(target = "originalSkuNo", source = "skuNo")
    })
    DmpReturnOrderItemEntity soReturnOrderToDmpReturnItem(SoReturnDetailEntity soReturnDetailEntity);

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
            @Mapping(target = "sourcePlatform", constant = "ERP"),
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
    })
    DmpOrderInfoEntity soB2cToDmpOrder(SoB2cDTO.ViewDTO viewDTO);


    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "orderId", source = "detailViewDTO.mainId"),
            @Mapping(target = "itemId", source = "detailViewDTO.id"),
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
            @Mapping(target = "erpOrderItemId", source = "detailViewDTO.skuId"),
            @Mapping(target = "currencyRate", source = "detailViewDTO.exchangeRate"),
            @Mapping(target = "cnySettleRate", source = "detailViewDTO.exchangeRate"),
            @Mapping(target = "sourceItemId", source = "id")
    })
    DmpOrderItemEntity soB2cToDmpOrderItem(SoB2cDetailDTO.ViewDTO detailViewDTO);
    List<DmpOrderItemEntity> soB2cToDmpOrderItem(List<SoB2cDetailDTO.ViewDTO> viewDTO);

    static int getIsGift(SoDetailEntity soDetailEntity) {
        if (Optional.ofNullable(soDetailEntity.getIsGift()).isPresent()) {
            return soDetailEntity.getIsGift() ? 1 : 2;
        } else {
            return 2;
        }
    }
}
