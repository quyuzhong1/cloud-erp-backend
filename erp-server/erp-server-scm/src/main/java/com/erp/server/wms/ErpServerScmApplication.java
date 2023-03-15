package com.erp.server.wms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


/**
 * @author yl
 */
@Slf4j
@SpringBootApplication(scanBasePackages = {"com.erp.*","com.common.*"})
@EnableDiscoveryClient
public class ErpServerScmApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErpServerScmApplication.class, args);
        log.info("================ SCM 启动成功 ================");
    }

}
