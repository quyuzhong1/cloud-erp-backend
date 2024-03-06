package com.erp.server.tms.convert;

import com.common.business.mapper.BigDecimalToIntMapperWork;
import com.erp.model.plm.dto.LogisticsProductDTO;
import com.erp.model.tms.dto.LogisticsBillDTO;
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
            @Mapping(target = "serviceName", source = "serviceName"),
            @Mapping(target = "logisticsType", source = "logisticsType")
    })
    LogisticsServicePlatformEntity convertLogisticsService(LogisticsServiceResponseVO obj);
    List<LogisticsServicePlatformEntity> convertLogisticsService( List<LogisticsServiceResponseVO> list);
}
