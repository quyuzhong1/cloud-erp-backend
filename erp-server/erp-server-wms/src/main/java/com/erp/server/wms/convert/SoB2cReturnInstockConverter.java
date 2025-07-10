package com.erp.server.wms.convert;

import com.erp.model.oms.dto.SoB2cReturnDTO;
import com.erp.model.wms.dto.SoReturnInstockDTO;
import com.erp.model.wms.dto.SoReturnInstockDetailDTO;
import com.erp.server.wms.convert.tool.TypeConversionWorker;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 退货入库单
 * @Author zdy
 * @Date 2024/09/24 18:55
 **/
@Mapper(uses = TypeConversionWorker.class)
@Component
public interface SoB2cReturnInstockConverter {
    SoB2cReturnInstockConverter INSTANCE = Mappers.getMapper(SoB2cReturnInstockConverter.class);
    @Mappings({
            @Mapping(target = "soReturnId", source = "instockDTO.id"),
            @Mapping(target = "soReturnCode", source = "instockDTO.code"),
            @Mapping(target = "sourceId", ignore = true),
            @Mapping(target = "sourceCode", ignore = true),
            @Mapping(target = "platformOrderCode", source = "instockDTO.platformOrderNo"),
            @Mapping(target = "customerId", ignore = true),
            @Mapping(target = "shopId", source = "instockDTO.shopId"),
            @Mapping(target = "salesOrgId", ignore = true),
            @Mapping(target = "salesDeptId", ignore = true),
            @Mapping(target = "sellerId", ignore = true),
            @Mapping(target = "type", expression = "java(com.erp.model.oms.enums.BillTypeEnum.B2C.getCode())"),
            @Mapping(target = "sourceType", expression = "java(com.common.business.enums.SourceTypeEnum.SO_B2C_RETURN.getCode())"),
            @Mapping(target = "billDate", source = "instockDTO.billDate"),
            @Mapping(target = "warehouseId", source = "instockDTO.warehouseId"),
            @Mapping(target = "warehouseKeeperId", ignore = true),
            @Mapping(target = "returnLogisticCode", source = "instockDTO.returnLogisticCode"),
            @Mapping(target = "detailList", ignore = true),
            @Mapping(target = "exchangeRate", ignore = true),
            @Mapping(target = "currency", ignore = true),
            @Mapping(target = "currencySymbol", ignore = true)
    })
    SoReturnInstockDTO.Add soB2cReturnEntityToAdd(SoB2cReturnDTO.ReturnInstockDTO instockDTO);
    @Mappings({
            @Mapping(target = "exchangeRate", ignore = true),
            @Mapping(target = "isChildSkuNo", ignore = true),
            @Mapping(target = "platformSkuNo", ignore = true),
            @Mapping(target = "realQty", source = "instockQty"),
            @Mapping(target = "mustQty", source = "instockQty"),
            @Mapping(target = "receiveQty", source = "instockQty"),
            @Mapping(target = "remark", ignore = true),
            @Mapping(target = "returnAmount", ignore = true),
            @Mapping(target = "returnAmountLocalCurrency", ignore = true),
            @Mapping(target = "returnReasonDict", ignore = true),
            @Mapping(target = "returnTypeDict", expression = "java(com.erp.model.wms.enums.ReturnTypeEnum.DEDUCTION.getCode())"),
            @Mapping(target = "soReturnDetailId", source = "detailId"),
            @Mapping(target = "sourceDetailId", ignore = true),
            @Mapping(target = "taxReturnAmount", ignore = true),
            @Mapping(target = "taxReturnAmountLocalCurrency", ignore = true),
            @Mapping(target = "warehouseLocation", ignore = true),
            @Mapping(target = "isCheckReceiveQty", ignore = true)
    })
    SoReturnInstockDetailDTO.Add soB2cReturnDetailEntityToAdd(SoB2cReturnDTO.ReturnInstockDTO returnInstockDTO);
    List<SoReturnInstockDetailDTO.Add> soB2cReturnDetailEntityToAdd(List<SoB2cReturnDTO.ReturnInstockDTO> returnInstockDTOS);
}
