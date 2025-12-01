package com.erp.server.fms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;


/**
 * 财务系统启动类
 * @author yl
 */
@Slf4j
@SpringBootApplication(scanBasePackages = {"com.erp","com.common"})
@EnableFeignClients(basePackages = {"com.erp.rpc"})
@EnableDiscoveryClient
@EnableAsync
@ServletComponentScan
public class ErpServerFmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErpServerFmsApplication.class, args);
        log.info("================ FMS 启动成功 ================");
    }

}

