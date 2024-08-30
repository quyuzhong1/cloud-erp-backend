package com.erp.server.oms.convert;

import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.mapper.BigDecimalMapperWork;
import com.common.business.mapper.BooleanMapperWork;
import com.common.business.mapper.NumberMapperWork;
import com.common.business.mapper.ObjectMapperWork;
import com.erp.model.oms.dto.*;
import com.erp.model.oms.entity.*;
import com.erp.model.plm.dto.LogisticsProductDTO;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.tms.vo.request.LogisticsProductVO;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.dto.third.ThirdWarehouseCreateOutboundReq;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 映射工具类
 * </p>
 *
 * @author Jim
 * @since 2023-11-15
 */

@Component
@Mapper(uses = {ObjectMapperWork.class,NumberMapperWork.class})
public interface B2cOrderConverter {
    B2cOrderConverter INSTANCE = Mappers.getMapper(B2cOrderConverter.class);


    @Mappings({
            @Mapping(target = "customerId", source = "customerId"),
            @Mapping(target = "name", source = "name"),
            @Mapping(target = "email", source = "email"),
            @Mapping(target = "receiverName", source = "receiverName"),
            @Mapping(target = "receiverTelNumber", source = "receiverTelNumber"),
            @Mapping(target = "fullAddress", source = "fullAddress"),
            @Mapping(target = "postCode", source = "postCode"),
            @Mapping(target = "country", source = "country"),
            @Mapping(target = "countryName", source = "countryName"),
            @Mapping(target = "provinceName", source = "provinceName"),
            @Mapping(target = "cityName", source = "cityName"),
            @Mapping(target = "districtName", source = "districtName"),
            @Mapping(target = "firstAddress", source = "firstAddress"),
            @Mapping(target = "secondAddress", source = "secondAddress"),
            @Mapping(target = "receiverTaxNo", source = "receiverTaxNo")

    })
    LogisticsBillDTO.ReceiverDTO convertReceiver(SoB2cReceiverEntity receiverEntity);

    @Mappings({
            @Mapping(target = "weight", source = "weight"),
            @Mapping(target = "length", source = "length"),
            @Mapping(target = "width", source = "width"),
            @Mapping(target = "height", source = "height"),

    })
    LogisticsBillDTO.PackageDTO convertPackage(SoB2cLogisticsEntity soB2cLogisticsEntity);


    @Mappings({
            @Mapping(target = "skuId", source = "skuId"),
            @Mapping(target = "skuNo", source = "skuNo"),
            @Mapping(target = "qty", source = "qty"),
            @Mapping(target = "platformSkuNo", source = "platformSkuNo"),
            @Mapping(target = "platformSpuNo", source = "platformSpuNo"),
            @Mapping(target = "sourceDetailId", source = "sourceDetailId"),

    })
    LogisticsBillDTO.SkuDTO convertSku(SoB2cDetailEntity data);
    List<LogisticsBillDTO.SkuDTO> convertSku(List<SoB2cDetailEntity> detailList);


    @Mappings({
            @Mapping(target = "name", source = "receiverName"),
            @Mapping(target = "buyerName", source = "name"),
            @Mapping(target = "phone", source = "receiverTelNumber"),
            @Mapping(target = "countryCode", source = "country"),
            @Mapping(target = "province", source = "provinceName"),
            @Mapping(target = "city", source = "cityName"),
            @Mapping(target = "zipcode", source = "postCode"),
            @Mapping(target = "address1", source = "firstAddress"),
            @Mapping(target = "address2", source = "secondAddress"),
            @Mapping(target = "email", source = "email"),
            @Mapping(target = "district", source = "districtName"),
            @Mapping(target = "taxNumber", source = "receiverTaxNo"),

    })
    ThirdWarehouseCreateOutboundReq.ReceiverInfo convertThirdWarehouseReceiver(SoB2cReceiverEntity receiverEntity);


    @Mappings({
            @Mapping(target = "productSku", source = "warehouseSkuNo"),
            @Mapping(target = "quantity", source = "qty"),
    })
    ThirdWarehouseCreateOutboundReq.Item convertThirdWarehouseItem(SoB2cDetailEntity detail);
    List<ThirdWarehouseCreateOutboundReq.Item> convertThirdWarehouseItem(List<SoB2cDetailEntity> detailList);


