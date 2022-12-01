package com.erp.server.dmp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = MongoAutoConfiguration.class)
@EnableScheduling
public class ErpServerDmpApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErpServerDmpApplication.class, args);
    }

}
