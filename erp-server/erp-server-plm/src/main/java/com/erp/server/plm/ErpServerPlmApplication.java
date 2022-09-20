package com.erp.server.plm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ErpServerPlmApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErpServerPlmApplication.class, args);
    }

}
