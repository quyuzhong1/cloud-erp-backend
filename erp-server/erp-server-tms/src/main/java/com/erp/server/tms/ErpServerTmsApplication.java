package com.erp.server.tms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@Slf4j
@SpringBootApplication(scanBasePackages = {"com.erp.*","com.common.*"})
@EnableDiscoveryClient
public class ErpServerTmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErpServerTmsApplication.class, args);
        log.info("================ TMS 启动成功 ================");
    }

}
