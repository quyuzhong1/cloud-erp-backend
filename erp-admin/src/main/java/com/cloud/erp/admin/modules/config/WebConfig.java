package com.cloud.erp.admin.modules.config;

import com.cloud.erp.admin.modules.interceptor.SysInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Classname WebConfig
 * @Description TODO
 * @Date 2022-07-15 19:06
 * @Created by yl
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 添加自定义拦截器
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SysInterceptor() ).addPathPatterns("/**");
    }
}
