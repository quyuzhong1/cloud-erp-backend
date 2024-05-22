package com.erp.server.oms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;


/**
 * OMS
 */
@Slf4j
@EnableCaching
@SpringBootApplication(scanBasePackages = {"com.erp.*","com.common.*"})
@EnableFeignClients(basePackages = {"com.erp.rpc.*"})
@EnableDiscoveryClient
public class ErpServerOmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErpServerOmsApplication.class, args);
        log.info("================ OMS 启动成功 ================");
    }

}
