package com.erp.server.workflow;

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableProcessApplication
@SpringBootApplication
@EnableFeignClients(basePackages = {"com.erp.rpc.*"})
@EnableDiscoveryClient
public class ErpServerWorkflowApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErpServerWorkflowApplication.class, args);
    }

}
