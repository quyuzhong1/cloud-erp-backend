package com.erp.server.dmp.pull.service;


import com.erp.server.dmp.enums.PlatformApiEnum;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SaveData {
    PlatformApiEnum method();
}
