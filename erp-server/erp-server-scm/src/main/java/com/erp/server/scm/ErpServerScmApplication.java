package com.erp.server.scm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;


/**
 * @author yl
 */
@Slf4j
@SpringBootApplication(scanBasePackages = {"com.erp.*","com.common.*"})
@EnableFeignClients(basePackages = {"com.erp.rpc.*"})
@EnableDiscoveryClient
@ServletComponentScan
public class ErpServerScmApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErpServerScmApplication.class, args);
        log.info("================ SCM 启动成功 ================");
    }

}
