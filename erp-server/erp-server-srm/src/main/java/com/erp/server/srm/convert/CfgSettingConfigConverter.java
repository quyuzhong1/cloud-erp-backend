package com.erp.server.srm.convert;

import com.alibaba.fastjson.JSONObject;
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
    @Mapping(target = "dataJson", source = "orderAcceptDTO", qualifiedByName = "getDataJson")
    @Mapping(target = "key", constant = "order_auto_accept")
    CfgSettingEntity ConfigToOrderAcceptEntity(OrderAcceptDTO orderAcceptDTO);

    @Mapping(target = "key", constant = "return_auto_confirm")
    @Mapping(target = "dataJson", source = "returnConfirmDTO", qualifiedByName = "getDataJson")
    CfgSettingEntity ConfigToReturnConfigEntity(ReturnConfirmDTO returnConfirmDTO);

    @Named("getDataJson")
    public default String getDataJson(Object object) {
        if (object instanceof OrderAcceptDTO){
            OrderAcceptDTO dto = (OrderAcceptDTO) object;
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("duration",dto.getDuration());
            jsonObject.put("unit",dto.getUnit());
            jsonObject.put("selectState",dto.getSelectState());
            return jsonObject.toJSONString();
        }else if (object instanceof ReturnConfirmDTO){
            ReturnConfirmDTO dto = (ReturnConfirmDTO) object;
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("duration",dto.getDuration());
            jsonObject.put("unit",dto.getUnit());
            jsonObject.put("selectState",dto.getSelectState());
            return jsonObject.toJSONString();
        }else {
            return "";
        }

    }
}
