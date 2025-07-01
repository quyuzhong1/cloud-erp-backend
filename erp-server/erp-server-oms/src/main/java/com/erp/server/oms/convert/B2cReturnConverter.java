package com.erp.server.oms.convert;

import com.common.business.mapper.NumberMapperWork;
import com.common.business.mapper.ObjectMapperWork;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.dto.SoB2cReturnDTO;
import com.erp.model.oms.dto.SoB2cReturnDetailDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author zdy
 * @ClassName B2cReturnConverter
 * @description: 销售退货单转换器
 * @date 2025年07月01日
 * @version: 1.0
 */
@Component
@Mapper(uses = {ObjectMapperWork.class, NumberMapperWork.class})
public interface B2cReturnConverter {
    B2cReturnConverter INSTANCE = Mappers.getMapper(B2cReturnConverter.class);

    @Mapping(target = "type", expression = "java(com.erp.model.wms.enums.ReturnTypeEnum.CUSTOMER_RETURNS.getCode())")
    @Mapping(target = "sysReturnTime", ignore = true)
    @Mapping(target = "status", expression = "java(com.erp.model.oms.enums.SoB2cReturnStatusEnum.TO_BE_RETURNED.getCode())")
    @Mapping(target = "sourceType", expression = "java(com.erp.model.oms.enums.SoB2cReturnSourceTypeEnum.SELF_ADD.getCode())")
    @Mapping(target = "soId", source = "id")
    @Mapping(target = "soCode", source = "code")
    @Mapping(target = "reason", source = "returnReason")
    @Mapping(target = "platformReturnNo", ignore = true)
    @Mapping(target = "detailList", ignore = true)
    SoB2cReturnDTO.AddDTO generateSoB2cReturnViewDTOToAddDTO(SoB2cDTO.GenerateSoB2cReturnViewDTO generateSoB2cReturnViewDTO);

    @Mapping(target = "soDetailId", source = "detailId")
    @Mapping(target = "mainId", ignore = true)
    SoB2cReturnDetailDTO.AddDTO generateSoB2cReturnDetailViewDTOToAddDTO(SoB2cDTO.GenerateSoB2cReturnViewDTO dto);
    List<SoB2cReturnDetailDTO.AddDTO> generateSoB2cReturnDetailViewDTOToAddDTO(List<SoB2cDTO.GenerateSoB2cReturnViewDTO> val);
}
