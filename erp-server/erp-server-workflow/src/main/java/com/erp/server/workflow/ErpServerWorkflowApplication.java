package com.erp.server.workflow;

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableProcessApplication
@SpringBootApplication
@EnableDiscoveryClient
public class ErpServerWorkflowApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErpServerWorkflowApplication.class, args);
    }

}
