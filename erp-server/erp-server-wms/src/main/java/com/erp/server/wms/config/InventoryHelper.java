package com.erp.server.wms.config;

import com.erp.model.wms.enums.inventory.*;
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
    private ApplicationContext applicationContext;

    private static Map<InventoryBizTypeEnum, InventoryStockService> inventoryServiceMap;

    @PostConstruct
    public void init() {
        Map<String,InventoryStockService> springInventoryServiceMap  = applicationContext.getBeansOfType(InventoryStockService.class);
        inventoryServiceMap = new ConcurrentHashMap<>();
        springInventoryServiceMap.forEach((key,value) -> inventoryServiceMap.put(value.handlerType(),value));
    }

    public AbstractInventoryServiceImpl getInventoryService(InventoryBizTypeEnum inventoryBizTypeEnum) {
        return (AbstractInventoryServiceImpl)inventoryServiceMap.get(inventoryBizTypeEnum);
    }


}