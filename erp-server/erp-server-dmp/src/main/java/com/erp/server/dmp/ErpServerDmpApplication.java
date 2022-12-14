package com.erp.server.dmp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = MongoAutoConfiguration.class,scanBasePackages ={"com.erp.*"})
@EnableFeignClients(basePackages = {"com.erp.rpc.*"})
@EnableScheduling
public class ErpServerDmpApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErpServerDmpApplication.class, args);
    }

}
