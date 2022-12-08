package com.erp.common.business.config;

import com.erp.common.business.aspect.DataPermissionAspect;
import com.erp.common.business.aspect.RequestPermissionsAspect;
import com.erp.common.business.interceptor.CommonInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * web 配置 {@link WebConfig}
 *
 *
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    //拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        CommonInterceptor commonInterceptor = new CommonInterceptor();
        registry.addInterceptor(commonInterceptor).addPathPatterns("/**");
    }

    @Bean
    public DataPermissionAspect getDataPermissionAspect(){
        return new DataPermissionAspect();
    }
    @Bean
    public RequestPermissionsAspect getRequestPermissionsAspect(){
        return new RequestPermissionsAspect();
    }
}
