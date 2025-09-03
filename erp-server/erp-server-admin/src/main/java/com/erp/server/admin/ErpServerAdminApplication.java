package com.erp.server.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@Slf4j
@SpringBootApplication(scanBasePackages = {"com.erp","com.common"})
@EnableDiscoveryClient
public class ErpServerAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErpServerAdminApplication.class, args);
        log.info("================ Admin 启动成功 ================");
    }

}
