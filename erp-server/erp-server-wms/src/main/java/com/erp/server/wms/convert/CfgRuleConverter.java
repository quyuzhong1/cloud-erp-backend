package com.erp.server.wms.convert;

import com.erp.model.wms.dto.pickingstrategy.CfgRuleActionDTO;
import com.erp.model.wms.entity.CfgRulePackingActionEntity;
import com.erp.server.wms.convert.tool.TypeConversionWorker;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 配置转换
 * @Author zdy
 * @Date 2026/7/21 18:55
 **/
@Mapper(uses = TypeConversionWorker.class)
@Component
public interface CfgRuleConverter {
    CfgRuleConverter INSTANCE = Mappers.getMapper(CfgRuleConverter.class);

    CfgRuleActionDTO.View entityToView(CfgRulePackingActionEntity entity);
    List<CfgRuleActionDTO.View> entityToView(List<CfgRulePackingActionEntity> pickingActionEntityList);
}
