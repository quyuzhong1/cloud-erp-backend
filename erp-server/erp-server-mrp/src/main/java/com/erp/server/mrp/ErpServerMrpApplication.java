package com.erp.server.mrp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.elasticsearch.config.EnableElasticsearchAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@Slf4j
@SpringBootApplication(scanBasePackages = {"com.erp","com.common"})
@EnableFeignClients(basePackages = {"com.erp.rpc"})
@EnableDiscoveryClient
@EnableElasticsearchAuditing
@EnableAsync
public class ErpServerMrpApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErpServerMrpApplication.class, args);
        log.info("================ MRP 启动成功 ================");
    }

}
