package com.erp.server.msg;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

@Slf4j
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class, scanBasePackages ={"com.erp.*","com.common.*"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.erp.rpc.*"})
@EnableAsync
public class ErpServerMsgApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErpServerMsgApplication.class, args);
        log.info("================ MSG 启动成功 ================");
    }

}
