package com.erp.server.file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages ={"com.erp","com.common"})
@EnableFeignClients(basePackages = {"com.erp.rpc"})
@EnableDiscoveryClient
@EnableCaching
@Slf4j
public class ErpServerFileApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErpServerFileApplication.class, args);
        log.info("================ FILE 启动成功 ================");
    }
}
