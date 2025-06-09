package com.erp.server.wms.convert;

import com.erp.model.wms.dto.WmsVirtualDetailMsgDTO;
import com.erp.model.wms.entity.WmsVirtualDetailMsgEntity;
import com.erp.server.wms.convert.tool.TypeConversionWorker;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

/**
 * FBA货件实体映射工具
 * @Author Luo_WG
 * @Date 2023/10/31 18:55
 **/
@Mapper(uses = TypeConversionWorker.class)
@Component
public interface WmsVirtualDetailMsgConverter {
    WmsVirtualDetailMsgConverter INSTANCE = Mappers.getMapper(WmsVirtualDetailMsgConverter.class);
    /**
     * 新增对象赋值
     */
    WmsVirtualDetailMsgEntity wmsVirtualDetailMsgToAdd(WmsVirtualDetailMsgDTO.AddDTO addDto);
}
