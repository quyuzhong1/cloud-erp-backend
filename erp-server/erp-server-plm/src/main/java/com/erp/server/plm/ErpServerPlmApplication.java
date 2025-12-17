package com.erp.server.plm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

@Slf4j
@EnableCaching
@SpringBootApplication(scanBasePackages ={"com.erp","com.common"})
@EnableFeignClients(basePackages = {"com.erp.rpc"})
@EnableDiscoveryClient
@EnableAsync
@ServletComponentScan
public class ErpServerPlmApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErpServerPlmApplication.class, args);
        log.info("================ PLM 启动成功 ================");
    }

}
