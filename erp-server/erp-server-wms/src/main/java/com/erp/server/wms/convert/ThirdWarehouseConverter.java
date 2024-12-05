package com.erp.server.wms.convert;

import com.erp.model.tms.dto.ShippingCalculationDTO;
import com.erp.model.wms.dto.PackingTaskDetailDTO;
import com.erp.model.wms.dto.WmsCartonDetailDTO;
import com.erp.model.wms.dto.WmsCartonSpecDTO;
import com.erp.model.wms.dto.third.ThirdWarehouseCalculateFeeReq;
import com.erp.model.wms.dto.third.ThirdWarehouseCalculateFeeResponse;
import com.erp.model.wms.entity.WmsCartonDetailEntity;
import com.erp.model.wms.entity.WmsCartonEntity;
import com.erp.model.wms.entity.WmsCartonSpecEntity;
import com.erp.server.wms.convert.tool.TypeConversionWorker;
import com.sdk.wms.antu.dto.request.AntuCalculateFeeReq;
import com.sdk.wms.antu.dto.response.AntuCalculateFeeResp;
import com.sdk.wms.goodcang.dto.request.GoodCangCalculateDeliveryFeeReq;
import com.sdk.wms.goodcang.dto.response.GoodCangCalculateDeliveryFeeResp;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 装箱明细转换类
 * @Author zdy
 * @Date 2024/7/2 18:55
 **/
@Mapper(uses = TypeConversionWorker.class)
@Component
public interface ThirdWarehouseConverter {
    ThirdWarehouseConverter INSTANCE = Mappers.getMapper(ThirdWarehouseConverter.class);


    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createUserId", source = "createUserId"),
            @Mapping(target = "createUserName", source = "createUserName"),
            @Mapping(target = "createTime", source = "createTime"),
            @Mapping(target = "updateUserId", source = "updateUserId"),
            @Mapping(target = "updateUserName", source = "updateUserName"),
            @Mapping(target = "updateTime", source = "updateTime"),
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "isDeleted", ignore = true),
            @Mapping(target = "boxSpecNo", source = "boxSpecNo"),
            @Mapping(target = "packageWeight", source = "packageWeight"),
            @Mapping(target = "weightUnit", constant = "kg"),
            @Mapping(target = "boxLength", source = "boxLength"),
            @Mapping(target = "boxWidth", source = "boxWidth"),
            @Mapping(target = "boxHeight", source = "boxHeight"),
            @Mapping(target = "sizeUnit", constant = "cm"),
            @Mapping(target = "boxQty", constant = "1"),
            @Mapping(target = "mainId", ignore = true),
            @Mapping(target = "measureSource", constant = "manual")
    })
    WmsCartonSpecEntity historyToSpec(PackingTaskDetailDTO.HistoryCartonDTO historyCartonDTO);
    @Mappings({
            @Mapping(target = "postcode", source = "zipCode"),
            @Mapping(target = "state", source = "province")
    })
    AntuCalculateFeeReq reqToAntuCalculateFeeReq(ThirdWarehouseCalculateFeeReq calculateFeeReq);

    @Mapping(target = "totalShippingCost", source = "totalFee")
    @Mapping(target = "currency", source = "currencyCode")
    @Mapping(target = "shippingCost", source = "SHIPPING")
    @Mapping(target = "operatingCost", source = "OPF")
    @Mapping(target = "otherCost", expression = "java(antuCalculateFeeResp.getFSC() + antuCalculateFeeResp.getDT() + antuCalculateFeeResp.getWHF() + antuCalculateFeeResp.getOTF())")
    @Mapping(target = "registrationCost", source = "RSF")
    @Mapping(target = "channelCode", source = "shippingMethod")
    @Mapping(target = "channelNameEn", source = "shippingName")
    @Mapping(target = "channelName", source = "shippingNameCn")
    ThirdWarehouseCalculateFeeResponse antuResToThirdWarehouseResponse(AntuCalculateFeeResp antuCalculateFeeResp);
    List<ThirdWarehouseCalculateFeeResponse> antuResToThirdWarehouseResponse(List<AntuCalculateFeeResp> antuCalculateFeeRespList);

    GoodCangCalculateDeliveryFeeReq reqToGucangCalculateFeeReq(ThirdWarehouseCalculateFeeReq calculateFeeReq);

    @Mapping(target = "channelCode", source = "smCode")
    @Mapping(target = "channelNameEn", source = "smName")
    @Mapping(target = "channelName", source = "smCode")
    @Mapping(target = "totalShippingCost", source = "total")
    ThirdWarehouseCalculateFeeResponse gucangResToThirdWarehouseResponse(GoodCangCalculateDeliveryFeeResp goodCangCalculateDeliveryFeeResp);
    List<ThirdWarehouseCalculateFeeResponse> gucangResToThirdWarehouseResponse(List<GoodCangCalculateDeliveryFeeResp> goodCangCalculateDeliveryFeeRespList);

    @Mapping(target = "templateRuleEntity", ignore = true)
    @Mapping(target = "templateEntity", ignore = true)
    @Mapping(target = "otherCostDTO", ignore = true)
    ShippingCalculationDTO.ListDTO responseToShippingDTO(ThirdWarehouseCalculateFeeResponse response);
    List<ShippingCalculationDTO.ListDTO> responseToShippingDTO(List<ThirdWarehouseCalculateFeeResponse> responseList);
}
