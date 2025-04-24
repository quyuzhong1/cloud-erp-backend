package com.erp.server.srm.convert;

import com.common.business.mapper.BooleanMapperWork;
import com.erp.model.srm.dto.OrderAcceptDTO;
import com.erp.model.srm.dto.ReturnConfirmDTO;
import com.erp.model.srm.entity.CfgSettingEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * @author zdy
 * @ClassName CfgSettingConfigConverter
 * @date 2024年01月11日
 * @version: 1.0
 */
@Mapper(uses = {BooleanMapperWork.class})
public interface CfgSettingConfigConverter {
    CfgSettingConfigConverter INSTANCE = Mappers.getMapper(CfgSettingConfigConverter.class);

    @Mapping(target = "index", constant = "1")
    @Mapping(target = "disabled", constant = "false")
    @Mapping(target = "key", constant = "orderAutoAccept")
    CfgSettingEntity configToOrderAcceptEntity(OrderAcceptDTO orderAcceptDTO);

    @Mapping(target = "index", constant = "2")
    @Mapping(target = "disabled", constant = "false")
    @Mapping(target = "key", constant = "returnAutoConfirm")
    CfgSettingEntity configToReturnConfigEntity(ReturnConfirmDTO returnConfirmDTO);
}
