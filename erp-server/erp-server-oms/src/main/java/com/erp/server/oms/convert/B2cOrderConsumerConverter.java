package com.erp.server.oms.convert;

import com.common.business.dto.*;
import com.erp.model.oms.dto.SoB2cReceiverDTO;
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
            @Mapping(target = "platformSpuNo", source = "platformSpuNo"),
            @Mapping(target = "saleFee", source = "detailDTO.saleFee"),
            @Mapping(target = "variantProperty", source = "detailDTO.variantProperty"),
    })
    SoB2cDetailEntity convertNewDetail(PlatformOrderDetailDTO detailDTO, String mainId, String skuId, String skuNo, String imageUrl, String platformSpuNo);



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
            @Mapping(target = "platformOrderStatus", source = "dto.platformOrderStatus"),
            @Mapping(target = "remark", expression = "java(org.apache.commons.lang3.StringUtils.isBlank(oldEntity.getRemark()) || \"延迟发货\".equals(oldEntity.getRemark()) ? dto.getRemark() : oldEntity.getRemark())"),
            @Mapping(target = "isCancel", source = "dto.isCancel"),
            @Mapping(target = "totalTaxFee", source = "dto.totalTaxFee"),
            @Mapping(target = "afterTaxAmount", source = "dto.afterTaxAmount"),
            @Mapping(target = "totalDiscount", source = "dto.totalDiscount"),
            // 历史实体
            @Mapping(target = "exchangeRate", source = "oldEntity.exchangeRate"),
            @Mapping(target = "code", source = "oldEntity.code"),
            @Mapping(target = "approveStatus", source = "oldEntity.approveStatus"),
            @Mapping(target = "platformCode", source = "oldEntity.platformCode"),
            @Mapping(target = "dictPlatform", source = "oldEntity.dictPlatform"),
            @Mapping(target = "thirdCode", source = "oldEntity.thirdCode"),
            @Mapping(target = "thirdSystem", source = "oldEntity.thirdSystem"),
            //@Mapping(target = "shopId", source = "oldEntity.shopId"),
            @Mapping(target = "orgId", source = "oldEntity.orgId"),
            @Mapping(target = "orgName", source = "oldEntity.orgName"),
            @Mapping(target = "isIntercept", source = "oldEntity.isIntercept"),
            @Mapping(target = "interceptRemark", source = "oldEntity.interceptRemark"),
            @Mapping(target = "sourceType", source = "oldEntity.sourceType"),
            @Mapping(target = "sourceId", source = "oldEntity.sourceId"),
            @Mapping(target = "sourceCode", source = "oldEntity.sourceCode"),
            @Mapping(target = "abnormalType", source = "oldEntity.abnormalType"),
            @Mapping(target = "isNotMerge", source = "oldEntity.isNotMerge"),
            @Mapping(target = "syncKingdeeId", source = "oldEntity.syncKingdeeId"),
            @Mapping(target = "shopName", source = "oldEntity.shopName"),
//            @Mapping(target = "totalDiscount", source = "oldEntity.totalDiscount"),
            @Mapping(target = "totalCancelGoodsAmount", source = "oldEntity.totalCancelGoodsAmount"),
            @Mapping(target = "cancelGoodsCurrency", source = "oldEntity.cancelGoodsCurrency"),
            @Mapping(target = "sellerOrderCode",ignore = true),
            @Mapping(target = "nfeInvoiceStatus", source = "dto.nfeInvoiceStatus"),
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
            // 更新的内容
            @Mapping(target = "platformSkuNo", source = "detailDTO.platformSkuNo"),
