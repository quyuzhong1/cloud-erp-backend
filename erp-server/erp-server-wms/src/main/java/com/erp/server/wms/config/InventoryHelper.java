package com.erp.server.wms.config;

import com.erp.model.wms.enums.inventory.*;
import com.erp.server.wms.annotation.InventoryHandler;
import com.erp.server.wms.service.*;
import com.erp.server.wms.service.impl.AbstractInventoryServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Classname: InventoryHelper
 * @Description: 库存辅助配置类
 * @CreateTime: 2023-04-26  16:31
 * @Author: zhangchunlin
 */
@Slf4j
@Component
public class InventoryHelper {
    @Resource
    private ApplicationContext context;

    private final Map<InventoryBizTypeEnum, InventoryStockService> handlers=new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        Map<String, InventoryStockService> beans = context.getBeansOfType(InventoryStockService.class);
        for (InventoryStockService bean : beans.values()) {
            InventoryHandler annotation = bean.getClass().getAnnotation(InventoryHandler.class);
            handlers.put(annotation.value(),bean);
        }

    }

    public InventoryStockService getInventoryService(InventoryBizTypeEnum inventoryBizTypeEnum) {
        return handlers.get(inventoryBizTypeEnum);
    }

}