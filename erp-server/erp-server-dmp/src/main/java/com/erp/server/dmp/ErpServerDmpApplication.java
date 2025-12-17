package com.erp.server.dmp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

@Slf4j
@SpringBootApplication(exclude = MongoAutoConfiguration.class, scanBasePackages ={"com.erp","com.common"})
@EnableFeignClients(basePackages = {"com.erp.rpc"})
@ServletComponentScan
public class ErpServerDmpApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErpServerDmpApplication.class, args);
        log.info("================ DMP 启动成功 ================");
    }

}
