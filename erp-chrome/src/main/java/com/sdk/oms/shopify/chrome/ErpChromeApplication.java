package com.sdk.oms.shopify.chrome;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication(scanBasePackages = {"com.erp","com.common"})
public class ErpChromeApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErpChromeApplication.class, args);
        log.info("================ Chrome 启动成功 ================");
    }

}
