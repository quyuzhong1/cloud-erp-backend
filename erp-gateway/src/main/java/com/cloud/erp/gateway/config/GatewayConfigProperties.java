package com.cloud.erp.gateway.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;


@ConfigurationProperties(prefix = GatewayConfigProperties.PREFIX)
@Component
@Data
public class GatewayConfigProperties {

    public static final String PREFIX = "spring.cloud.gateway";

    private List<Routes> routes;
    
    @Data
    public static class Routes{
    	private String id;
    	private String uri;
    	private List<String> predicates;
    	private List<String> filters;
    }
}
