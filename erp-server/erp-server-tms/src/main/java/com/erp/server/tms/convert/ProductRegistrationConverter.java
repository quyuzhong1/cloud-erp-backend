package com.erp.server.tms.convert;

import com.common.business.mapper.BigDecimalMapperWork;
import com.common.business.mapper.BooleanMapperWork;
import com.erp.model.plm.dto.LogisticsProductDTO;
import com.erp.model.tms.dto.transfer.TransferLogisticsCreateInboundReq;
import com.erp.model.tms.dto.transfer.TransferLogisticsCreateOrderReq;
import com.erp.model.tms.dto.transfer.TransferLogisticsCreateProductReq;
import com.erp.model.tms.dto.transfer.TransferLogisticsOrderDTO;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.entity.ProductRegistrationEntity;
import com.erp.model.tms.entity.TransferLogisticsChannelEntity;
import com.erp.model.tms.enums.TransferLogisticsStatusEnum;
import com.sdk.tms.baohong.api.asn.ReceivingInfo;
import com.sdk.tms.baohong.api.asn.ReceivingItemsType;
import com.sdk.tms.baohong.api.order.CreateOrderInfo;
import com.sdk.tms.baohong.api.order.OrderDataArr;
import com.sdk.tms.baohong.api.order.ProductDeatil;
import com.sdk.tms.baohong.api.order.SmRow;
import com.sdk.tms.baohong.api.product.DataRow;
import com.sdk.tms.baohong.api.product.RecordItemRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author lrp
 */
@Mapper(uses = {TypeConversionWorker.class,BooleanMapperWork.class, BigDecimalMapperWork.class})
public interface ProductRegistrationConverter {

    ProductRegistrationConverter INSTANCE = Mappers.getMapper(ProductRegistrationConverter.class);

    @Mappings({
            @Mapping(target = "sku", source = "skuNo"),
            @Mapping(target = "name", source = "cnName"),
            @Mapping(target = "englishName", source = "enName"),
            @Mapping(target = "unit", source = "declareUnit"),
            @Mapping(target = "model", source = "declareModel"),
            @Mapping(target = "currencyCode", source = "declareCurrency"),
            @Mapping(target = "declaredValue", source = "declarePrice"),
            @Mapping(target = "weight", expression = "java(java.util.Objects.nonNull(productDTO.getGrossWeight())?productDTO.getGrossWeight().divide(java.math.BigDecimal.valueOf(1000)):null)"),
            @Mapping(target = "length", source = "boxSizeLength"),
            @Mapping(target = "width", source = "boxSizeWide"),
            @Mapping(target = "height", source = "boxSizeHigh"),
            @Mapping(target = "hasBattery", source = "isElectric"),
            @Mapping(target = "batteryType", source = "batteryType"),
            @Mapping(target = "hsName", source = "declareChineseName"),
            @Mapping(target = "hsCode", source = "customsCode"),
            @Mapping(target = "hsElement", source = "declareElement"),
            @Mapping(target = "firstQauntity", source = "firstQty"),
            @Mapping(target = "secondQauntity", source = "secondQty"),
            @Mapping(target = "productProperty", source = "productProperty"),
    })
    TransferLogisticsCreateProductReq convertToCreateProduct(LogisticsProductDTO.ProductDTO productDTO);
}
