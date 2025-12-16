package com.erp.server.wms.handler;

import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.server.wms.service.OverseasProviderService;
import com.erp.server.wms.service.ThirdWarehouseService;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 海外仓初始化处理
 */
@Service
public class ThirdWarehouseRegistry {
    private final Map<String, ThirdWarehouseService> handlers = new HashMap<>();

    @Resource
    private ApplicationContext context;

    @Resource
    private OverseasProviderService overseasProviderService;

    @PostConstruct
    public void init() {
        Map<String, ThirdWarehouseService> beans = context.getBeansOfType(ThirdWarehouseService.class);
        for (ThirdWarehouseService bean : beans.values()) {
            handlers.put(bean.getPlatForm().getCode(), bean);
        }
    }

    public ThirdWarehouseService getHandler(String platform) {
        return handlers.get(platform);
    }

    public ThirdWarehouseService getHandlerByAuthId(String authId) {
        OverseasProviderEntity authEntity = overseasProviderService.getById(authId);
        if(Objects.isNull(authEntity)){
            throw new ServiceException(ApiError.WH_OVERSEAS_PROVIDER_NOT_FOUND);
        }
        ThirdWarehouseService warehouseService = getHandler(authEntity.getCode());
        if(Objects.isNull(warehouseService)){
            throw new ServiceException(ApiError.COMMON_PROVIDER_SERVICE_NOT_ENABLED);
        }
        return warehouseService;
    }

}
