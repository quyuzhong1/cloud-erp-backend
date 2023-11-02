package com.erp.server.wms.convert.tool;

import cn.hutool.json.JSONUtil;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.server.wms.service.WarehouseService;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
@Named("TypeConversionWorker")
public class TypeConversionWorker {

    /**
     * 根据仓库id获取name
     * @Author Luo_WG
     * @Date 2023/11/2 14:46
     * @param warehouseId 仓库id
     * @return java.lang.String
     **/
    @Named("warehouseIdByName")
    public String warehouseIdByName(String warehouseId, List<WarehouseEntity> warehouseEntities) {
        String name = warehouseEntities.stream().filter(req -> req.getId().equals(warehouseId)).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        return name;
    }

}
