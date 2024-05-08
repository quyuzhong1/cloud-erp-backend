package com.erp.server.tms.convert;

import com.common.business.mapper.BigDecimalMapperWork;
import com.common.business.mapper.BooleanMapperWork;
import com.erp.model.tms.dto.TmsFirstMileLogisticDTO;
import com.erp.model.tms.dto.transfer.TransferLogisticsCreateInboundReq;
import com.erp.model.tms.dto.transfer.TransferLogisticsCreateOrderReq;
import com.erp.model.tms.dto.transfer.TransferLogisticsCreateProductReq;
import com.erp.model.tms.dto.transfer.TransferLogisticsOrderDTO;
import com.erp.model.tms.entity.LogisticsBillEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.entity.ProductRegistrationEntity;
import com.erp.model.tms.entity.TransferLogisticsChannelEntity;
import com.erp.model.tms.enums.TransferLogisticsStatusEnum;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.sdk.tms.baohong.api.asn.ReceivingInfo;
import com.sdk.tms.baohong.api.asn.ReceivingItemsType;
import com.sdk.tms.baohong.api.order.CreateOrderInfo;
import com.sdk.tms.baohong.api.order.OrderDataArr;
import com.sdk.tms.baohong.api.order.ProductDeatil;
import com.sdk.tms.baohong.api.order.SmRow;
import com.sdk.tms.baohong.api.product.DataRow;
import com.sdk.tms.baohong.api.product.ProductRow;
import com.sdk.tms.baohong.api.product.RecordItemRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author lrp
 * @description: 头程物流单
 */
@Mapper(uses = {TypeConversionWorker.class,BooleanMapperWork.class, BigDecimalMapperWork.class})
public interface FmLogisticsConverter {

    FmLogisticsConverter INSTANCE = Mappers.getMapper(FmLogisticsConverter.class);

    @Mappings({
            @Mapping(target = "shopId", source = "deliveryDTO.shopId"),
            @Mapping(target = "shopName", source = "deliveryDTO.shopName"),
            @Mapping(target = "sourceType", source = "deliveryDTO.sourceType"),
            @Mapping(target = "sourceId", source = "deliveryDTO.sourceId"),
            @Mapping(target = "sourceCode", source = "deliveryDTO.sourceCode"),
            @Mapping(target = "outstockId", source = "deliveryDTO.outstockId"),
            @Mapping(target = "outstockCode", source = "deliveryDTO.outstockCode"),
            @Mapping(target = "channelId", source = "addDTO.logisticsChannelId"),
//            @Mapping(target = "orderTime", source = "addDTO.logisticsOrderTime"),
            @Mapping(target = "deliveryTime", source = "deliveryDTO.approveTime"),
            @Mapping(target = "transportNo", source = "addDTO.transportNo"),
            @Mapping(target = "toCountry", source = "deliveryDTO.toCountryName"),
            @Mapping(target = "orderType", expression = "java(com.common.business.enums.OrderTypeEnum.FIRST_MILE.getCode())"),
            @Mapping(target = "counterNo", source = "addDTO.counterNo"),
            @Mapping(target = "carrierId", source = "addDTO.carrierId"),
            @Mapping(target = "shippingMethod", source = "addDTO.shippingMethod"),
            @Mapping(target = "logisticsSupplierId", source = "addDTO.logisticsSupplierId"),
            @Mapping(target = "remark", source = "addDTO.remark"),
    })
    LogisticsBillEntity addLogisticsBill(FirstMileDeliveryDTO.GenerateLogisticDTO deliveryDTO, TmsFirstMileLogisticDTO.CommonDTO addDTO);

}
