
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
 * 多渠道订单消费映射工具类
 * </p>
 *
 * @author Jim
 * @since 2024-05-24
 */
@Mapper
@Component
public interface SoMultiChannelConsumerConverter {
    SoMultiChannelConsumerConverter INSTANCE = Mappers.getMapper(SoMultiChannelConsumerConverter.class);


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
            @Mapping(target = "warehouseId", constant = ""),
            @Mapping(target = "warehouseName", constant = ""),
            @Mapping(target = "warehouseOrgId", constant = ""),
            @Mapping(target = "warehouseOrgName", constant = ""),
            @Mapping(target = "warehouseLocation", constant = ""),
            @Mapping(target = "skuId", source = "skuId"),
            @Mapping(target = "skuNo", source = "skuNo"),
            @Mapping(target = "imageUrl", source = "imageUrl"),
            @Mapping(target = "platformSpuNo", source = "platformSpuNo"),
    })
    SoMultiChannelDetailEntity convertNewDetail(PlatformOrderDetailDTO detailDTO, String mainId, String skuId, String skuNo, String imageUrl, String platformSpuNo);


    @Mappings({
            // 更新的内容
            @Mapping(target = "platformSkuNo", source = "detailDTO.platformSkuNo"),
//            @Mapping(target = "platformSpuNo", source = "detailDTO.platformSpuNo"),
            @Mapping(target = "warehouseSkuNo", source = "detailDTO.warehouseSkuNo"),
            @Mapping(target = "qty", source = "detailDTO.qty"),
            @Mapping(target = "price", source = "detailDTO.price"),
            @Mapping(target = "amount", source = "detailDTO.amount"),
            @Mapping(target = "currency", source = "detailDTO.currency"),
            @Mapping(target = "exchangeRate", source = "detailDTO.exchangeRate"),
            @Mapping(target = "advicePrice", source = "detailDTO.advicePrice"),
            @Mapping(target = "sourcePlatform", source = "detailDTO.sourcePlatform"),
            @Mapping(target = "labelJson", source = "detailDTO.labelJson"),
            // 历史实体
            @Mapping(target = "mainId", source = "oldEntity.mainId"),
            @Mapping(target = "warehouseId", source = "oldEntity.warehouseId"),
            @Mapping(target = "warehouseName", source = "oldEntity.warehouseName"),
            @Mapping(target = "taxCost", source = "oldEntity.taxCost"),
            @Mapping(target = "sourceDetailId", source = "oldEntity.sourceDetailId"),
            @Mapping(target = "warehouseOrgId", source = "oldEntity.warehouseOrgId"),
            @Mapping(target = "warehouseOrgName", source = "oldEntity.warehouseOrgName"),
            @Mapping(target = "warehouseLocation", source = "oldEntity.warehouseLocation"),
            @Mapping(target = "platformLineNumber", source = "oldEntity.platformLineNumber"),
            // 映射关系
            @Mapping(target = "skuId", source = "skuId"),
            @Mapping(target = "skuNo", source = "skuNo"),
            @Mapping(target = "imageUrl", source = "imageUrl"),
            @Mapping(target = "platformSpuNo", source = "platformSpuNo"),

    })
    SoMultiChannelDetailEntity convertUpdateDetail(SoMultiChannelDetailEntity oldEntity, PlatformOrderDetailDTO detailDTO, String skuId, String skuNo, String imageUrl, String platformSpuNo);


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
            @Mapping(target = "remark", expression = "java(org.apache.commons.lang3.StringUtils.isBlank(oldEntity.getRemark()) ? dto.getRemark() : oldEntity.getRemark())"),
            @Mapping(target = "isCancel", source = "dto.isCancel"),
            @Mapping(target = "sellerOrderCode",source = "dto.sellerOrderCode"),
            // 历史实体
            @Mapping(target = "exchangeRate", source = "oldEntity.exchangeRate"),
            @Mapping(target = "code", source = "oldEntity.code"),
            @Mapping(target = "approveStatus", source = "oldEntity.approveStatus"),
            @Mapping(target = "platformCode", source = "oldEntity.platformCode"),
            @Mapping(target = "dictPlatform", source = "oldEntity.dictPlatform"),
            //@Mapping(target = "shopId", source = "oldEntity.shopId"),
            @Mapping(target = "orgId", source = "oldEntity.orgId"),
            @Mapping(target = "orgName", source = "oldEntity.orgName"),
            @Mapping(target = "sourceType", source = "oldEntity.sourceType"),
            @Mapping(target = "sourceId", source = "oldEntity.sourceId"),
            @Mapping(target = "sourceCode", source = "oldEntity.sourceCode"),
            @Mapping(target = "shopName", source = "oldEntity.shopName"),
    })
    SoMultiChannelEntity convertUpdateMainOrder(SoMultiChannelEntity oldEntity, PlatformOrderDTO dto);
}
