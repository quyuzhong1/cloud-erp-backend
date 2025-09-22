package com.cloud.erp.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@Slf4j
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.erp.rpc.*"})
public class ErpGatewayApplication {

    public static void main(String[] args) {
        SpringApplication springApplication=new SpringApplication(
                ErpGatewayApplication.class
        );

        springApplication.setBannerMode(Banner.Mode.LOG);
        springApplication.run(args);
        log.info("================ Gateway 启动成功 ================");
    }

}
