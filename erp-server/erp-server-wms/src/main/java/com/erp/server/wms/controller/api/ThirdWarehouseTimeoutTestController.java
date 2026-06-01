package com.erp.server.wms.controller.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 三方仓超时重试测试接口。
 */
@RestController
@RequestMapping("/test")
public class ThirdWarehouseTimeoutTestController {

    @GetMapping("/thirdWarehouseTimeout")
    public String thirdWarehouseTimeout() throws InterruptedException {
        Thread.sleep(35000);
        return "ok";
    }
}
