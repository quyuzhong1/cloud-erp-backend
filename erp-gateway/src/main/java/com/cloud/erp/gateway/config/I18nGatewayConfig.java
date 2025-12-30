package com.cloud.erp.gateway.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

@Configuration
public class I18nGatewayConfig {

    @Bean("gatewayMessageSource")
    public MessageSource gatewayMessageSource() {
        ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();
        ms.setBasename("classpath:i18n/messages");
        ms.setDefaultEncoding("UTF-8");
        ms.setCacheSeconds(3600);
        ms.setUseCodeAsDefaultMessage(true);
        return ms;
    }

}