//            @Mapping(target = "platformSpuNo", source = "detailDTO.platformSpuNo"),
            @Mapping(target = "warehouseSkuNo", source = "detailDTO.warehouseSkuNo"),
            @Mapping(target = "qty", source = "detailDTO.qty"),
            @Mapping(target = "price", source = "detailDTO.price"),
            @Mapping(target = "taxRate", source = "detailDTO.taxRate"),
            @Mapping(target = "amount", source = "detailDTO.amount"),
            @Mapping(target = "currency", source = "detailDTO.currency"),
            @Mapping(target = "exchangeRate", source = "detailDTO.exchangeRate"),
            @Mapping(target = "advicePrice", source = "detailDTO.advicePrice"),
            @Mapping(target = "sourcePlatform", source = "detailDTO.sourcePlatform"),
            @Mapping(target = "labelJson", source = "detailDTO.labelJson"),
            @Mapping(target = "platformSkuId", source = "detailDTO.platformSkuId"),
            // 历史实体
            @Mapping(target = "mainId", source = "oldEntity.mainId"),
            @Mapping(target = "warehouseId", source = "oldEntity.warehouseId"),
            @Mapping(target = "warehouseName", source = "oldEntity.warehouseName"),
            @Mapping(target = "taxCost", source = "oldEntity.taxCost"),
            @Mapping(target = "sourceDetailId", source = "oldEntity.sourceDetailId"),
            @Mapping(target = "thirdDetailId", source = "oldEntity.thirdDetailId"),
            @Mapping(target = "kingdeeDetailId", source = "oldEntity.kingdeeDetailId"),
            @Mapping(target = "warehouseOrgId", source = "oldEntity.warehouseOrgId"),
            @Mapping(target = "warehouseOrgName", source = "oldEntity.warehouseOrgName"),
            @Mapping(target = "warehouseLocation", source = "oldEntity.warehouseLocation"),
            @Mapping(target = "platformLineNumber", source = "oldEntity.platformLineNumber"),
            @Mapping(target = "platformPackageId", source = "oldEntity.platformPackageId"),
            @Mapping(target = "extendData", source = "oldEntity.extendData"),
            // 映射关系
            @Mapping(target = "skuId", source = "skuId"),
            @Mapping(target = "skuNo", source = "skuNo"),
            @Mapping(target = "imageUrl", source = "imageUrl"),
            @Mapping(target = "saleFee", source = "detailDTO.saleFee"),
            @Mapping(target = "platformSpuNo", source = "platformSpuNo"),
            @Mapping(target = "variantProperty", source = "detailDTO.variantProperty"),

    })
    SoB2cDetailEntity convertUpdateDetail(SoB2cDetailEntity oldEntity, PlatformOrderDetailDTO detailDTO, String skuId, String skuNo, String imageUrl, String platformSpuNo);


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
            @Mapping(target = "platformCost", source = "financeDTO.platformCost"),
            @Mapping(target = "accessoriesCost", source = "financeDTO.accessoriesCost"),
            @Mapping(target = "platformRate", source = "financeDTO.platformRate"),
            @Mapping(target = "vatRate", source = "financeDTO.vatRate"),
            @Mapping(target = "transferRate", source = "financeDTO.transferRate"),
            @Mapping(target = "platformCostType", source = "financeDTO.platformCostType"),
            @Mapping(target = "transferCostType", source = "financeDTO.transferCostType"),
            @Mapping(target = "vatCostType", source = "financeDTO.vatCostType"),
    })
    SoB2cFinanceEntity convertUpdateFinance(SoB2cFinanceEntity oldEntity, PlatformOrderFinanceDTO financeDTO);

    /**
     * 转换参数预览
     * @param soB2cReceiverEntity
     * @return
     */
    @Mappings({
            // 更新的内容
            @Mapping(target = "id", source = "soB2cReceiverEntity.id"),
            @Mapping(target = "name", source = "soB2cReceiverEntity.name"),
            @Mapping(target = "countryName", source = "soB2cReceiverEntity.countryName"),
            @Mapping(target = "loginId", source = "soB2cReceiverEntity.loginId"),
            @Mapping(target = "customerId", source = "soB2cReceiverEntity.customerId"),
            @Mapping(target = "email", source = "soB2cReceiverEntity.email"),
            @Mapping(target = "telNumber", source = "soB2cReceiverEntity.telNumber"),
            @Mapping(target = "firstAddress", source = "soB2cReceiverEntity.firstAddress"),
            @Mapping(target = "secondAddress", source = "soB2cReceiverEntity.secondAddress"),
            @Mapping(target = "cityName", source = "soB2cReceiverEntity.cityName"),
            @Mapping(target = "country", source = "soB2cReceiverEntity.country"),
            @Mapping(target = "provinceName", source = "soB2cReceiverEntity.provinceName"),
            @Mapping(target = "districtName", source = "soB2cReceiverEntity.districtName"),
            @Mapping(target = "receiverName", source = "soB2cReceiverEntity.receiverName"),
            @Mapping(target = "receiverTelNumber", source = "soB2cReceiverEntity.receiverTelNumber"),
            @Mapping(target = "postCode", source = "soB2cReceiverEntity.postCode"),
            @Mapping(target = "fullAddress", source = "soB2cReceiverEntity.fullAddress"),
            @Mapping(target = "receiverTaxNo", source = "soB2cReceiverEntity.receiverTaxNo"),
            @Mapping(target = "soB2cCode", source = "soB2cEntity.code"),
            @Mapping(target = "mainId", source = "soB2cReceiverEntity.mainId")
    })
    SoB2cReceiverDTO.ViewDTO convertReceiverToView(SoB2cReceiverEntity soB2cReceiverEntity,SoB2cEntity soB2cEntity);

    /**
     * 收件人信息重置
     * @param dto
     * @return
     */
    @Mappings({
            @Mapping(target = "createTime", source = "old.createTime"),
            @Mapping(target = "createUserId", source = "old.createUserId"),
            @Mapping(target = "createUserName", source = "old.createUserName"),
            @Mapping(target = "updateTime", source = "old.updateTime"),
            @Mapping(target = "updateUserId", source = "old.updateUserId"),
            @Mapping(target = "updateUserName", source = "old.updateUserName"),
            @Mapping(target = "isDeleted", source = "old.isDeleted"),
            @Mapping(target = "version", source = "old.version"),
            @Mapping(target = "customerId", source = "old.customerId"),
            @Mapping(target = "districtName", source = "old.districtName"),
            @Mapping(target = "email", source = "old.email"),
            @Mapping(target = "loginId", source = "old.loginId"),
            @Mapping(target = "name", source = "old.name"),
            @Mapping(target = "telNumber", source = "old.telNumber"),
            @Mapping(target = "id", source = "old.id"),
            @Mapping(target = "mainId", source = "old.mainId"),
            @Mapping(target = "country", source = "dto.country"),
            @Mapping(target = "countryName", source = "dto.countryName"),
            @Mapping(target = "provinceName", source = "dto.provinceName"),
            @Mapping(target = "cityName", source = "dto.cityName"),
            @Mapping(target = "postCode", source = "dto.postCode"),
            @Mapping(target = "receiverName", source = "dto.receiverName"),
            @Mapping(target = "receiverTelNumber", source = "dto.receiverTelNumber"),
            @Mapping(target = "receiverTaxNo", source = "dto.receiverTaxNo"),
            @Mapping(target = "firstAddress", source = "dto.firstAddress"),
            @Mapping(target = "secondAddress", source = "dto.secondAddress"),
            @Mapping(target = "fullAddress", source = "dto.fullAddress")

    })
    SoB2cReceiverEntity convertUpdateReceiverByDto(SoB2cReceiverDTO.UpdateBaseDTO dto,SoB2cReceiverEntity old);
}
