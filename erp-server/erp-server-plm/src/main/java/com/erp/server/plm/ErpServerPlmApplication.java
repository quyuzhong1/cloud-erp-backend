package com.erp.server.plm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages ={"com.erp.*"})
@EnableFeignClients(basePackages = {"com.erp.rpc.*"})
@EnableDiscoveryClient
@EnableAsync
public class ErpServerPlmApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErpServerPlmApplication.class, args);
    }

}
