package com.erp.server.srm.convert;

import cn.hutool.json.JSONObject;
import com.common.business.mapper.BooleanMapperWork;
import com.erp.model.srm.dto.OrderAcceptDTO;
import com.erp.model.srm.dto.ReturnConfirmDTO;
import com.erp.model.srm.entity.CfgSettingEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.Objects;

/**
 * @author zdy
 * @ClassName CfgSettingConfigConverter
 * @description: TODO
 * @date 2024年01月11日
 * @version: 1.0
 */
@Mapper(uses = {BooleanMapperWork.class})
public interface CfgSettingConfigConverter {
    CfgSettingConfigConverter INSTANCE = Mappers.getMapper(CfgSettingConfigConverter.class);

    @Mapping(target = "index", constant = "1")
    @Mapping(target = "disabled", constant = "false")
    @Mapping(target = "key", constant = "order_auto_accept")
    CfgSettingEntity ConfigToOrderAcceptEntity(OrderAcceptDTO orderAcceptDTO);

    @Mapping(target = "index", constant = "2")
    @Mapping(target = "disabled", constant = "false")
    @Mapping(target = "key", constant = "return_auto_confirm")
    CfgSettingEntity ConfigToReturnConfigEntity(ReturnConfirmDTO returnConfirmDTO);
}
