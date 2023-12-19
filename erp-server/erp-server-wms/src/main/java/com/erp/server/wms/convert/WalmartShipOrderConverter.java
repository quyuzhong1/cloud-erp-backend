package com.erp.server.wms.convert;

import com.common.business.dto.WalmartShipDTO;
import com.common.business.dto.WalmartShipOrderDetailDTO;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.wms.dto.RequisitionApplicationDTO;
import com.erp.model.wms.dto.RequisitionApplicationDetailDTO;
import com.erp.model.wms.dto.TransferInfoDetailDTO;
import com.erp.model.wms.entity.RequisitionApplicationDetailEntity;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import com.erp.server.wms.convert.tool.TypeConversionWorker;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface WalmartShipOrderConverter {

    WalmartShipOrderConverter INSTANCE = Mappers.getMapper(WalmartShipOrderConverter.class);

    @Mappings({
            @Mapping(target = "shopId", source = "soB2cEntity.shopId"),
            @Mapping(target = "platformCode", source = "soB2cEntity.platformCode"),
            @Mapping(target = "soCode", source = "soB2cEntity.code"),
            @Mapping(target = "trackNo", source = "soB2cLogisticsEntity.code"),
            @Mapping(target = "shipDateTime", source = "soB2cLogisticsEntity.deliveryTime"),
            @Mapping(target = "detailList", ignore = true)
    })
    WalmartShipDTO soB2cEntityToWalmartShipDTO(SoB2cEntity soB2cEntity, SoB2cLogisticsEntity soB2cLogisticsEntity);

    @Mappings({
            @Mapping(target = "platformLineNumber", source = "platformLineNumber"),
            @Mapping(target = "platformSkuNo", source = "platformSkuNo"),
            @Mapping(target = "platformSpuNo", source = "platformSpuNo"),
            @Mapping(target = "qty", source = "qty"),
    })
    WalmartShipOrderDetailDTO soB2cDetailEntityToWalmartShipOrderDetail(SoB2cDetailEntity soB2cDetailEntity);
    List<WalmartShipOrderDetailDTO> soB2cDetailEntityToWalmartShipOrderDetail(List<SoB2cDetailEntity> soB2cDetailEntity);

}
