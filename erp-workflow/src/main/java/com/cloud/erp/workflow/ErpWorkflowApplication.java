package com.cloud.erp.workflow;

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableProcessApplication
@SpringBootApplication
@EnableDiscoveryClient
public class ErpWorkflowApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErpWorkflowApplication.class, args);
    }

}
