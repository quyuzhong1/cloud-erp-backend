package com.erp.server.oms.convert;

import com.common.business.dto.*;
import com.erp.model.oms.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * <p>
 * b2c订单消费映射工具类
 * </p>
 *
 * @author Jim
 * @since 2023-11-28
 */
@Mapper
@Component
public interface B2cOrderConsumerConverter {
    B2cOrderConsumerConverter INSTANCE = Mappers.getMapper(B2cOrderConsumerConverter.class);


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
            @Mapping(target = "mainId", source = "mainId"),
            @Mapping(target = "kingdeeDetailId", constant = ""),
            @Mapping(target = "warehouseId", constant = ""),
            @Mapping(target = "warehouseName", constant = ""),
            @Mapping(target = "warehouseOrgId", constant = ""),
            @Mapping(target = "warehouseOrgName", constant = ""),
            @Mapping(target = "warehouseLocation", constant = ""),
            @Mapping(target = "skuId", source = "skuId"),
            @Mapping(target = "skuNo", source = "skuNo"),
            @Mapping(target = "imageUrl", source = "imageUrl"),
    })
    SoB2cDetailEntity convertNewDetail(PlatformOrderDetailDTO detailDTO, String mainId, String skuId, String skuNo,String imageUrl);


    @Mappings({
            // 更新的内容
            @Mapping(target = "platformSkuNo", source = "detailDTO.platformSkuNo"),
            @Mapping(target = "platformSpuNo", source = "detailDTO.platformSpuNo"),
            @Mapping(target = "warehouseSkuNo", source = "detailDTO.warehouseSkuNo"),
            @Mapping(target = "qty", source = "detailDTO.qty"),
            @Mapping(target = "price", source = "detailDTO.price"),
            @Mapping(target = "amount", source = "detailDTO.amount"),
            @Mapping(target = "currency", source = "detailDTO.currency"),
            @Mapping(target = "exchangeRate", source = "detailDTO.exchangeRate"),
            @Mapping(target = "advicePrice", source = "detailDTO.advicePrice"),
            @Mapping(target = "sourcePlatform", source = "detailDTO.sourcePlatform"),
            // 历史实体
            @Mapping(target = "mainId", source = "oldEntity.mainId"),
            @Mapping(target = "warehouseId", source = "oldEntity.warehouseId"),
            @Mapping(target = "warehouseName", source = "oldEntity.warehouseName"),
            @Mapping(target = "taxCost", source = "oldEntity.taxCost"),
            @Mapping(target = "sourceDetailId", source = "oldEntity.sourceDetailId"),
            @Mapping(target = "labelJson", source = "oldEntity.labelJson"),
            @Mapping(target = "kingdeeDetailId", source = "oldEntity.kingdeeDetailId"),
            @Mapping(target = "warehouseOrgId", source = "oldEntity.warehouseOrgId"),
            @Mapping(target = "warehouseOrgName", source = "oldEntity.warehouseOrgName"),
            @Mapping(target = "warehouseLocation", source = "oldEntity.warehouseLocation"),
            @Mapping(target = "platformLineNumber", source = "oldEntity.platformLineNumber"),
            // 映射关系
            @Mapping(target = "skuId", source = "skuId"),
            @Mapping(target = "skuNo", source = "skuNo"),
            @Mapping(target = "imageUrl", source = "imageUrl"),

    })
    SoB2cDetailEntity convertUpdateDetail(SoB2cDetailEntity oldEntity, PlatformOrderDetailDTO detailDTO, String skuId, String skuNo,String imageUrl);


    @Mappings({
            @Mapping(target = "billDate", source = "dto.billDate"),
            @Mapping(target = "billStatus", source = "dto.billStatus"),
            @Mapping(target = "payStatus", source = "dto.payStatus"),
            @Mapping(target = "amount", source = "dto.amount"),
            @Mapping(target = "currency", source = "dto.currency"),
            @Mapping(target = "shippingFee", source = "dto.shippingFee"),
            @Mapping(target = "payTime", source = "dto.payTime"),
            @Mapping(target = "payAmount", source = "dto.payAmount"),
            @Mapping(target = "dictPayMethod", source = "dto.dictPayMethod"),
            @Mapping(target = "buyerRemark", source = "dto.buyerRemark"),
            @Mapping(target = "invalidStatus", source = "dto.invalidStatus"),
            @Mapping(target = "invalidType", source = "dto.invalidType"),
            @Mapping(target = "invalidRemark", source = "dto.invalidRemark"),
            @Mapping(target = "labelJson", source = "dto.labelJson"),
            @Mapping(target = "extendData", source = "dto.extendData"),
            @Mapping(target = "shopId",source = "dto.shopId"),
            @Mapping(target = "platformOrderCreateTime",source = "dto.platformOrderCreateTime"),
            // 历史实体
            @Mapping(target = "exchangeRate", source = "oldEntity.exchangeRate"),
            @Mapping(target = "code", source = "oldEntity.code"),
            @Mapping(target = "approveStatus", source = "oldEntity.approveStatus"),
            @Mapping(target = "platformCode", source = "oldEntity.platformCode"),
            @Mapping(target = "dictPlatform", source = "oldEntity.dictPlatform"),
            //@Mapping(target = "shopId", source = "oldEntity.shopId"),
            @Mapping(target = "remark", source = "oldEntity.remark"),
            @Mapping(target = "orgId", source = "oldEntity.orgId"),
            @Mapping(target = "orgName", source = "oldEntity.orgName"),
            @Mapping(target = "isIntercept", source = "oldEntity.isIntercept"),
            @Mapping(target = "interceptRemark", source = "oldEntity.interceptRemark"),
            @Mapping(target = "sourceType", source = "oldEntity.sourceType"),
            @Mapping(target = "sourceId", source = "oldEntity.sourceId"),
            @Mapping(target = "sourceCode", source = "oldEntity.sourceCode"),
            @Mapping(target = "abnormalType", source = "oldEntity.abnormalType"),
            @Mapping(target = "isNotMerge", source = "oldEntity.isNotMerge"),
            @Mapping(target = "syncKingdeeStatus", source = "oldEntity.syncKingdeeStatus"),
            @Mapping(target = "syncKingdeeTime", source = "oldEntity.syncKingdeeTime"),
            @Mapping(target = "syncKingdeeId", source = "oldEntity.syncKingdeeId"),
            @Mapping(target = "syncOperate", source = "oldEntity.syncOperate"),
            @Mapping(target = "shopName", source = "oldEntity.shopName"),
    })
    SoB2cEntity convertUpdateMainOrder(SoB2cEntity oldEntity, PlatformOrderDTO dto);


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
            @Mapping(target = "mainId", source = "mainId"),
            @Mapping(target = "weight", source = "allNetWeight"),
            @Mapping(target = "length", source = "maxLength"),
            @Mapping(target = "width", source = "maxWidth"),
            @Mapping(target = "height", source = "totalHeight"),
    })
    SoB2cLogisticsEntity convertNewLogistics(PlatformOrderLogisticsDTO dto, String mainId, BigDecimal allNetWeight, BigDecimal maxLength, BigDecimal maxWidth, BigDecimal totalHeight);


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
            @Mapping(target = "mainId", source = "mainId"),
    })
    SoB2cReceiverEntity convertNewReceiver(PlatformOrderReceiverDTO dto, String mainId);


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
            @Mapping(target = "mainId", source = "mainId"),
            @Mapping(target = "currency", source = "dto.currency"),
            @Mapping(target = "shippingCost", source = "dto.shippingCost"),
            @Mapping(target = "itemCost", source = "dto.itemCost"),
            @Mapping(target = "logisticsCost", source = "dto.logisticsCost"),
            @Mapping(target = "accessoriesCost", source = "dto.accessoriesCost"),
            @Mapping(target = "platformRate", source = "dto.platformRate"),
            @Mapping(target = "vatRate", source = "dto.vatRate"),
            @Mapping(target = "transferRate", source = "dto.transferRate"),
            @Mapping(target = "platformCostType", source = "dto.platformCostType"),
            @Mapping(target = "transferCostType", source = "dto.transferCostType"),
            @Mapping(target = "vatCostType", source = "dto.vatCostType"),
    })
    SoB2cFinanceEntity convertNewFinance(PlatformOrderFinanceDTO dto, String mainId);


    @Mappings({
            // 更新的内容
            @Mapping(target = "mainId", source = "oldEntity.mainId"),
            @Mapping(target = "currency", source = "financeDTO.currency"),
            @Mapping(target = "shippingCost", source = "financeDTO.shippingCost"),
            @Mapping(target = "itemCost", source = "financeDTO.itemCost"),
            @Mapping(target = "logisticsCost", source = "financeDTO.logisticsCost"),
            @Mapping(target = "accessoriesCost", source = "financeDTO.accessoriesCost"),
            @Mapping(target = "platformRate", source = "financeDTO.platformRate"),
            @Mapping(target = "vatRate", source = "financeDTO.vatRate"),
            @Mapping(target = "transferRate", source = "financeDTO.transferRate"),
            @Mapping(target = "platformCostType", source = "financeDTO.platformCostType"),
            @Mapping(target = "transferCostType", source = "financeDTO.transferCostType"),
            @Mapping(target = "vatCostType", source = "financeDTO.vatCostType"),
    })
    SoB2cFinanceEntity convertUpdateFinance(SoB2cFinanceEntity oldEntity, PlatformOrderFinanceDTO financeDTO);
}
