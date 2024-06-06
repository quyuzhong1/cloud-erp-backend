package com.erp.server.wms.config;

import com.erp.model.wms.enums.inventory.InventoryBizTypeEnum;
import com.erp.server.wms.annotation.InventoryHandler;
import com.erp.server.wms.service.VirtualInventoryStockService;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 给服务bean进行初始化
 * @author will
 * @date 2024/6/5 17:42
 */
@Component
public class VirtualInventoryHelper {
    @Resource
    private ApplicationContext context;

    private final Map<InventoryBizTypeEnum, VirtualInventoryStockService> handlers=new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        Map<String, VirtualInventoryStockService> beans = context.getBeansOfType(VirtualInventoryStockService.class);
        for (VirtualInventoryStockService bean : beans.values()) {
            Class<?> actualClass = AopProxyUtils.ultimateTargetClass(bean);
            InventoryHandler annotation = actualClass.getAnnotation(InventoryHandler.class);
            handlers.put(annotation.value(),bean);
        }

    }

    public VirtualInventoryStockService getInventoryService(InventoryBizTypeEnum inventoryBizTypeEnum) {
        return handlers.get(inventoryBizTypeEnum);
    }

}