    @Mappings({
            @Mapping(target = "skuId", source = "skuId"),
            @Mapping(target = "skuNo", source = "skuNo"),
            @Mapping(target = "planQty", source = "qty"),
            @Mapping(target = "actualQty", source = "qty"),
            @Mapping(target = "warehouseLocation", source = "warehouseLocation"),
            @Mapping(target = "sourceDetailId", source = "sourceDetailId"),
            @Mapping(target = "soDetailId", source = "soDetailId"),
    })
    SoOutstockDetailDTO.AddDTO convertOutstockDetail(SoB2cDetailDTO.OutstockDTO detail);
    List<SoOutstockDetailDTO.AddDTO> convertOutstockDetail(List<SoB2cDetailDTO.OutstockDTO> detailList);
    @Mappings({
            @Mapping(target = "soCode", source = "code"),
            @Mapping(target = "sourceId", source = "id"),
            @Mapping(target = "sourceCode", source = "code"),
            @Mapping(target = "sourceType", constant  ="soB2c"),
            @Mapping(target = "shopId", constant  ="shopId"),
            @Mapping(target = "shopName", constant  ="shopName"),
    })
    SoB2cDeliveryDTO.AddDTO convertDelivery(SoB2cEntity entity);

    @Mappings({
            @Mapping(target = "skuId", source = "skuId"),
            @Mapping(target = "skuNo", source = "skuNo"),
            @Mapping(target = "deliveryQty", source = "qty"),
            @Mapping(target = "sourceDetailId", source  ="id"),
    })
    SoB2cDeliveryDetailDTO.AddDTO convertDeliveryDetail(SoB2cDetailEntity item);
    List<SoB2cDeliveryDetailDTO.AddDTO> convertDeliveryDetail(List<SoB2cDetailEntity> list );


    @Mappings({
            @Mapping(target = "skuId", source = "skuId"),
            @Mapping(target = "skuNo", source = "skuNo"),
            @Mapping(target = "warehouseId", source = "warehouseId"),
    })
    SkuMappingDTO.ListingSkuParamDTO convertFindListingSku(SoB2cDetailEntity item);
    List<SkuMappingDTO.ListingSkuParamDTO> convertFindListingSku(List<SoB2cDetailEntity> detailList);


    @Mappings({
            @Mapping(target = "soId", source = "id"),
            @Mapping(target = "sourceId", source = "id"),
            @Mapping(target = "sourceCode", source = "code"),
            @Mapping(target = "sourceType", ignore = true),
            @Mapping(target = "soCode", source = "code"),
            @Mapping(target = "detailList", ignore = true),
    })
    SoB2cDeliveryInterceptDTO.AddDTO convertIntercept(SoB2cEntity entity);


    @Mappings({
            @Mapping(target = "skuId", source = "skuId"),
            @Mapping(target = "deliveryQty", source = "qty"),
            @Mapping(target = "warehouseId", source = "warehouseId"),
            @Mapping(target = "warehouseName", source = "warehouseName"),
            @Mapping(target = "warehouseLocation", source = "warehouseLocation"),
            @Mapping(target = "sourceDetailId", source = "id"),
    })
    SoB2cDeliveryInterceptDetailDTO.AddDTO convertInterceptDetail(SoB2cDetailEntity detailEntity);
    List<SoB2cDeliveryInterceptDetailDTO.AddDTO> convertInterceptDetail(List<SoB2cDetailEntity> detailList);


    @Mappings({
    })
    List<SoOutstockDetailDTO.ListingInfoWithSkuMappingGenDTO> skuMappingDTOListToGenDTOList(List<ListingInfoWithSkuMappingDTO> list);


