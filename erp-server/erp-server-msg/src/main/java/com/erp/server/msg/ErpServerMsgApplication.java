package com.erp.server.msg;

import io.seata.spring.boot.autoconfigure.SeataAutoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@Slf4j
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, SeataAutoConfiguration.class}, scanBasePackages ={"com.erp","com.common"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.erp.rpc"})
public class ErpServerMsgApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErpServerMsgApplication.class, args);
        log.info("================ MSG 启动成功 ================");
    }

}
