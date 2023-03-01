package com.erp.rpc.sys.feign.config;


import com.erp.rpc.sys.feign.aspect.DataPermissionAspect;
import com.erp.rpc.sys.feign.aspect.RequestPermissionsAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * web 配置 {@link WebConfig}
 *
 *
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public DataPermissionAspect getDataPermissionAspect(){
        return new DataPermissionAspect();
    }
    @Bean
    public RequestPermissionsAspect getRequestPermissionsAspect(){
        return new RequestPermissionsAspect();
    }
}
