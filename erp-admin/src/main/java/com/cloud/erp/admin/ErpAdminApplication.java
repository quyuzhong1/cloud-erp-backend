package com.cloud.erp.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
public class ErpAdminApplication {




    public static void main(String[] args) {
        SpringApplication.run(ErpAdminApplication.class, args);
    }

}
