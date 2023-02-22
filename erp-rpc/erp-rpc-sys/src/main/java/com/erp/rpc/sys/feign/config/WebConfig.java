package com.erp.rpc.sys.feign.config;


import com.common.business.interceptor.CommonInterceptor;
import com.erp.rpc.sys.feign.aspect.DataPermissionAspect;
import com.erp.rpc.sys.feign.aspect.RequestPermissionsAspect;
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
