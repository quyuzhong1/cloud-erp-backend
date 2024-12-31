package com.erp.server.tms.convert;

import com.common.business.mapper.BigDecimalToIntMapperWork;
import com.erp.model.plm.dto.LogisticsProductDTO;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.tms.dto.LogisticsServicePlatformDTO;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.entity.LogisticsServicePlatformEntity;
import com.erp.model.tms.vo.request.LogisticsProductVO;
import com.erp.model.tms.vo.request.ParceInfoVO;
import com.erp.model.tms.vo.request.ReceiverInfoVO;
import com.erp.model.tms.vo.response.LogisticsServiceResponseVO;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 *物流单转化
 *@author yl
 *@date 2023-11-23
 */
@Mapper(uses = {BigDecimalToIntMapperWork.class},builder = @Builder(disableBuilder = true))
public interface LogisticsServiceConverter {

    LogisticsServiceConverter INSTANCE = Mappers.getMapper(LogisticsServiceConverter.class);






    @Mappings({
            @Mapping(target = "cnName", source = "serviceName"),
            @Mapping(target = "code", source = "logisticsType")
    })
    LogisticsSaleChannelEntity convertLogisticsService(LogisticsServiceResponseVO obj);
    List<LogisticsSaleChannelEntity> convertLogisticsService( List<LogisticsServiceResponseVO> list);

    @Mapping(target = "serviceName", source = "cnName")
    LogisticsServicePlatformDTO.ServiceNameDTO convertToServiceName(LogisticsSaleChannelEntity logisticsSaleChannelEntity);
    List<LogisticsServicePlatformDTO.ServiceNameDTO> convertToServiceName(List<LogisticsSaleChannelEntity> logisticsSaleChannelEntityList);
}
