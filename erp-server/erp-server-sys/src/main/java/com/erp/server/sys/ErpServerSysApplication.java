package com.erp.server.sys;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;


@SpringBootApplication
@EnableFeignClients(basePackages = {"com.erp.rpc.*"})
@EnableDiscoveryClient
public class ErpServerSysApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErpServerSysApplication.class, args);
    }

}