    @Mappings({
    })
    SoOutstockDetailDTO.ListingInfoWithSkuMappingGenDTO skuMappingDTOToGenDTO(ListingInfoWithSkuMappingDTO sourceDTO);
    @Mappings({
            @Mapping(target = "soId", source = "soId", qualifiedByName = "objToString"),
            @Mapping(target = "soCode", source = "soCode",qualifiedByName = "objToString"),
            @Mapping(target = "soDetailId", source = "soDetailId", qualifiedByName = "objToString"),
            @Mapping(target = "skuId", source = "skuId", qualifiedByName = "objToString"),
            @Mapping(target = "skuNo", source = "skuNo", qualifiedByName = "objToString"),
            @Mapping(target = "qty", source = "qty", qualifiedByName = "objToString"),
            @Mapping(target = "declareCn", source = "declareCn", qualifiedByName = "objToString"),
            @Mapping(target = "declareEn", source = "declareEn", qualifiedByName = "objToString"),
            @Mapping(target = "toDeclarePrice", source = "toDeclarePrice", qualifiedByName = "objToBigDecimal"),
            @Mapping(target = "toCurrency", source = "toCurrency", qualifiedByName = "objToString"),
            @Mapping(target = "toCurrencySymbol", source = "toCurrencySymbol", qualifiedByName = "objToString"),
            @Mapping(target = "weight", source = "grossWeight", qualifiedByName = "objToBigDecimal"),
            @Mapping(target = "toCustomsCode", source = "toCustomsCode", qualifiedByName = "objToString"),
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "createUserId", ignore = true),
            @Mapping(target = "createUserName", ignore = true),
            @Mapping(target = "isDeleted", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "updateUserId", ignore = true),
            @Mapping(target = "updateUserName", ignore = true),
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "declareLabel", ignore = true)
    })
    SoB2cDeclareProductEntity convertDeclareProductByMap(Map<String, Object> detailMap);

    /**
     *
     * @param updateDTO
     * @return
     */
    @Mappings({
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "createUserId", ignore = true),
            @Mapping(target = "createUserName", ignore = true),
            @Mapping(target = "isDeleted", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "updateUserId", ignore = true),
            @Mapping(target = "updateUserName", ignore = true),
            @Mapping(target = "version", ignore = true),
    })
    SoB2cDeclareProductEntity convertDeclareProductByDto(SoB2cDeclareProductDTO.UpdateDTO updateDTO);

    /**
     * 申报信息转换
     * @param soB2cDeclareProductEntity
     * @param soB2cDetail
     * @param productDTO
     * @return
     */
    @Mappings({
            @Mapping(target = "skuNo", source = "soB2cDeclareProductEntity.skuNo"),
            @Mapping(target = "skuId", source = "soB2cDeclareProductEntity.skuId"),
            @Mapping(target = "id", source = "soB2cDeclareProductEntity.skuId"),
            @Mapping(target = "destCurrency", source = "soB2cDeclareProductEntity.toCurrency"),
            @Mapping(target = "destCurrencySymbol", source = "soB2cDeclareProductEntity.toCurrencySymbol"),
            @Mapping(target = "destDeclarePrice", source = "soB2cDeclareProductEntity.toDeclarePrice"),
            @Mapping(target = "weight", source = "soB2cDeclareProductEntity.weight", qualifiedByName = "bigDecimalToInt"),
            @Mapping(target = "grossWeight", source = "soB2cDeclareProductEntity.weight"),
            @Mapping(target = "quantity", source = "soB2cDeclareProductEntity.qty"),
            @Mapping(target = "customsCode", source = "soB2cDeclareProductEntity.toCustomsCode"),
            @Mapping(target = "declareChineseName", source = "soB2cDeclareProductEntity.declareCn"),
            @Mapping(target = "declareEnglishName", source = "soB2cDeclareProductEntity.declareEn"),
            @Mapping(target = "childOrderId", source = "soB2cDetail.sourceDetailId", qualifiedByName = "stringToLong"),
            @Mapping(target = "declareCurrency", source = "productDTO.declareCurrency"),
            @Mapping(target = "declareCurrencySymbol", source = "productDTO.declareCurrencySymbol"),
            @Mapping(target = "declareElement", source = "productDTO.declareElement"),
            @Mapping(target = "declareModel", source = "productDTO.declareModel"),
            @Mapping(target = "declarePrice", source = "productDTO.declarePrice"),
            @Mapping(target = "declareUnit", source = "productDTO.declareUnit"),
            @Mapping(target = "distributionInfo", ignore = true),
            @Mapping(target = "englishMaterial", source = "productDTO.englishMaterial"),
            @Mapping(target = "englishUsage", source = "productDTO.englishUsage"),
            @Mapping(target = "exemption", source = "productDTO.exemption"),
            @Mapping(target = "isElectric", source = "productDTO.isElectric"),
            @Mapping(target = "onlyBattery", source = "productDTO.onlyBattery"),
            @Mapping(target = "isLiquid", source = "productDTO.isLiquid"),
            @Mapping(target = "productProperty", source = "productDTO.productProperty"),
            @Mapping(target = "productPropertyId", source = "productDTO.productPropertyId"),
            @Mapping(target = "remark", ignore = true),
            @Mapping(target = "scItemCode", ignore = true),
            @Mapping(target = "scItemId", ignore = true),
            @Mapping(target = "scItemName", ignore = true),
            @Mapping(target = "skuName", ignore = true),
            @Mapping(target = "sourceCargo", source = "productDTO.sourceCargo"),
            @Mapping(target = "sourceCountry", source = "productDTO.sourceCountry"),
            @Mapping(target = "combinationDeclareType", source = "productDTO.combinationDeclareType"),
            @Mapping(target = "url", source = "soB2cDetail.imageUrl")
    })
    LogisticsProductVO convertDeclareProductVOByEntity(SoB2cDeclareProductEntity soB2cDeclareProductEntity, SoB2cDetailEntity soB2cDetail, LogisticsProductDTO.ProductDTO productDTO);


    @Mappings({
            @Mapping(target = "id",ignore = true),
            @Mapping(target = "version",ignore = true),
            @Mapping(target = "createTime",ignore = true),
            @Mapping(target = "createUserId",ignore = true),
            @Mapping(target = "createUserName",ignore = true),
            @Mapping(target = "updateTime",ignore = true),
            @Mapping(target = "updateUserId",ignore = true),
            @Mapping(target = "updateUserName",ignore = true),
    })
    SoB2cDetailEntity cloneSoB2cDetail(SoB2cDetailEntity soB2cDetailEntity);

    /**
     * 展示明细转换为拆分明细
     * @param emptyWarehouse
     * @return
     */
    SoB2cDTO.SplitDetailSaveDTO convertViewToSplitDto(SoB2cDTO.ViewSplitDetailDTO emptyWarehouse);
    List<SoB2cDTO.SplitDetailSaveDTO> convertViewToSplitDto(List<SoB2cDTO.ViewSplitDetailDTO> emptyWarehouseList);

    /**
     * 转换新增参数转换为订单明细
     * @param detail
     * @return
     */
    @Mapping(target = "skuId", source = "skuId")
    @Mapping(target = "qty", source = "qty")
    SoB2cDetailEntity convertAddToDetail(SoB2cDetailDTO.AddDTO detail);
    List<SoB2cDetailEntity> convertAddToDetail(List<SoB2cDetailDTO.AddDTO> detailList);

    /**
     * 转换更新参数为订单明
     * @param detail
     * @return
     */
    @Mapping(target = "skuId", source = "skuId")
    @Mapping(target = "qty", source = "qty")
    SoB2cDetailEntity convertUpdateToDetail(SoB2cDetailDTO.UpdateDTO detail);
    List<SoB2cDetailEntity> convertUpdateToDetail(List<SoB2cDetailDTO.UpdateDTO> detailList);

    /**
     * 根据订单进行预览数据转换
     * @param soB2cEntity
     * @return
     */
    SoB2cDTO.ViewDTO convertEntityToViewDTO(SoB2cEntity soB2cEntity);
    /**
     * 转换订单明细
     * @param dto
     * @return
     */
    @Mapping(target = "mainId", source = "id")
    @Mapping(target = "imageUrl", source = "imageUrl")
    @Mapping(target = "skuId", source = "skuId")
    @Mapping(target = "skuNo", source = "skuNo")
    @Mapping(target = "qty", source = "qty")
    @Mapping(target = "warehouseId", source = "warehouseId")
    @Mapping(target = "warehouseName", source = "warehouseName")
    @Mapping(target = "id", ignore = true)
    SoB2cDetailEntity convertB2cDetailByGiftDto(SoB2cDTO.GiftDTO dto);
    List<SoB2cDetailEntity> convertB2cDetailByGiftDto(List<SoB2cDTO.GiftDTO> dtoList);
}
