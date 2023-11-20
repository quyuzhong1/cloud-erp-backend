package com.erp.server.wms.handler;

import com.erp.server.wms.service.ThirdWarehouseService;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 海外仓初始化处理
 */
@Service
public class ThirdWarehouseRegistry {
    private final Map<String, ThirdWarehouseService> handlers = new HashMap<>();

    @Resource
    private ApplicationContext context;

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
}
