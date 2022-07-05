package com.cloud.erp.gateway;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


@SpringBootApplication
@EnableDiscoveryClient
public class ErpGatewayApplication {

    public static void main(String[] args) {
        SpringApplication springApplication=new SpringApplication(
                ErpGatewayApplication.class
        );
        springApplication.setBannerMode(Banner.Mode.LOG);
        springApplication.run(args);

    }

}
