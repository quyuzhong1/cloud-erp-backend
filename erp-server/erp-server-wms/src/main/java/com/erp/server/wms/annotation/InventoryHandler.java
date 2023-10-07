package com.erp.server.wms.annotation;

import com.erp.model.wms.enums.inventory.InventoryBizTypeEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface InventoryHandler {
    InventoryBizTypeEnum value();
}
